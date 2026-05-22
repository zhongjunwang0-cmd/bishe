package com.english.learning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("biz_oral")
public class Oral implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String topic;
    /** English sentence for Whisper+WER pronunciation scoring */
    private String referenceText;
    private Integer score;
    private Integer attempts;
    private LocalDateTime createTime;
}
