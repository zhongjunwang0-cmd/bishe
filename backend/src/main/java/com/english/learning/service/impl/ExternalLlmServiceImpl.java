package com.english.learning.service.impl;

import com.english.learning.dto.AiIntegrationConfig;
import com.english.learning.service.ExternalLlmService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ExternalLlmServiceImpl implements ExternalLlmService {

    private static final Logger log = LoggerFactory.getLogger(ExternalLlmServiceImpl.class);

    private static final String SYSTEM_PROMPT = """
            你是 Brix 英语学习系统的 AI 辅导老师，专门帮助中国学生提升英语能力。
            回答要求：
            1. 使用中文为主，必要时给出英文示例
            2. 回答具体、可操作，包含学习方法和建议
            3. 涵盖词汇、语法、阅读、听力、写作、口语、考试备考等场景
            4. 语气友好专业，适当使用 emoji
            5. 若问题与英语学习无关，礼貌引导回英语学习话题
            6. 当用户提供具体英文句子要求批改、纠错、修改或语法分析时，必须直接分析该句子，不要反问用户要更多背景
            7. 写作批改格式：分段输出，使用「原句」「修改后」「语法讲解」等小标题，不要使用 Markdown 星号加粗
            8. 当用户询问语法点时，给出结构、用法、例句和常见易错点
            """;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ExternalLlmServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<String> chat(AiIntegrationConfig config, String userMessage) {
        if (config == null || !config.isConfigured() || userMessage == null || userMessage.isBlank()) {
            return Optional.empty();
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.apiKey());

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", config.model());
            body.put("messages", List.of(
                    Map.of("role", "system", "content", SYSTEM_PROMPT),
                    Map.of("role", "user", "content", userMessage.trim())
            ));
            body.put("max_tokens", 1024);
            body.put("temperature", 0.7);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(config.endpoint(), entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("External LLM returned status {}", response.getStatusCode());
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
                log.warn("External LLM response missing content");
                return Optional.empty();
            }
            return Optional.of(contentNode.asText().trim());
        } catch (RestClientResponseException e) {
            log.warn("External LLM HTTP error {}: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("External LLM call failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public String testConnection(AiIntegrationConfig config) {
        Optional<String> reply = chat(config, "请仅回复：连接成功");
        if (reply.isPresent()) {
            return "成功";
        }
        return "失败：无法连接 AI 服务，请检查 Endpoint、Model 与 API Key";
    }
}
