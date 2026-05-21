package com.english.learning.dto;

public record AiIntegrationConfig(
        boolean enabled,
        String endpoint,
        String apiKey,
        String model
) {
    public boolean isConfigured() {
        return enabled
                && endpoint != null && !endpoint.isBlank()
                && apiKey != null && !apiKey.isBlank()
                && model != null && !model.isBlank();
    }
}
