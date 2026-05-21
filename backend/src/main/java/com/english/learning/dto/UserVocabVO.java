package com.english.learning.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVocabVO {
    private Long id;
    private Long vocabId;
    private String word;
    private String phonetic;
    private String translation;
    private String example;
    private String level;
    private Integer masteryLevel;
    private Integer reviewStage;
    private String status;
    private LocalDateTime nextReviewTime;
    private LocalDateTime lastReviewTime;
    private Integer reviewCount;
}
