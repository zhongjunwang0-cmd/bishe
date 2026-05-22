package com.english.learning.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class GrammarCorrectDto {
    private String corrected;
    private List<Map<String, String>> issues = new ArrayList<>();
    /** T5-GEC | T5-base | rule_fallback */
    private String source;
}
