package com.scriptkill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("team_up")
public class TeamUp {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long scriptId;
    private Long initiatorId;
    private LocalDateTime expectedTime;
    private Integer totalPlayers;
    private Integer currentPlayers;
    private String remark;
    private Integer status; // 0-已关闭，1-进行中，2-已达标，3-已生成场次
    private Long sessionId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String scriptName;

    /**
     * 当前登录用户是否已加入该拼场（仅在玩家端列表/推荐中使用）
     */
    @TableField(exist = false)
    private Boolean joined;

    /**
     * 针对当前用户的推荐理由（由大模型生成，仅在推荐列表中使用）
     */
    @TableField(exist = false)
    private String recommendReason;
}
