package com.queuectl.controller;

import com.queuectl.service.ConfigService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    public static class ConfigRequest {
        public String key;
        public String value;
    }

    @PostMapping
    public void setConfig(@RequestBody ConfigRequest request) {
        if (request.key == null || request.value == null) {
            throw new com.queuectl.exception.InvalidInputException("Missing 'key' or 'value' in request body.");
        }
        configService.setConfig(request.key, request.value);
    }
}
