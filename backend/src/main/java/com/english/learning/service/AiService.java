package com.english.learning.service;

import com.english.learning.dto.AiChatResult;
import com.english.learning.dto.KtRecommendDto;

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

    /**
     * Learning recommendation from EdNet-trained model (via ai-service) with rule fallback.
     */
    KtRecommendDto getLearningRecommend(Long userId);
}
