package ru.mkenopsia.nightcore;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@ConfigurationProperties(prefix = "audio.processing")
public class AudioProcessingProperties {

    private final Duration timeout;
    private final int mp3Bitrate;
    private final Path tempDir;

    public AudioProcessingProperties(
            @DefaultValue("60s") Duration timeout,
            @DefaultValue("192") int mp3Bitrate,
            @DefaultValue("${java.io.tmpdir}/audio-uploads") Path tempDir
    ) {
        this.timeout = timeout;
        this.mp3Bitrate = mp3Bitrate;
        this.tempDir = tempDir;
    }

    public Duration getTimeout() { return timeout; }
    public int getMp3Bitrate() { return mp3Bitrate; }
    public Path getTempDir() { return tempDir; }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(tempDir);
    }
}
