package com.english.learning.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public final class ScoreUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ScoreUtil() {
    }

    public static int calculateScore(String answersJson, List<String> userAnswers) {
        if (answersJson == null || answersJson.isBlank() || userAnswers == null || userAnswers.isEmpty()) {
            return 0;
        }
        try {
            JsonNode answers = MAPPER.readTree(answersJson);
            if (!answers.isArray() || answers.isEmpty()) {
                return 0;
            }
            int total = answers.size();
            int correct = 0;
            for (int i = 0; i < total; i++) {
                String expected = answers.get(i).get("correct").asText();
                if (i < userAnswers.size() && expected.equals(userAnswers.get(i))) {
                    correct++;
                }
            }
            return Math.round(correct * 100f / total);
        } catch (Exception e) {
            return 0;
        }
    }
}
