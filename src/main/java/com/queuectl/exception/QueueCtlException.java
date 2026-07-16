package com.queuectl.exception;

public class QueueCtlException extends RuntimeException {
    private final int exitCode;

    public QueueCtlException(String message, int exitCode) {
        super(message);
        this.exitCode = exitCode;
    }

    public QueueCtlException(String message, Throwable cause, int exitCode) {
        super(message, cause);
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }
}
