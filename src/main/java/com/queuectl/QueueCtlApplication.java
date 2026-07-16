package com.queuectl;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;
import com.queuectl.cli.QueueCtlCommand;

@SpringBootApplication
public class QueueCtlApplication implements CommandLineRunner, ExitCodeGenerator {

    private final QueueCtlCommand queueCtlCommand;
    private final CommandLine.IFactory factory;
    private int exitCode;

    public QueueCtlApplication(QueueCtlCommand queueCtlCommand, CommandLine.IFactory factory) {
        this.queueCtlCommand = queueCtlCommand;
        this.factory = factory;
    }

    public static void main(String[] args) {
        org.springframework.context.ApplicationContext context = SpringApplication.run(QueueCtlApplication.class, args);
        if (args.length > 0) {
            System.exit(SpringApplication.exit(context));
        }
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0) {
            CommandLine cmd = new CommandLine(queueCtlCommand, factory);
            cmd.setExecutionExceptionHandler(new com.queuectl.cli.GlobalExceptionHandler());
            exitCode = cmd.execute(args);
        }
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
