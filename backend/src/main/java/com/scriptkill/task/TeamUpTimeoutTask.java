package com.scriptkill.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.scriptkill.common.Constants;
import com.scriptkill.entity.TeamUp;
import com.scriptkill.entity.TeamUpParticipant;
import com.scriptkill.mapper.TeamUpParticipantMapper;
import com.scriptkill.service.NotificationService;
import com.scriptkill.service.TeamUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class TeamUpTimeoutTask {
    @Autowired
    private TeamUpService teamUpService;
    @Autowired
    private TeamUpParticipantMapper teamUpParticipantMapper;
    @Autowired
    private NotificationService notificationService;

    /**
     * 每小时检查一次超时24小时未满员的拼场，自动取消并通知
     */
    @Scheduled(fixedRate = 3600000) // 1小时 = 3600000毫秒
    public void checkTimeoutTeamUps() {
        // 查找进行中的拼场，且期望时间已超过24小时且未满员
        QueryWrapper<TeamUp> wrapper = new QueryWrapper<>();
        wrapper.eq("status", Constants.TEAM_UP_STATUS_ACTIVE);
        wrapper.apply("expected_time < {0}", LocalDateTime.now().minusHours(24));
        
        List<TeamUp> timeoutTeamUps = teamUpService.list(wrapper);
        
        for (TeamUp teamUp : timeoutTeamUps) {
            try {
                // 检查是否确实未满员
                if (teamUp.getCurrentPlayers() < teamUp.getTotalPlayers()) {
                    closeTimeoutTeamUp(teamUp);
                }
            } catch (Exception e) {
                System.err.println("处理超时拼场失败: " + teamUp.getId() + ", 原因: " + e.getMessage());
            }
        }
    }

    private void closeTimeoutTeamUp(TeamUp teamUp) {
        // 更新拼场状态为已关闭
        UpdateWrapper<TeamUp> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", teamUp.getId());
        updateWrapper.set("status", Constants.TEAM_UP_STATUS_CLOSED);
        teamUpService.update(updateWrapper);
        
        // 获取所有参与者并发送通知
        QueryWrapper<TeamUpParticipant> participantWrapper = new QueryWrapper<>();
        participantWrapper.eq("team_up_id", teamUp.getId());
        List<TeamUpParticipant> participants = teamUpParticipantMapper.selectList(participantWrapper);
        
        for (TeamUpParticipant participant : participants) {
            try {
                notificationService.sendNotification(
                    participant.getUserId(),
                    2, // 拼场通知
                    "拼场已超时取消",
                    "您参与的拼场因超过24小时未凑齐人数，已被系统自动取消。",
                    null,
                    teamUp.getId(),
                    null
                );
            } catch (Exception e) {
                System.err.println("发送拼场取消通知失败: " + e.getMessage());
            }
        }
    }
}
