package com.queuectl.controller;

import com.queuectl.model.JobState;
import com.queuectl.service.ConfigService;
import com.queuectl.service.JobService;
import com.queuectl.service.WorkerService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class WorkerController {

    private final WorkerService workerService;
    private final JobService jobService;
    private final ConfigService configService;

    public WorkerController(WorkerService workerService, JobService jobService, ConfigService configService) {
        this.workerService = workerService;
        this.jobService = jobService;
        this.configService = configService;
    }

    @PostMapping("/workers/start")
    public void startWorkers(@RequestParam(defaultValue = "1") int count) {
        workerService.startWorkers(count);
    }

    @PostMapping("/workers/stop")
    public void stopWorkers() {
        workerService.sendStopSignal();
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        
        Map<String, Long> jobCounts = new HashMap<>();
        for (JobState state : JobState.values()) {
            jobCounts.put(state.name(), (long) jobService.getJobsByState(state).size());
        }
        status.put("jobs", jobCounts);
        
        String workerStop = configService.getConfig("workers.stop");
        status.put("workersRunning", "false".equals(workerStop) || workerStop == null);
        status.put("maxRetries", configService.getMaxRetries());
        status.put("backoffBase", configService.getBackoffBase());
        
        return status;
    }
}
