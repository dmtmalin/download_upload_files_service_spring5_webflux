package ru.iteco.attach.core.storage;

import java.nio.channels.AsynchronousFileChannel;

public interface ClosableChanel {
    void close(AsynchronousFileChannel channel);
}
