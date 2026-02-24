package com.scriptkill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("script")
public class Script {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type; // 恐怖/情感/硬核/欢乐/推理等
    private String difficulty; // BEGINNER, INTERMEDIATE, EXPERT
    private Integer duration; // 时长（分钟）
    private Integer minPlayers;
    private Integer maxPlayers;
    private BigDecimal price;
    private String cover;
    private String description;
    private Integer status; // 0-下架，1-上架
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
