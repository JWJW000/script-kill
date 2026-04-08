package com.scriptkill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.scriptkill.common.Result;
import com.scriptkill.entity.TeamUp;
import com.scriptkill.entity.TeamUpParticipant;
import com.scriptkill.mapper.TeamUpParticipantMapper;
import com.scriptkill.service.TeamUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/team-up")
public class TeamUpController {
    @Autowired
    private TeamUpService teamUpService;
    @Autowired
    private TeamUpParticipantMapper teamUpParticipantMapper;

    @PostMapping("/create")
    public Result<TeamUp> createTeamUp(@RequestParam Long scriptId, @RequestParam String expectedTime,
                                       @RequestParam Integer totalPlayers, @RequestParam(required = false) String remark,
                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            LocalDateTime time = LocalDateTime.parse(expectedTime);
            TeamUp teamUp = teamUpService.createTeamUp(userId, scriptId, time, totalPlayers, remark);
            return Result.success("拼场创建成功", teamUp);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/join/{id}")
    public Result<?> joinTeamUp(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            teamUpService.joinTeamUp(id, userId);
            return Result.success("加入拼场成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/leave/{id}")
    public Result<?> leaveTeamUp(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            teamUpService.leaveTeamUp(id, userId);
            return Result.success("已退出该拼场");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<TeamUp>> listTeamUps(@RequestParam(required = false) String type,
                                            @RequestParam(required = false) String startTime,
                                            @RequestParam(required = false) String endTime,
                                            @RequestParam(required = false) Long scriptId,
                                            HttpServletRequest request) {
        LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime) : null;
        LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime) : null;
        
        List<TeamUp> teamUps;
        if (scriptId != null) {
            teamUps = teamUpService.listTeamUpsByScriptId(scriptId);
        } else {
            teamUps = teamUpService.listTeamUps(type, start, end);
        }

        // 为玩家标记 joined 状态
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (userId != null && "PLAYER".equals(role) && teamUps != null && !teamUps.isEmpty()) {
            QueryWrapper<TeamUpParticipant> qw = new QueryWrapper<>();
            qw.eq("user_id", userId);
            List<TeamUpParticipant> participants = teamUpParticipantMapper.selectList(qw);
            java.util.Set<Long> joinedIds = new java.util.HashSet<>();
            for (TeamUpParticipant p : participants) {
                joinedIds.add(p.getTeamUpId());
            }
            for (TeamUp t : teamUps) {
                t.setJoined(joinedIds.contains(t.getId()));
            }
        }
        return Result.success(teamUps);
    }

    @GetMapping("/recommend")
    public Result<List<TeamUp>> getRecommendedTeamUps(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<TeamUp> teamUps = teamUpService.getRecommendedTeamUps(userId);

        // 推荐列表同样标记 joined 状态
        if (userId != null && teamUps != null && !teamUps.isEmpty()) {
            QueryWrapper<TeamUpParticipant> qw = new QueryWrapper<>();
            qw.eq("user_id", userId);
            List<TeamUpParticipant> participants = teamUpParticipantMapper.selectList(qw);
            java.util.Set<Long> joinedIds = new java.util.HashSet<>();
            for (TeamUpParticipant p : participants) {
                joinedIds.add(p.getTeamUpId());
            }
            for (TeamUp t : teamUps) {
                t.setJoined(joinedIds.contains(t.getId()));
            }
        }
        return Result.success(teamUps);
    }

    @GetMapping("/my-teamups")
    public Result<List<TeamUp>> getMyTeamUps(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        
        // 获取用户参与的所有拼场ID
        QueryWrapper<TeamUpParticipant> participantWrapper = new QueryWrapper<>();
        participantWrapper.eq("user_id", userId);
        List<TeamUpParticipant> participants = teamUpParticipantMapper.selectList(participantWrapper);
        
        if (participants.isEmpty()) {
            return Result.success(java.util.Collections.emptyList());
        }
        
        java.util.List<Long> teamUpIds = new java.util.ArrayList<>();
        for (TeamUpParticipant p : participants) {
            teamUpIds.add(p.getTeamUpId());
        }
        
        // 获取这些拼场的详情
        List<TeamUp> teamUps = teamUpService.listByIds(teamUpIds);
        
        return Result.success(teamUps);
    }
}
