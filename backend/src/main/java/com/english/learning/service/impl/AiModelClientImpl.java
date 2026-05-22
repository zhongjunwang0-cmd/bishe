package com.english.learning.service.impl;

import com.english.learning.dto.GrammarCorrectDto;
import com.english.learning.dto.KtRecommendDto;
import com.english.learning.dto.LearningEventDto;
import com.english.learning.dto.ModuleStatDto;
import com.english.learning.dto.PronunciationScoreDto;
import com.english.learning.service.AiModelClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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

    @Override
    public GrammarCorrectDto correctGrammar(String text) {
        if (!enabled || text == null || text.isBlank()) {
            return null;
        }
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("text", text.trim());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            String url = baseUrl.replaceAll("/$", "") + "/api/grammar/correct";
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return null;
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            GrammarCorrectDto dto = new GrammarCorrectDto();
            if (root.has("corrected")) {
                dto.setCorrected(root.get("corrected").asText());
            }
            if (root.has("source")) {
                dto.setSource(root.get("source").asText());
            }
            if (root.has("issues") && root.get("issues").isArray()) {
                List<Map<String, String>> issues = new ArrayList<>();
                root.get("issues").forEach(node -> {
                    Map<String, String> issue = new LinkedHashMap<>();
                    if (node.has("type")) {
                        issue.put("type", node.get("type").asText());
                    }
                    if (node.has("message")) {
                        issue.put("message", node.get("message").asText());
                    }
                    issues.add(issue);
                });
                dto.setIssues(issues);
            }
            return dto;
        } catch (RestClientException e) {
            log.warn("Grammar correction unreachable at {}: {}", baseUrl, e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Failed to parse grammar correction response: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public PronunciationScoreDto scorePronunciation(String referenceText, byte[] audioBytes, String filename) {
        if (!enabled || referenceText == null || referenceText.isBlank()
                || audioBytes == null || audioBytes.length == 0) {
            return null;
        }
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("reference_text", referenceText.trim());
            body.add("audio", new ByteArrayResource(audioBytes) {
                @Override
                public String getFilename() {
                    return filename != null && !filename.isBlank() ? filename : "recording.webm";
                }
            });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            String url = baseUrl.replaceAll("/$", "") + "/api/pronunciation/score";
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return null;
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            PronunciationScoreDto dto = new PronunciationScoreDto();
            if (root.has("score")) {
                dto.setScore(root.get("score").asInt());
            }
            if (root.has("wer")) {
                dto.setWer(root.get("wer").asDouble());
            }
            if (root.has("transcript")) {
                dto.setTranscript(root.get("transcript").asText());
            }
            if (root.has("feedback")) {
                dto.setFeedback(root.get("feedback").asText());
            }
            if (root.has("source")) {
                dto.setSource(root.get("source").asText());
            }
            if (root.has("calibrated")) {
                dto.setCalibrated(root.get("calibrated").asBoolean());
            }
            if (root.has("misread_words") && root.get("misread_words").isArray()) {
                List<String> misread = new ArrayList<>();
                root.get("misread_words").forEach(n -> misread.add(n.asText()));
                dto.setMisreadWords(misread);
            }
            if (root.has("suggestions") && root.get("suggestions").isArray()) {
                List<String> tips = new ArrayList<>();
                root.get("suggestions").forEach(n -> tips.add(n.asText()));
                dto.setSuggestions(tips);
            }
            return dto;
        } catch (RestClientException e) {
            log.warn("Pronunciation scoring unreachable at {}: {}", baseUrl, e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Failed to parse pronunciation score response: {}", e.getMessage());
            return null;
        }
    }
}
