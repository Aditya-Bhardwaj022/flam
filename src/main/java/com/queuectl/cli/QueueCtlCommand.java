package com.queuectl.cli;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

@Component
@Command(name = "queuectl", mixinStandardHelpOptions = true, version = "1.0",
        description = "Background job queue system",
        subcommands = {
                EnqueueCommand.class,
                WorkerCommand.class,
                StatusCommand.class,
                ListCommand.class,
                DlqCommand.class,
                ConfigCommand.class,
                HelpCommand.class
        })
public class QueueCtlCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Use 'queuectl --help' to see available commands.");
    }
}
