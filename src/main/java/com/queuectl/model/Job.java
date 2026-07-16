package com.queuectl.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(name = "command", nullable = false)
    private String command;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private JobState state;

    @Column(name = "attempts")
    private int attempts = 0;

    @Column(name = "max_retries")
    private int maxRetries = 3;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
        if (state == null) {
            state = JobState.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }
    public JobState getState() { return state; }
    public void setState(JobState state) { this.state = state; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
