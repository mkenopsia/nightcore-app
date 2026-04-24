package ru.mkenopsia.nightcore;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class ProcessExecutor {

    private final Duration defaultTimeout;
    private final Path tempDir;

    public ProcessExecutor(AudioProcessingProperties props) {
        this.defaultTimeout = props.getTimeout();
        this.tempDir = props.getTempDir();
    }

    /**
     * Запускает команду и ждёт завершения
     *
     * @throws ProcessExecutionException если процесс упал или таймаут
     */
    public void execute(List<String> command) throws ProcessExecutionException {
        execute(command, defaultTimeout);
    }

    public void execute(List<String> command, Duration timeout) throws ProcessExecutionException {
        log.debug("Executing: {}", String.join(" ", command));

        var pb = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .directory(tempDir.toFile());

        try {
            Process process = pb.start();

            var outputFuture = CompletableFuture.supplyAsync(() -> {
                try (var reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.trace("[{}] {}", command.getFirst(), line);
                        sb.append(line).append("\n");
                    }
                    return sb.toString();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });

            boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new ProcessExecutionException(
                        "Process timed out after %ds: %s".formatted(timeout.getSeconds(), command),
                        command,
                        null);
            }

            outputFuture.get(5, TimeUnit.SECONDS);

            if (process.exitValue() != 0) {
                throw new ProcessExecutionException(
                        "Process failed with exit code %d: %s".formatted(process.exitValue(), command),
                        command,
                        null);
            }

        } catch (IOException e) {
            throw new ProcessExecutionException("Failed to start process: " + command.getFirst(), command, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProcessExecutionException("Process interrupted: " + command.getFirst(), command, e);
        } catch (ExecutionException | TimeoutException e) {
            throw new ProcessExecutionException("Failed to read process output: " + command.getFirst(), command, e);
        }
    }
}

