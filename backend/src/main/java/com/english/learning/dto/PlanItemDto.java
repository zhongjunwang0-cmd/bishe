package com.english.learning.dto;

import lombok.Data;

@Data
public class PlanItemDto {
    private Long id;
    private String planDate;
    private Integer dayOfWeek;
    private String dayLabel;
    private String module;
    private String moduleLabel;
    private String source;
    private String taskText;
    private Integer targetMinutes;
    private Boolean completed;
}
