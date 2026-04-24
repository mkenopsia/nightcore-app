package ru.mkenopsia.nightcore;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class LocalAudioFileStorage {

    public static Path TEMP_DIR = Paths.get("tmp");

    public LocalAudioFileStorage() {
        TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "audio-uploads");
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(TEMP_DIR);
        log.info("Temp directory initialized: {}", TEMP_DIR.toAbsolutePath());
    }

    public Path saveFileLocally(MultipartFile file) throws IOException {
        Path dest = TEMP_DIR.resolve(file.getOriginalFilename());
        file.transferTo(dest);
        return dest;
    }
}
