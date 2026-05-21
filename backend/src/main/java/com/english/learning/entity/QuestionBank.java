package com.english.learning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("biz_question_bank")
public class QuestionBank implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String moduleType;
    private String title;
    private String content;
    private String difficulty;
    private String category;
    private String duration;
    private String audioUrl;
    private String questionsJson;
    private String answersJson;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
