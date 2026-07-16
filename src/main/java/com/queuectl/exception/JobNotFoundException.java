package com.queuectl.exception;

public class JobNotFoundException extends QueueCtlException {
    public JobNotFoundException(String message) {
        super(message, 1);
    }

    public JobNotFoundException(String message, Throwable cause) {
        super(message, cause, 1);
    }
}
