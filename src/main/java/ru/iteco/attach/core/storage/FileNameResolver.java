package ru.iteco.attach.core.storage;

import org.apache.commons.io.FilenameUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.util.retry.RetrySpec;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileNameResolver {
    private static final String regex = "^([0-9]*)([0-9])([0-9])([0-9])([0-9])([0-9])(\\..*)?$";
    private static final Pattern pattern = Pattern.compile(regex);

    private final String fileName;
    private final String basePath;

    public static FileNameResolver of(@NonNull final String fileName, @NonNull final String basePath) {
        return new FileNameResolver(fileName, basePath);
    }

    public FileNameResolver(final String fileName, final String basePath) {
        this.fileName = fileName;
        this.basePath = basePath;
    }

    public static class FileAlreadyExists extends RuntimeException {};

    public Mono<Path> resolveSaveFileName(final String unqFileName) {
        return Mono.fromCallable(() -> {
            String codedName = codedName(unqFileName);
            Path saveFileName = buildSaveFileName(codedName);
            Path fullFileName = Path.of(this.basePath, saveFileName.toString());
            if (Files.exists(fullFileName))
                throw new FileAlreadyExists();
            return fullFileName;
        }).retryWhen(RetrySpec.indefinitely());
    }

    public String uniqueFileName() {
        String ext = getExt(this.fileName);
        String baseName = FilenameUtils.getBaseName(this.fileName);
        Long salt = Math.abs(new Random().nextLong());
        StringBuilder sb;
        sb = new StringBuilder();
        sb.append(baseName);
        sb.append(".");
        sb.append(salt);
        sb.append(ext);
        return sb.toString();
    }

    public static String codedName(final String unqFileName) {
        String ext = getExt(unqFileName);
        return Math.abs(unqFileName.hashCode()) + ext;
    }

    public static Path buildSaveFileName(final String codedName) {
        Matcher matcher = pattern.matcher(codedName);
        if (!matcher.matches())
            return Path.of(codedName);
        String lvl1 = matcher.group(6) + matcher.group(5);
        String lvl2 = matcher.group(4) + matcher.group(3);
        String lvl3 = matcher.group(2);
        return Path.of(lvl1, lvl2, lvl3, codedName);
    }

    private static String getExt(final String fileName) {
        String ext = FilenameUtils.getExtension(fileName);
        if (!StringUtils.isEmpty(ext))
            ext = "." + ext;
        return ext;
    }
}
