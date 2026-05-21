package com.english.learning.service;

import com.english.learning.dto.AiIntegrationConfig;

import java.util.Optional;

public interface ExternalLlmService {
    Optional<String> chat(AiIntegrationConfig config, String userMessage);

    String testConnection(AiIntegrationConfig config);
}
