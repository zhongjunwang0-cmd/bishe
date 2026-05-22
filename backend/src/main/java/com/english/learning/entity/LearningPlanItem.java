package com.english.learning.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_learning_plan_item")
public class LearningPlanItem implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate weekStart;
    private LocalDate planDate;
    private Integer dayOfWeek;
    private String module;
    /** template | ai_recommend */
    private String source;
    private String taskText;
    private Integer targetMinutes;
    private Integer completed;
    private LocalDateTime completedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
