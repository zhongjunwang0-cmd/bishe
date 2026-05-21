package com.english.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.english.learning.dto.AiIntegrationConfig;
import com.english.learning.entity.SysConfig;
import com.english.learning.mapper.SysConfigMapper;
import com.english.learning.service.SystemConfigService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SystemConfigService {

    private static final List<String> KNOWN_KEYS = List.of(
            "aiEnabled", "aiEndpoint", "aiModel", "aiApiKey",
            "voiceEnabled", "voiceProvider", "voiceToken"
    );

    private static final Map<String, String> DEFAULTS = Map.of(
            "aiEnabled", "false",
            "aiEndpoint", "https://api.deepseek.com/v1/chat/completions",
            "aiModel", "deepseek-chat",
            "aiApiKey", "",
            "voiceEnabled", "false",
            "voiceProvider", "azure",
            "voiceToken", ""
    );

    @Override
    public Map<String, Object> getConfigForAdmin() {
        Map<String, Object> config = loadRawConfig();
        maskSecret(config, "aiApiKey");
        maskSecret(config, "voiceToken");
        return config;
    }

    @Override
    public void saveConfig(Map<String, Object> incoming) {
        if (incoming == null || incoming.isEmpty()) {
            return;
        }
        Map<String, Object> toSave = new HashMap<>(incoming);
        if (isMasked(toSave.get("aiApiKey"))) {
            toSave.remove("aiApiKey");
        }
        if (isMasked(toSave.get("voiceToken"))) {
            toSave.remove("voiceToken");
        }
        for (Map.Entry<String, Object> entry : toSave.entrySet()) {
            if (!KNOWN_KEYS.contains(entry.getKey())) {
                continue;
            }
            saveKey(entry.getKey(), stringify(entry.getValue()));
        }
    }

    @Override
    public AiIntegrationConfig getAiIntegrationConfig() {
        Map<String, Object> config = loadRawConfig();
        return new AiIntegrationConfig(
                parseBoolean(config.get("aiEnabled"), false),
                stringValue(config.get("aiEndpoint"), DEFAULTS.get("aiEndpoint")),
                stringValue(config.get("aiApiKey"), ""),
                stringValue(config.get("aiModel"), DEFAULTS.get("aiModel"))
        );
    }

    @Override
    public String testAiConnection() {
        AiIntegrationConfig config = getAiIntegrationConfig();
        if (!config.isConfigured()) {
            return "未启用或未配置 API Key";
        }
        return externalLlmService.testConnection(config);
    }

    private final com.english.learning.service.ExternalLlmService externalLlmService;

    public SystemConfigServiceImpl(com.english.learning.service.ExternalLlmService externalLlmService) {
        this.externalLlmService = externalLlmService;
    }

    private Map<String, Object> loadRawConfig() {
        Map<String, Object> config = new HashMap<>();
        DEFAULTS.forEach(config::put);
        this.list().forEach(item -> config.put(item.getConfigKey(), item.getConfigValue()));
        config.put("aiEnabled", parseBoolean(config.get("aiEnabled"), false));
        config.put("voiceEnabled", parseBoolean(config.get("voiceEnabled"), false));
        return config;
    }

    private void saveKey(String key, String value) {
        SysConfig config = new SysConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        this.saveOrUpdate(config);
    }

    private static String stringify(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Boolean bool) {
            return bool.toString();
        }
        return String.valueOf(value);
    }

    private static boolean parseBoolean(Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        return "true".equalsIgnoreCase(String.valueOf(value));
    }

    private static String stringValue(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String text = String.valueOf(value);
        return text.isBlank() ? defaultValue : text;
    }

    private static void maskSecret(Map<String, Object> config, String key) {
        Object value = config.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            config.put(key, "");
            return;
        }
        String text = String.valueOf(value);
        if (text.length() <= 4) {
            config.put(key, "****");
        } else {
            config.put(key, "****" + text.substring(text.length() - 4));
        }
    }

    static boolean isMasked(Object value) {
        if (value == null) {
            return true;
        }
        String text = String.valueOf(value);
        return text.isBlank() || text.startsWith("****");
    }
}
