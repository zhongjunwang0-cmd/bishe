package com.english.learning.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ModuleStatDto {
    private String module;
    private double accuracy;
    private int attempts;

    @JsonProperty("avg_elapsed")
    private double avgElapsed;
}
