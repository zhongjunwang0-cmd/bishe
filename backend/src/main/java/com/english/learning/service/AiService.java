package com.english.learning.service;

import com.english.learning.dto.AiChatResult;

public interface AiService {
    /**
     * Get personalized learning advice based on user's recent activity.
     */
    String getPersonalizedAdvice(Long userId);

    /**
     * Get AI response for a specific user query.
     * Tries external LLM when configured; falls back to the rule engine.
     */
    AiChatResult getChatResponse(Long userId, String query);
}
