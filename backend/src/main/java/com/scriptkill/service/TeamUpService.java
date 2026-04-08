package com.scriptkill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scriptkill.entity.TeamUp;
import java.time.LocalDateTime;
import java.util.List;

public interface TeamUpService extends IService<TeamUp> {
    TeamUp createTeamUp(Long userId, Long scriptId, LocalDateTime expectedTime, Integer totalPlayers, String remark);
    void joinTeamUp(Long teamUpId, Long userId);
    void leaveTeamUp(Long teamUpId, Long userId);
    List<TeamUp> listTeamUps(String type, LocalDateTime startTime, LocalDateTime endTime);
    List<TeamUp> listTeamUpsByScriptId(Long scriptId);
    List<TeamUp> getRecommendedTeamUps(Long userId);
    void createSessionFromTeamUp(Long teamUpId);
}
