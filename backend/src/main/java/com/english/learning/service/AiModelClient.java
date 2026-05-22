package com.english.learning.service;

import com.english.learning.dto.GrammarCorrectDto;
import com.english.learning.dto.KtRecommendDto;
import com.english.learning.dto.LearningEventDto;
import com.english.learning.dto.ModuleStatDto;
import com.english.learning.dto.PronunciationScoreDto;

import java.util.List;

public interface AiModelClient {
    /**
     * Call Python ai-service /api/kt/recommend (DKT LSTM). Empty if service unavailable.
     */
    KtRecommendDto recommend(List<ModuleStatDto> moduleStats, List<LearningEventDto> events, String userId);

    /**
     * Call Python ai-service /api/grammar/correct (T5-GEC). Null if service unavailable.
     */
    GrammarCorrectDto correctGrammar(String text);

    /**
     * Call Python ai-service /api/pronunciation/score (Whisper+WER). Null if service unavailable.
     */
    PronunciationScoreDto scorePronunciation(String referenceText, byte[] audioBytes, String filename);
}
