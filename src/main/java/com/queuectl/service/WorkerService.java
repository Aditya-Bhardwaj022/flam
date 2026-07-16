package com.queuectl.service;

import com.queuectl.model.Job;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class WorkerService {
    private final JobService jobService;
    private final ConfigService configService;
    private ExecutorService executorService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public WorkerService(JobService jobService, ConfigService configService) {
        this.jobService = jobService;
        this.configService = configService;
    }

    public void startWorkers(int count) {
        if (running.get()) {
            System.out.println("Workers are already running in this process.");
            return;
        }

        // Clear stop signal
        configService.setConfig("workers.stop", "false");

        running.set(true);
        executorService = Executors.newFixedThreadPool(count);
        System.out.println("Starting " + count + " workers...");

        for (int i = 0; i < count; i++) {
            int workerId = i + 1;
            executorService.submit(() -> runWorker(workerId));
        }

        Runtime.getRuntime().addShutdownHook(new Thread(this::stopWorkers));

        while (running.get()) {
            try {
                if ("true".equals(configService.getConfig("workers.stop"))) {
                    System.out.println("Received stop signal via config.");
                    stopWorkers();
                    break;
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                stopWorkers();
            }
        }
    }

    public void stopWorkers() {
        if (!running.get()) return;
        
        System.out.println("Stopping workers gracefully...");
        running.set(false);
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Workers stopped.");
    }
    
    public void sendStopSignal() {
        configService.setConfig("workers.stop", "true");
        System.out.println("Stop signal sent to workers.");
    }

    private void runWorker(int workerId) {
        System.out.println("Worker " + workerId + " started.");
        while (running.get()) {
            try {
                jobService.requeueReadyFailedJobs();

                Optional<Job> optionalJob = jobService.claimNextJob();
                if (optionalJob.isPresent()) {
                    Job job = optionalJob.get();
                    System.out.println("Worker " + workerId + " processing job: " + job.getId());
                    boolean success = executeCommand(job.getCommand());
                    if (success) {
                        System.out.println("Worker " + workerId + " completed job: " + job.getId());
                        jobService.markCompleted(job.getId());
                    } else {
                        System.out.println("Worker " + workerId + " failed job: " + job.getId());
                        jobService.markFailed(job.getId());
                    }
                } else {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Worker " + workerId + " encountered error: " + e.getMessage());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        System.out.println("Worker " + workerId + " shutting down.");
    }

    private boolean executeCommand(String commandString) {
        try {
            boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
            ProcessBuilder pb;
            if (isWindows) {
                pb = new ProcessBuilder("cmd.exe", "/c", commandString);
            } else {
                pb = new ProcessBuilder("sh", "-c", commandString);
            }
            
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // System.out.println("  [Out] " + line);
                }
            }
            
            int exitCode = process.waitFor();
            return exitCode == 0;
            
        } catch (Exception e) {
            System.err.println("Failed to execute command: " + commandString);
            return false;
        }
    }
}
