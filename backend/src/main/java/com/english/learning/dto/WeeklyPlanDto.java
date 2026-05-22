package com.english.learning.dto;

import lombok.Data;

import java.util.List;

@Data
public class WeeklyPlanDto {
    private String weekStart;
    private String weekEnd;
    private String level;
    private String targetExam;
    private Integer dailyGoal;
    private Integer completedCount;
    private Integer totalCount;
    private List<PlanDayDto> days;
    private List<PlanItemDto> todayItems;
}
