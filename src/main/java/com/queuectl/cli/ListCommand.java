package com.queuectl.cli;

import com.queuectl.model.Job;
import com.queuectl.model.JobState;
import com.queuectl.service.JobService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;

@Component
@Command(name = "list", description = "List jobs by state")
public class ListCommand implements Runnable {

    private final JobService jobService;

    @Option(names = {"--state"}, description = "Filter by state (pending, processing, completed, failed, dead)")
    private String state;

    public ListCommand(JobService jobService) {
        this.jobService = jobService;
    }

    @Override
    public void run() {
        List<Job> jobs;
        if (state != null) {
            try {
                JobState jobState = JobState.valueOf(state.toUpperCase());
                jobs = jobService.getJobsByState(jobState);
            } catch (IllegalArgumentException e) {
                throw new com.queuectl.exception.InvalidInputException("Invalid state: " + state, e);
            }
        } else {
            jobs = jobService.getAllJobs();
        }

        System.out.printf("%-15s %-20s %-15s %-10s %-25s%n", "ID", "COMMAND", "STATE", "ATTEMPTS", "UPDATED_AT");
        System.out.println("-----------------------------------------------------------------------------------------");
        for (Job job : jobs) {
            System.out.printf("%-15s %-20s %-15s %-10d %-25s%n", 
                job.getId(), job.getCommand(), job.getState(), job.getAttempts(), job.getUpdatedAt());
        }
    }
}
