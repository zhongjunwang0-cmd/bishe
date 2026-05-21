package com.english.learning.dto;

import lombok.Data;

@Data
public class VocabReviewSubmitRequest {
    private Long vocabId;
    private Boolean remembered;
}
