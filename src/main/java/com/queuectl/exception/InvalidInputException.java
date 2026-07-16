package com.queuectl.exception;

public class InvalidInputException extends QueueCtlException {
    public InvalidInputException(String message) {
        super(message, 1);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause, 1);
    }
}
