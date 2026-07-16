package com.queuectl.model;

public enum JobState {
    PENDING("pending"),
    PROCESSING("processing"),
    COMPLETED("completed"),
    FAILED("failed"),
    DEAD("dead");

    private final String value;

    JobState(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
