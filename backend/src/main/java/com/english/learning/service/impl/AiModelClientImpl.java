package com.english.learning.service.impl;

import com.english.learning.dto.KtRecommendDto;
import com.english.learning.dto.LearningEventDto;
import com.english.learning.dto.ModuleStatDto;
import com.english.learning.service.AiModelClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiModelClientImpl implements AiModelClient {

    private static final Logger log = LoggerFactory.getLogger(AiModelClientImpl.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.service.enabled:true}")
    private boolean enabled;

    @Value("${ai.service.base-url:http://localhost:8000}")
    private String baseUrl;

    public AiModelClientImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public KtRecommendDto recommend(List<ModuleStatDto> moduleStats, List<LearningEventDto> events, String userId) {
        if (!enabled || moduleStats == null || moduleStats.isEmpty()) {
            return null;
        }
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("user_id", userId);
            body.put("module_stats", moduleStats);
            body.put("events", events != null ? events : List.of());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            String url = baseUrl.replaceAll("/$", "") + "/api/kt/recommend";
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return null;
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            KtRecommendDto dto = new KtRecommendDto();
            if (root.has("model_type") && "LSTM-DKT".equals(root.get("model_type").asText())) {
                dto.setSource("dkt_model");
            } else {
                dto.setSource("ml_model");
            }

            if (root.has("mastery")) {
                dto.setMastery(root.get("mastery").asDouble());
            }
            if (root.has("weak_modules") && root.get("weak_modules").isArray()) {
                List<String> weak = new ArrayList<>();
                root.get("weak_modules").forEach(n -> weak.add(n.asText()));
                dto.setWeakModules(weak);
            }
            if (root.has("today_tasks") && root.get("today_tasks").isArray()) {
                List<String> tasks = new ArrayList<>();
                root.get("today_tasks").forEach(n -> tasks.add(n.asText()));
                dto.setTodayTasks(tasks);
            }
            return dto;
        } catch (RestClientException e) {
            log.warn("AI service unreachable at {}: {}", baseUrl, e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Failed to parse AI recommend response: {}", e.getMessage());
            return null;
        }
    }
}
