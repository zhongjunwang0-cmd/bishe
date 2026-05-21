package com.english.learning.service;

public interface AiService {
    /**
     * Get personalized learning advice based on user's recent activity.
     */
    String getPersonalizedAdvice(Long userId);

    /**
     * Get AI response for a specific user query.
     */
    String getChatResponse(Long userId, String query);
}
