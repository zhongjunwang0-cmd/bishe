package com.english.learning.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class KtRecommendDto {
    private List<String> weakModules = new ArrayList<>();
    private List<String> todayTasks = new ArrayList<>();
    private double mastery;
    private String advice;
    /** ml_model | rule_fallback */
    private String source;
}
