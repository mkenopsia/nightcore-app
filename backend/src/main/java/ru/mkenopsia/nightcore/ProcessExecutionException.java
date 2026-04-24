package ru.mkenopsia.nightcore;

import java.util.List;

public class ProcessExecutionException extends RuntimeException {
    private final List<String> command;

    public ProcessExecutionException(String message, List<String> command, Throwable cause) {
        super(message, cause);
        this.command = command;
    }

    public List<String> getCommand() { return command; }
}
