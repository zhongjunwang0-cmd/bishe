package com.english.learning.dto;

import lombok.Data;

@Data
public class UserProfileDto {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String level;
    private String targetExam;
    private Integer dailyGoal;
}
