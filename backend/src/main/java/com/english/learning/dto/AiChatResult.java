package com.english.learning.dto;

import lombok.Data;

@Data
public class AiChatResult {
    private String content;
    /** external | rule_engine | t5_gec */
    private String source;
}
