package ru.iteco.attach.core.attach.web;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.iteco.attach.core.attach.AttachService;

import java.nio.file.NoSuchFileException;

@RestController
@RequestMapping
public class AttachController {
    private final AttachService attachService;

    public AttachController(final AttachService attachService) {
        this.attachService = attachService;
    }

    @PostMapping(value = {"/avatar", "/image", "/file"},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AttachResponse> upload(@RequestPart("file") FilePart filePart) {
        return attachService.store(filePart, "upload/");
    }

    @GetMapping(value = {"/avatar", "/image", "/file"})
    public Flux<DataBuffer> download(@RequestParam(name = "file") String fileName, final ServerHttpResponse response) {
        return attachService.resolveDownloadHeaders(fileName, response)
                .thenMany(attachService.load(fileName))
                .onErrorMap(NoSuchFileException.class, e -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping(value = {"/avatar", "/image", "/file"})
    public Mono<Void> delete(@RequestParam(name = "file") String fileName, final ServerHttpResponse response) {
        return attachService.delete(fileName)
                .doOnSuccess(r -> response.setStatusCode(HttpStatus.NO_CONTENT))
                .onErrorMap(NoSuchFileException.class, e -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
