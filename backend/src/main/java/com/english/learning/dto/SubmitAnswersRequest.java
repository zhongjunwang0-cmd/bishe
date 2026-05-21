package com.english.learning.dto;

import lombok.Data;

import java.util.List;

@Data
public class SubmitAnswersRequest {
    private List<String> answers;
}
