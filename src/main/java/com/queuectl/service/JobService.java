package com.queuectl.service;

import com.queuectl.model.Job;
import com.queuectl.model.JobState;
import com.queuectl.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class JobService {
    private final JobRepository jobRepository;
    private final ConfigService configService;

    public JobService(JobRepository jobRepository, ConfigService configService) {
        this.jobRepository = jobRepository;
        this.configService = configService;
    }

    @Transactional
    public Job enqueue(String id, String command) {
        Job job = new Job();
        job.setId(id);
        job.setCommand(command);
        job.setState(JobState.PENDING);
        job.setMaxRetries(configService.getMaxRetries());
        return jobRepository.save(job);
    }

    public List<Job> getJobsByState(JobState state) {
        return jobRepository.findByState(state);
    }
    
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    @Transactional
    public Optional<Job> claimNextJob() {
        Optional<Job> job = jobRepository.findFirstByStateOrderByCreatedAtAsc(JobState.PENDING);
        if (job.isPresent()) {
            Job claimed = job.get();
            claimed.setState(JobState.PROCESSING);
            return Optional.of(jobRepository.save(claimed));
        }
        return Optional.empty();
    }

    @Transactional
    public void markCompleted(String id) {
        jobRepository.findById(id).ifPresent(job -> {
            job.setState(JobState.COMPLETED);
            jobRepository.save(job);
        });
    }

    @Transactional
    public void markFailed(String id) {
        jobRepository.findById(id).ifPresent(job -> {
            job.setAttempts(job.getAttempts() + 1);
            if (job.getAttempts() >= job.getMaxRetries()) {
                job.setState(JobState.DEAD);
            } else {
                job.setState(JobState.FAILED);
            }
            jobRepository.save(job);
        });
    }

    @Transactional
    public void requeueReadyFailedJobs() {
        List<Job> failedJobs = jobRepository.findByState(JobState.FAILED);
        int base = configService.getBackoffBase();
        
        for (Job job : failedJobs) {
            long delaySeconds = (long) Math.pow(base, job.getAttempts());
            if (job.getUpdatedAt().plusSeconds(delaySeconds).isBefore(Instant.now())) {
                job.setState(JobState.PENDING);
                jobRepository.save(job);
            }
        }
    }

    @Transactional
    public void retryDeadJob(String id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new com.queuectl.exception.JobNotFoundException("Job not found: " + id));
        
        if (job.getState() == JobState.DEAD) {
            job.setState(JobState.PENDING);
            job.setAttempts(0); // Reset attempts
            jobRepository.save(job);
        } else {
            throw new com.queuectl.exception.InvalidInputException("Job " + id + " is not DEAD (current state: " + job.getState() + ")");
        }
    }
}
