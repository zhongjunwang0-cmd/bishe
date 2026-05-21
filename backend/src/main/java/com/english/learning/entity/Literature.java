package com.english.learning.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_literature")
public class Literature {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String translation;
    private String author;
    private LocalDateTime createTime;
}
