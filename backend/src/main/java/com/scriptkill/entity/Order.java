package com.scriptkill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long sessionId;
    private Integer playerCount;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private Integer paymentStatus;
    private Integer orderStatus;
    private LocalDateTime payTime;
    private LocalDateTime cancelTime;
    private LocalDateTime completeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 非持久化字段，用于前端展示关联信息
    @TableField(exist = false)
    private String scriptName;

    @TableField(exist = false)
    private Long scriptId;

    @TableField(exist = false)
    private Long hostId;

    @TableField(exist = false)
    private String hostName;

    @TableField(exist = false)
    private LocalDateTime sessionTime;

    @TableField(exist = false)
    private String location;

    @TableField(exist = false)
    private String userName;

    /**
     * 当前订单是否已评价（由后端在列表中补充）
     */
    @TableField(exist = false)
    private Boolean reviewed;
}
