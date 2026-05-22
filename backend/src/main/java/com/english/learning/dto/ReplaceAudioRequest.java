package com.english.learning.dto;

import lombok.Data;

@Data
public class ReplaceAudioRequest {
    private String audioUrl;
    private String duration;
}
