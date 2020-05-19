package ru.iteco.attach.core.storage;

import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.iteco.attach.core.attach.web.AttachResponse;

import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Service
public class FileWriteService extends FileClosableChanel {

    public Mono<AttachResponse> writeFile(final FilePart file, final String rootUri, final String prefix) {
        final String basePath = Path.of(rootUri, prefix).toString();
        FileNameResolver resolver = FileNameResolver.of(file.filename(), basePath);
        final String fileName = resolver.uniqueFileName();
        return resolver.resolveSaveFileName(fileName)
                .flatMap(this::createTargetFile)
                .flatMap(targetFile -> write(file, targetFile))
                .map(size -> AttachResponse.of(Path.of(prefix, fileName), size));
    }

    private Mono<Path> createTargetFile(final Path saveFileName) {
        final Path fullPath = Path.of(FilenameUtils.getFullPath(saveFileName.toString()));
        return Mono.fromCallable(() -> {
            if (!Files.isDirectory(fullPath))
                Files.createDirectories(fullPath);
            return Files.createFile(saveFileName);
        });
    }

    private Mono<Long> write(final FilePart file, final Path targetFile) {
        return Mono.fromCallable(() -> AsynchronousFileChannel.open(targetFile, StandardOpenOption.WRITE))
                .flatMap(channel ->
                        DataBufferUtils.write(file.content(), channel, 0)
                                .map(dataBuffer -> {
                                    long writeBytes = dataBuffer.writePosition();
                                    DataBufferUtils.release(dataBuffer);
                                    return writeBytes;
                                })
                                .reduce(0L, Long::sum)
                                .doAfterTerminate(() -> this.close(channel))
                                .map(size -> size)
                );
    }
}
