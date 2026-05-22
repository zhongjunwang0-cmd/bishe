package com.english.learning.service;

import com.english.learning.dto.KtRecommendDto;
import com.english.learning.dto.LearningEventDto;
import com.english.learning.dto.ModuleStatDto;

import java.util.List;

public interface AiModelClient {
    /**
     * Call Python ai-service /api/kt/recommend (DKT LSTM). Empty if service unavailable.
     */
    KtRecommendDto recommend(List<ModuleStatDto> moduleStats, List<LearningEventDto> events, String userId);
}
