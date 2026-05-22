package com.english.learning.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PronunciationScoreDto {
    private int score;
    private double wer;
    private String transcript;
    private List<String> misreadWords = new ArrayList<>();
    private String feedback;
    private List<String> suggestions = new ArrayList<>();
    /** Whisper-WER */
    private String source;
    private boolean calibrated;
}
