package com.english.learning.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("biz_vocab")
public class Vocab implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String word;
    private String phonetic;
    private String translation;
    private String example;
    private String level;
    private LocalDateTime createTime;
}
