package com.scriptkill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ai_match_log")
public class AiMatchLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long teamUpId;
    private BigDecimal matchScore;
    private String matchReason;
    private LocalDateTime createTime;
}

