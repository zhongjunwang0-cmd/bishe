package com.english.learning.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("biz_discuss")
public class Discuss implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String content;
    private String targetType;
    private Long targetId;
    private LocalDateTime createTime;
}
