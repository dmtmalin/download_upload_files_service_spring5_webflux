package ru.iteco.attach.core.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.AsynchronousFileChannel;

public abstract class FileClosableChanel {
    private final static Logger logger = LoggerFactory.getLogger(FileClosableChanel.class);

    protected void close(AsynchronousFileChannel channel) {
        if (channel != null) {
            try {
                channel.close();
                logger.debug("complete close channel");
            } catch (Exception ignored) {
                logger.warn("can't close i/o channel");
            }
        }
    }
}
