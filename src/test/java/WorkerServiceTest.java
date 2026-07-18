package com.queuectl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class WorkerServiceTest {

    private JobService jobService;

    private ConfigService configService;

    private WorkerService workerService;

    @BeforeEach
    void setup() {

        jobService = mock(JobService.class);

        configService = mock(ConfigService.class);

        workerService = new WorkerService(jobService, configService);
    }

    @Test
    void sendStopSignalUpdatesConfiguration() {

        workerService.sendStopSignal();

        verify(configService)
                .setConfig("workers.stop", "true");
    }

    @Test
    void startWorkersInitializesStopFlag() {

        Thread t = new Thread(() -> workerService.startWorkers(1));

        t.start();

        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }

        workerService.stopWorkers();

        verify(configService)
                .setConfig("workers.stop", "false");
    }
}