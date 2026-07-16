package com.queuectl.cli;

import com.queuectl.service.WorkerService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(name = "worker", description = "Manage workers",
        subcommands = {
                WorkerCommand.StartCommand.class,
                WorkerCommand.StopCommand.class
        })
public class WorkerCommand {

    @Component
    @Command(name = "start", description = "Start one or more workers")
    public static class StartCommand implements Runnable {
        private final WorkerService workerService;

        @Option(names = {"--count"}, description = "Number of workers to start", defaultValue = "1")
        private int count;

        public StartCommand(WorkerService workerService) {
            this.workerService = workerService;
        }

        @Override
        public void run() {
            workerService.startWorkers(count);
        }
    }

    @Component
    @Command(name = "stop", description = "Stop running workers gracefully")
    public static class StopCommand implements Runnable {
        private final WorkerService workerService;

        public StopCommand(WorkerService workerService) {
            this.workerService = workerService;
        }

        @Override
        public void run() {
            workerService.sendStopSignal();
        }
    }
}
