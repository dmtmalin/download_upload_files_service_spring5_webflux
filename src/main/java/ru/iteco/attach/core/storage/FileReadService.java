package ru.iteco.attach.core.storage;

import io.netty.buffer.PooledByteBufAllocator;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Service
public class FileReadService extends FileClosableChanel {
    private final DataBufferFactory factory;
    private final int bufferSize = 64 * 1024;

    public FileReadService() {
        PooledByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;
        this.factory = new NettyDataBufferFactory(allocator);
    }

    public Flux<DataBuffer> readFile(String fileName, String rootUri) {
        Path path = this.getOsFileName(fileName, rootUri);
        return Mono.fromCallable(() -> AsynchronousFileChannel.open(path, StandardOpenOption.READ))
                .flatMapMany(channel -> DataBufferUtils
                        .readAsynchronousFileChannel(() -> channel, this.factory, bufferSize)
                        .doAfterTerminate(() -> this.close(channel)));
    }

    public Path getOsFileName(String fileName, String rootUri) {
        String name = FilenameUtils.getName(fileName);
        String prefix = FilenameUtils.getFullPath(fileName);
        String codedName = FileNameResolver.codedName(name);
        Path saveFileName = FileNameResolver.buildSaveFileName(codedName);
        return Path.of(rootUri, prefix, saveFileName.toString());
    }
}
