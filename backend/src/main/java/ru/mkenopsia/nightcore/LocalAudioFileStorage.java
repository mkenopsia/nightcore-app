package ru.mkenopsia.nightcore;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Slf4j
@Service
public class LocalAudioFileStorage {

    private final Path tempDir;

    public LocalAudioFileStorage(@Value("${audio.processing.temp-dir:#{null}}") Path tempDir,
                                  AudioProcessingProperties props) {
        this.tempDir = tempDir != null ? tempDir : props.getTempDir();
    }

    // test-only constructor
    LocalAudioFileStorage(Path tempDir) {
        this.tempDir = tempDir;
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(tempDir);
        log.info("Temp directory initialized: {}", tempDir.toAbsolutePath());
    }

    public Path getTempDir() {
        return tempDir;
    }

    public Path saveFileLocally(MultipartFile file) throws IOException {
        String name = file.getOriginalFilename();
        if (name != null) {
            name = Paths.get(name).getFileName().toString();
        }
        Path dest = tempDir.resolve(name != null ? name : "upload.bin");
        file.transferTo(dest);
        return dest;
    }
}
