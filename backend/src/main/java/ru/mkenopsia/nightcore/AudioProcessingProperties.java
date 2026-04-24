package ru.mkenopsia.nightcore;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "audio.processing")
@Data
public class AudioProcessingProperties {

    private Duration timeout = Duration.ofSeconds(60);
    private int mp3Bitrate = 192;
    private Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "audio-uploads");

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(tempDir);
    }
}
