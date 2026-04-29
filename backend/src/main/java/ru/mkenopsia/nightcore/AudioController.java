package ru.mkenopsia.nightcore;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
public class AudioController {

    private final AudioProcessingService audioProcessingService;
    private final LocalAudioFileStorage fileStorage;
    private final AudioProcessingProperties props;

    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProcessingResponse> processAudio(
            @RequestPart("file") MultipartFile file,
            @RequestParam("tempo") Double tempo,
            @RequestParam("pitch") Double pitch
    ) {
        try {
            Path inputPath = fileStorage.saveFileLocally(file);

            if (tempo == null || pitch == null) {
                return ResponseEntity.badRequest()
                        .body(new ProcessingResponse("ERROR", "Missing tempo or pitch parameter"));
            }

            Preset preset = new Preset(tempo, pitch);
            ProcessingResponse response = audioProcessingService.processAudio(inputPath, preset);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ProcessingResponse("ERROR", e.getMessage()));
        } catch (AudioProcessingException e) {
            log.error("Processing failed: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new ProcessingResponse("ERROR", "Processing failed: " + e.getMessage()));
        } catch (IOException e) {
            log.error("File operation failed", e);
            return ResponseEntity.status(500)
                    .body(new ProcessingResponse("ERROR", "File system error"));
        }
    }

    @GetMapping("/download/{path}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String path) {

        Path filePath = Path.of(props.getTempDir().toString(), path);
        if (!Files.exists(filePath)) {
            return ResponseEntity.status(410).body(null);
        }

        Resource resource = new FileSystemResource(filePath);

        String originalName = filePath.getFileName().toString();
        ContentDisposition disposition = ContentDisposition.builder("attachment")
                .filename(originalName, StandardCharsets.UTF_8)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(disposition);
        headers.setContentType(MediaType.parseMediaType(getMimeType(filePath)));
        try {
            headers.setContentLength(Files.size(filePath));
        } catch (IOException e) {
        }
        headers.setCacheControl("no-cache, no-store, must-revalidate");

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    private String getMimeType(Path path) {
        try {
            String type = Files.probeContentType(path);
            return type != null ? type : "application/octet-stream";
        } catch (IOException e) {
            return "audio/mpeg";
        }
    }
}
