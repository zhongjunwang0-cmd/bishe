package com.english.learning.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String nickname;
    private String email;
    private Long roleId;
    /** 英语水平：A1/A2/B1/B2/CET-4/CET-6/IELTS/TOEFL */
    private String level;
    /** 目标考试：CET-4/CET-6/IELTS/TOEFL/GENERAL */
    private String targetExam;
    /** 每日学习目标（分钟） */
    private Integer dailyGoal;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
