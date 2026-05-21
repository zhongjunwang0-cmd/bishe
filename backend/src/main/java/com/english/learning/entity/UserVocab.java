package com.english.learning.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("biz_user_vocab")
public class UserVocab implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long vocabId;
    private Integer masteryLevel;
    private Integer reviewStage;
    private String status;
    private LocalDateTime nextReviewTime;
    private LocalDateTime lastReviewTime;
    private Integer reviewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
