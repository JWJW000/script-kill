package com.scriptkill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`session`")
public class Session {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long scriptId;
    private Long hostId;
    private LocalDateTime sessionTime;
    private String location;
    private BigDecimal price;
    private Integer maxPlayers;
    private Integer currentPlayers;
    private Integer status;
    /** 主持人是否确认到场：0-未确认，1-已确认 */
    private Integer attendanceConfirmed;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String scriptName;

    @TableField(exist = false)
    private String hostName;

    /** 当前用户是否已预约该场次（仅对玩家列表有效） */
    @TableField(exist = false)
    private Boolean userBooked;
}
