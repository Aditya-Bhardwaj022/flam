package com.queuectl.controller;

import com.queuectl.model.Job;
import com.queuectl.model.JobState;
import com.queuectl.service.JobService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    public static class EnqueueRequest {
        public String id;
        public String command;
    }

    @PostMapping
    public Job enqueueJob(@RequestBody EnqueueRequest request) {
        if (request.id == null || request.command == null) {
            throw new com.queuectl.exception.InvalidInputException("Missing 'id' or 'command' in request body.");
        }
        return jobService.enqueue(request.id, request.command);
    }

    @GetMapping
    public List<Job> listJobs(@RequestParam(required = false) String state) {
        if (state != null) {
            try {
                JobState jobState = JobState.valueOf(state.toUpperCase());
                return jobService.getJobsByState(jobState);
            } catch (IllegalArgumentException e) {
                throw new com.queuectl.exception.InvalidInputException("Invalid state: " + state, e);
            }
        }
        return jobService.getAllJobs();
    }

    @PostMapping("/{id}/retry")
    public void retryJob(@PathVariable String id) {
        jobService.retryDeadJob(id);
    }
}
