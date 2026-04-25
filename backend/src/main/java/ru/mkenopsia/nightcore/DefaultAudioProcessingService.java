package ru.mkenopsia.nightcore;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultAudioProcessingService implements AudioProcessingService {

    private final ProcessExecutor processExecutor;
    private final AudioProcessingProperties props;

    @Override
    public ProcessingResponse processAudio(Path inputPath, Preset preset) {
        Path tempWav = null;
        try {
            Path wavSource = isWav(inputPath) ? inputPath : convertToWav(inputPath);

            tempWav = createTempPath("processed", ".wav");
            executeSoundStretch(wavSource, tempWav, preset);

            Path outputMp3 = convertToMp3(tempWav);

            log.info("Обработка завершена: {} -> {}", inputPath.getFileName(), outputMp3.getFileName());
            return new ProcessingResponse("OK", outputMp3.getFileName().toString());
        } catch (ProcessExecutionException e) {
            log.error("Обработка закончилась ошибкой {}: {}", inputPath.getFileName(), e.getMessage());
            throw new AudioProcessingException("Не удалось обработать аудио: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("IO ошибка {}: {}", inputPath.getFileName(), e.getMessage());
            throw new AudioProcessingException("File system error: " + e.getMessage(), e);
        } finally {
            cleanupTempFiles(inputPath, tempWav);
        }
    }

    private Path convertToWav(Path input) throws ProcessExecutionException, IOException {
        Path output = createTempPath(PathUtils.getFilenameWithoutExtension(input), ".wav");

        var command = List.of(
                "ffmpeg", "-i", input.toString(),
                "-f", "wav",
                "-y",
                output.toString()
        );

        log.debug("Converting to WAV: {} → {}", input.getFileName(), output.getFileName());
        processExecutor.execute(command);
        return output;
    }

    private void executeSoundStretch(Path input, Path output, Preset preset) throws ProcessExecutionException {
        var command = List.of(
                "soundstretch",
                input.toString(),
                output.toString(),
                "-tempo=" + preset.tempo(),
                "-pitch=" + preset.pitch()
        );

        log.debug("Обработка soundstretch: tempo={}%, pitch={}",
                preset.tempo(), preset.pitch());
        processExecutor.execute(command);
    }

    private Path convertToMp3(Path inputWav) throws ProcessExecutionException, IOException {
        Path output = createTempPath(PathUtils.getFilenameWithoutExtension(inputWav), ".mp3");

        var command = List.of(
                "lame",
                "-b", String.valueOf(props.getMp3Bitrate()),
                "--quiet",
                inputWav.toString(),
                output.toString()
        );

        log.debug("Кодирование в MP3: {} -> {}@{}kbps",
                inputWav.getFileName(), output.getFileName(), props.getMp3Bitrate());
        processExecutor.execute(command);
        return output;
    }

    private boolean isWav(Path path) {
        String ext = StringUtils.getFilenameExtension(path.toString());
        return "wav".equalsIgnoreCase(ext);
    }

    private Path createTempPath(String prefix, String extension) throws IOException {
        return Files.createTempFile(props.getTempDir(), prefix + "_", extension);
    }

    private void cleanupTempFiles(Path... paths) {
        for (Path path : paths) {
            if (path != null && !path.equals(props.getTempDir())) {
                try {
                    Files.deleteIfExists(path);
                    log.trace("Удалён файл: {}", path.getFileName());
                } catch (IOException e) {
                    log.warn("Не удалось удалить файл {}: {}", path.getFileName(), e.getMessage());
                }
            }
        }
    }
}

