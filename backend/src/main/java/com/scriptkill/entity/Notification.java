package com.scriptkill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 通知类型：1-系统通知，2-拼场通知，3-订单通知，4-场次通知，5-退款通知 */
    private Integer type;
    
    /** 通知标题 */
    private String title;
    
    /** 通知内容 */
    private String content;
    
    /** 关联的用户ID */
    private Long userId;
    
    /** 关联的订单ID（可选） */
    private Long orderId;
    
    /** 关联的拼场ID（可选） */
    private Long teamUpId;
    
    /** 关联的场次ID（可选） */
    private Long sessionId;
    
    /** 是否已读：0-未读，1-已读 */
    private Integer isRead;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private String typeName;
}
