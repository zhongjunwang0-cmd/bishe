package com.english.learning.service;

import com.english.learning.dto.AiIntegrationConfig;

import java.util.Map;

public interface SystemConfigService {
    Map<String, Object> getConfigForAdmin();

    void saveConfig(Map<String, Object> config);

    AiIntegrationConfig getAiIntegrationConfig();

    String testAiConnection();
}
