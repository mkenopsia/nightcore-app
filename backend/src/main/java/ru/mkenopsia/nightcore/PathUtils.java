package ru.mkenopsia.nightcore;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class PathUtils {

    public static String getFilenameWithoutExtension(Path path) {
        String filename = path.getFileName().toString();
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(0, lastDot) : filename;
    }

    public static Path createTempFile(Path directory, String prefix, String extension) throws IOException {
        return Files.createTempFile(directory, prefix + "_", extension);
    }
}
