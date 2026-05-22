package com.english.learning.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String nickname;
    private String email;
    private String level;
    private String targetExam;
    private Integer dailyGoal;
}
