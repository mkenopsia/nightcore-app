package ru.mkenopsia.nightcore;

import java.util.Collections;
import java.util.List;

public class ProcessExecutionException extends RuntimeException {
    private final List<String> command;

    public ProcessExecutionException(String message, List<String> command, Throwable cause) {
        super(message, cause);
        this.command = command != null ? List.copyOf(command) : List.of();
    }

    public List<String> getCommand() {
        return Collections.unmodifiableList(command);
    }
}
