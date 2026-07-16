package com.queuectl.service;

import com.queuectl.model.Config;
import com.queuectl.repository.ConfigRepository;
import org.springframework.stereotype.Service;

@Service
public class ConfigService {
    private final ConfigRepository configRepository;

    public ConfigService(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    public int getMaxRetries() {
        return configRepository.findById("max-retries")
                .map(c -> Integer.parseInt(c.getConfigValue()))
                .orElse(3);
    }

    public int getBackoffBase() {
        return configRepository.findById("backoff-base")
                .map(c -> Integer.parseInt(c.getConfigValue()))
                .orElse(2);
    }

    public String getConfig(String key) {
        return configRepository.findById(key)
                .map(Config::getConfigValue)
                .orElse(null);
    }

    public void setConfig(String key, String value) {
        configRepository.save(new Config(key, value));
    }
}
