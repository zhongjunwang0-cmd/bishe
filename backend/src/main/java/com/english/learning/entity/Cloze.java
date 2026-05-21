package com.english.learning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("biz_cloze")
public class Cloze implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bankId;
    private String title;
    private String content;
    private Integer blanksCount;
    @JsonIgnore
    private String questionsJson;
    @JsonIgnore
    private String answersJson;
    private String completionStatus;
    private Integer score;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private Object questions;

    @TableField(exist = false)
    private Object answers;
}
