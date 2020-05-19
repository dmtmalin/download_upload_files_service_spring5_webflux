package ru.iteco.attach.core.attach.web;

import java.nio.file.Path;

public class AttachResponse {
    private String uri;
    private Long size;

    public static AttachResponse of(final Path path, final Long size) {
        AttachResponse response = new AttachResponse();
        response.setUri(path.toString());
        response.setSize(size);
        return response;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }
}
