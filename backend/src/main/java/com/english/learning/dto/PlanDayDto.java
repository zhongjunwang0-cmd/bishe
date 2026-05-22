package com.english.learning.dto;

import lombok.Data;

import java.util.List;

@Data
public class PlanDayDto {
    private String date;
    private Integer dayOfWeek;
    private String dayLabel;
    private boolean today;
    private List<PlanItemDto> items;
}
