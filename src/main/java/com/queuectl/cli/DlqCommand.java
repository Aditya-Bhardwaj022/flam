package com.queuectl.cli;

import com.queuectl.model.Job;
import com.queuectl.model.JobState;
import com.queuectl.service.JobService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;

@Component
@Command(name = "dlq", description = "Manage Dead Letter Queue",
        subcommands = {
                DlqCommand.ListDlqCommand.class,
                DlqCommand.RetryDlqCommand.class
        })
public class DlqCommand {

    @Component
    @Command(name = "list", description = "View DLQ jobs")
    public static class ListDlqCommand implements Runnable {
        private final JobService jobService;

        public ListDlqCommand(JobService jobService) {
            this.jobService = jobService;
        }

        @Override
        public void run() {
            List<Job> jobs = jobService.getJobsByState(JobState.DEAD);
            System.out.printf("%-15s %-20s %-25s%n", "ID", "COMMAND", "DEAD_SINCE");
            System.out.println("----------------------------------------------------------------");
            for (Job job : jobs) {
                System.out.printf("%-15s %-20s %-25s%n", job.getId(), job.getCommand(), job.getUpdatedAt());
            }
        }
    }

    @Component
    @Command(name = "retry", description = "Retry a job from DLQ")
    public static class RetryDlqCommand implements Runnable {
        private final JobService jobService;

        @Parameters(index = "0", description = "ID of the job to retry")
        private String jobId;

        public RetryDlqCommand(JobService jobService) {
            this.jobService = jobService;
        }

        @Override
        public void run() {
            jobService.retryDeadJob(jobId);
            System.out.println("Job " + jobId + " moved back to pending queue.");
        }
    }
}
