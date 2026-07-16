package com.queuectl.cli;

import com.queuectl.service.ConfigService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Component
@Command(name = "config", description = "Manage configuration",
        subcommands = {
                ConfigCommand.SetCommand.class
        })
public class ConfigCommand {

    @Component
    @Command(name = "set", description = "Set a configuration property")
    public static class SetCommand implements Runnable {
        private final ConfigService configService;

        @Parameters(index = "0", description = "Configuration key (e.g. max-retries)")
        private String key;

        @Parameters(index = "1", description = "Configuration value")
        private String value;

        public SetCommand(ConfigService configService) {
            this.configService = configService;
        }

        @Override
        public void run() {
            configService.setConfig(key, value);
            System.out.println("Configuration updated: " + key + " = " + value);
        }
    }
}
