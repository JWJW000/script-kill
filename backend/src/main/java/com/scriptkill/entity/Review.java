package com.scriptkill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long userId;
    private Long scriptId;
    private Long hostId;
    private Integer scriptRating;
    private Integer hostRating;
    private String scriptComment;
    private String hostComment;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String scriptName;

    @TableField(exist = false)
    private String hostName;

    /**
     * 评价对应的场次时间（通过订单 -> 场次补充）
     */
    @TableField(exist = false)
    private LocalDateTime sessionTime;
}
