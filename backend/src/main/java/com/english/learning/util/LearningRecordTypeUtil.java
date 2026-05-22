package com.english.learning.util;

import java.util.Map;

public final class LearningRecordTypeUtil {

    private static final Map<String, String> TYPE_ALIASES = Map.of(
            "阅读理解", "READING",
            "听力训练", "LISTENING",
            "选词填空", "CLOZE",
            "口语练习", "ORAL",
            "词汇学习", "VOCAB",
            "词汇通关", "VOCAB",
            "语法解析", "GRAMMAR",
            "文献阅读", "LIT"
    );

    private static final Map<String, String> TYPE_ZH = Map.of(
            "VOCAB", "词汇学习",
            "GRAMMAR", "语法解析",
            "LIT", "文献阅读",
            "READING", "阅读理解",
            "LISTENING", "听力训练",
            "CLOZE", "选词填空",
            "ORAL", "口语练习"
    );

    private LearningRecordTypeUtil() {
    }

    public static String normalize(String type) {
        if (type == null || type.isBlank()) {
            return type;
        }
        String trimmed = type.trim();
        return TYPE_ALIASES.getOrDefault(trimmed, trimmed.toUpperCase());
    }

    public static String toDisplayName(String type) {
        String normalized = normalize(type);
        return TYPE_ZH.getOrDefault(normalized, normalized);
    }

    /** Map record type to KT model module (VOCAB/GRAMMAR/READING/LISTENING/ORAL). */
    public static String toKtModule(String type) {
        String normalized = normalize(type);
        return switch (normalized) {
            case "VOCAB", "GRAMMAR", "READING", "LISTENING", "ORAL" -> normalized;
            case "CLOZE", "LIT" -> "READING";
            default -> null;
        };
    }
}
