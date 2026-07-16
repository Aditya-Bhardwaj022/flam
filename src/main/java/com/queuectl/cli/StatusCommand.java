package com.queuectl.cli;

import com.queuectl.model.JobState;
import com.queuectl.service.ConfigService;
import com.queuectl.service.JobService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(name = "status", description = "Show summary of all job states & active workers")
public class StatusCommand implements Runnable {

    private final JobService jobService;
    private final ConfigService configService;

    public StatusCommand(JobService jobService, ConfigService configService) {
        this.jobService = jobService;
        this.configService = configService;
    }

    @Override
    public void run() {
        System.out.println("System Status:");
        System.out.println("-------------------------");
        
        for (JobState state : JobState.values()) {
            long count = jobService.getJobsByState(state).size();
            System.out.printf("%-15s: %d%n", state.name(), count);
        }

        System.out.println("-------------------------");
        String workerStop = configService.getConfig("workers.stop");
        if ("false".equals(workerStop) || workerStop == null) {
            System.out.println("Workers State  : RUNNING (or ready to run)");
        } else {
            System.out.println("Workers State  : STOPPED");
        }
        System.out.println("Max Retries    : " + configService.getMaxRetries());
        System.out.println("Backoff Base   : " + configService.getBackoffBase());
    }
}
