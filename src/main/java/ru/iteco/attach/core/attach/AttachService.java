package ru.iteco.attach.core.attach;

import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.iteco.attach.core.attach.web.AttachResponse;
import ru.iteco.attach.core.storage.FileReadService;
import ru.iteco.attach.core.storage.FileWriteService;
import ru.iteco.attach.property.StorageProperty;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class AttachService {

    private final StorageProperty property;
    private final FileWriteService writer;
    private final FileReadService reader;
    private final Tika tika;

    public AttachService(final StorageProperty property,
                         final FileWriteService writer,
                         final FileReadService reader) {
        this.property = property;
        this.writer = writer;
        this.reader = reader;
        this.tika = new Tika();
    }

    public Mono<AttachResponse> store(final FilePart filePart, String prefix) {
        return writer.writeFile(filePart, property.getRootUri(), prefix);
    }

    public Flux<DataBuffer> load(final String fileName) {
        return reader.readFile(fileName, property.getRootUri());
    }

    public Mono<Void> delete(final String fileName) {
        Path path = reader.getOsFileName(fileName, property.getRootUri());
        return Mono.fromCallable(() -> {
            Files.delete(path);
            return true;
        }).then();
    }

    public Mono<String> getMimeType(final String fileName) {
        Path path = reader.getOsFileName(fileName, property.getRootUri());
        return Mono.fromCallable(() -> this.tika.detect(path));
    }

    public Mono<Void> resolveDownloadHeaders(String fileName, final ServerHttpResponse response) {
        return getMimeType(fileName)
                .flatMap(mimeType -> {
                    response.getHeaders().setContentType(MediaType.valueOf(mimeType));
                    boolean mediaType = mimeType.contains("video/") || mimeType.contains("image/");
                    if (!mediaType) {
                        String name = FilenameUtils.getName(fileName);
                        response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + name);
                    }
                    return Mono.empty();
                }).then();
    }
}
