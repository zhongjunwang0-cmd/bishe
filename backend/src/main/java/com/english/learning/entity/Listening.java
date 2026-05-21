package com.english.learning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("biz_listening")
public class Listening implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String category;
    private String duration;
    private String audioUrl;
    private Integer score;
    private LocalDateTime createTime;
}
