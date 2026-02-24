package com.scriptkill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("team_up_participant")
public class TeamUpParticipant {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long teamUpId;
    private Long userId;
    private LocalDateTime joinTime;
}

