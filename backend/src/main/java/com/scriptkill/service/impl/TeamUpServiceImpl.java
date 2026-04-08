package com.scriptkill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scriptkill.common.Constants;
import com.scriptkill.entity.AiMatchLog;
import com.scriptkill.entity.Order;
import com.scriptkill.entity.Script;
import com.scriptkill.entity.Session;
import com.scriptkill.entity.TeamUp;
import com.scriptkill.entity.TeamUpParticipant;
import com.scriptkill.mapper.AiMatchLogMapper;
import com.scriptkill.mapper.OrderMapper;
import com.scriptkill.mapper.TeamUpMapper;
import com.scriptkill.mapper.TeamUpParticipantMapper;
import com.scriptkill.service.AiMatchReasonService;
import com.scriptkill.service.OrderService;
import com.scriptkill.service.ScriptService;
import com.scriptkill.service.SessionService;
import com.scriptkill.service.TeamUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeamUpServiceImpl extends ServiceImpl<TeamUpMapper, TeamUp> implements TeamUpService {
    @Autowired
    private ScriptService scriptService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private AiMatchLogMapper aiMatchLogMapper;
    @Autowired
    private AiMatchReasonService aiMatchReasonService;
    @Autowired
    private TeamUpParticipantMapper teamUpParticipantMapper;
    @Autowired
    private OrderMapper orderMapper;

    @Override
    @Transactional
    public TeamUp createTeamUp(Long userId, Long scriptId, LocalDateTime expectedTime, Integer totalPlayers, String remark) {
        TeamUp teamUp = new TeamUp();
        teamUp.setScriptId(scriptId);
        teamUp.setInitiatorId(userId);
        teamUp.setExpectedTime(expectedTime);
        teamUp.setTotalPlayers(totalPlayers);
        teamUp.setCurrentPlayers(1);
        teamUp.setRemark(remark);
        teamUp.setStatus(Constants.TEAM_UP_STATUS_ACTIVE);
        
        this.save(teamUp);

        // 记录发起人为参与者
        TeamUpParticipant initiator = new TeamUpParticipant();
        initiator.setTeamUpId(teamUp.getId());
        initiator.setUserId(userId);
        initiator.setJoinTime(LocalDateTime.now());
        teamUpParticipantMapper.insert(initiator);

        Script script = scriptService.getById(scriptId);
        if (script != null) {
            teamUp.setScriptName(script.getName());
        }

        // 添加发起人到参与表（这里简化处理，实际应该用关联表）
        return teamUp;
    }

    @Override
    @Transactional
    public void joinTeamUp(Long teamUpId, Long userId) {
        TeamUp teamUp = this.getById(teamUpId);
        if (teamUp == null) {
            throw new RuntimeException("拼场不存在");
        }
        if (teamUp.getStatus() != Constants.TEAM_UP_STATUS_ACTIVE) {
            throw new RuntimeException("拼场已关闭或已完成");
        }
        // 已经在该拼场中，禁止重复加入
        QueryWrapper<TeamUpParticipant> existsWrapper = new QueryWrapper<>();
        existsWrapper.eq("team_up_id", teamUpId).eq("user_id", userId);
        if (teamUpParticipantMapper.selectCount(existsWrapper) > 0) {
            throw new RuntimeException("您已在该拼场中，无需重复加入");
        }

        if (teamUp.getCurrentPlayers() >= teamUp.getTotalPlayers()) {
            throw new RuntimeException("拼场已满");
        }
        
        UpdateWrapper<TeamUp> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", teamUpId);
        int newCurrent = teamUp.getCurrentPlayers() + 1;
        updateWrapper.set("current_players", newCurrent);
        
        if (newCurrent >= teamUp.getTotalPlayers()) {
            updateWrapper.set("status", Constants.TEAM_UP_STATUS_FULL);
            // 满员后生成待支付订单（不立即创建session，等所有支付完成后创建）
            createOrdersForTeamUp(teamUp);
        }
        
        this.update(updateWrapper);

        // 记录当前用户为参与者
        TeamUpParticipant participant = new TeamUpParticipant();
        participant.setTeamUpId(teamUpId);
        participant.setUserId(userId);
        participant.setJoinTime(LocalDateTime.now());
        teamUpParticipantMapper.insert(participant);
    }

    @Override
    @Transactional
    public void leaveTeamUp(Long teamUpId, Long userId) {
        TeamUp teamUp = this.getById(teamUpId);
        if (teamUp == null) {
            throw new RuntimeException("拼场不存在");
        }
        // 只允许在进行中的拼场中退出，生成场次后需要通过订单流程处理
        if (teamUp.getStatus() != Constants.TEAM_UP_STATUS_ACTIVE) {
            throw new RuntimeException("拼场已锁定，无法直接退出，请联系店员处理订单");
        }

        QueryWrapper<TeamUpParticipant> wrapper = new QueryWrapper<>();
        wrapper.eq("team_up_id", teamUpId).eq("user_id", userId);
        TeamUpParticipant participant = teamUpParticipantMapper.selectOne(wrapper);
        if (participant == null) {
            throw new RuntimeException("您尚未加入该拼场");
        }

        // 删除参与记录并减少当前人数
        teamUpParticipantMapper.delete(wrapper);
        int newCurrent = Math.max(0, teamUp.getCurrentPlayers() - 1);
        teamUp.setCurrentPlayers(newCurrent);
        if (newCurrent <= 0) {
            teamUp.setStatus(Constants.TEAM_UP_STATUS_CLOSED);
        }
        this.updateById(teamUp);
    }

    /**
     * 满员后为所有参与者生成待支付订单（不创建session，等所有支付完成后创建）
     */
    private void createOrdersForTeamUp(TeamUp teamUp) {
        Script script = scriptService.getById(teamUp.getScriptId());
        if (script == null) {
            return;
        }
        
        // 为所有参与者生成待支付订单（临时用teamUp.getId()作为sessionId标识）
        QueryWrapper<TeamUpParticipant> participantWrapper = new QueryWrapper<>();
        participantWrapper.eq("team_up_id", teamUp.getId());
        List<TeamUpParticipant> participants = teamUpParticipantMapper.selectList(participantWrapper);
        if (participants != null && !participants.isEmpty()) {
            for (TeamUpParticipant participant : participants) {
                try {
                    // 使用teamUp.id * -1 作为临时sessionId标识，实际创建session时替换
                    orderService.createTeamUpOrder(participant.getUserId(), teamUp.getId(), 1, script.getPrice());
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * 全部支付完成后创建正式场次
     */
    public void createSessionFromTeamUp(Long teamUpId) {
        TeamUp teamUp = this.getById(teamUpId);
        if (teamUp == null || teamUp.getSessionId() != null) {
            return; // 已经有场次或拼场不存在
        }
        
        Script script = scriptService.getById(teamUp.getScriptId());
        if (script == null) {
            return;
        }
        
        Session session = new Session();
        session.setScriptId(teamUp.getScriptId());
        session.setHostId(1L); // 默认主持人，实际应该分配
        session.setSessionTime(teamUp.getExpectedTime());
        session.setLocation("待定");
        session.setPrice(script.getPrice());
        session.setMaxPlayers(teamUp.getTotalPlayers());
        session.setCurrentPlayers(teamUp.getTotalPlayers());
        session.setStatus(Constants.SESSION_STATUS_FULL); // 直接设为已满员
        
        sessionService.save(session);
        
        // 更新订单中的临时sessionId为正式sessionId
        orderService.updateTeamUpOrdersToSession(teamUp.getId(), session.getId());
        
        // 更新拼场状态并关联生成的场次
        teamUp.setSessionId(session.getId());
        teamUp.setStatus(Constants.TEAM_UP_STATUS_SESSION_CREATED);
        this.updateById(teamUp);
    }

    @Override
    public List<TeamUp> listTeamUps(String type, LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper<TeamUp> wrapper = new QueryWrapper<>();
        wrapper.eq("status", Constants.TEAM_UP_STATUS_ACTIVE);
        
        if (startTime != null) {
            wrapper.ge("expected_time", startTime);
        }
        if (endTime != null) {
            wrapper.le("expected_time", endTime);
        }

        List<TeamUp> list = this.list(wrapper);
        fillScriptInfo(list);
        return list;
    }

    @Override
    public List<TeamUp> listTeamUpsByScriptId(Long scriptId) {
        QueryWrapper<TeamUp> wrapper = new QueryWrapper<>();
        wrapper.eq("status", Constants.TEAM_UP_STATUS_ACTIVE);
        wrapper.eq("script_id", scriptId);
        wrapper.gt("expected_time", LocalDateTime.now());
        wrapper.orderByAsc("expected_time");
        
        List<TeamUp> list = this.list(wrapper);
        fillScriptInfo(list);
        return list;
    }

    @Override
    public List<TeamUp> getRecommendedTeamUps(Long userId) {
        // AI智能匹配：根据用户历史订单偏好推荐
        // 1. 获取用户历史订单，分析偏好（剧本类型、难度、时间段等）
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Order> orderWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        orderWrapper.eq("user_id", userId);
        orderWrapper.eq("order_status", Constants.ORDER_STATUS_COMPLETED);
        List<Order> userOrders = orderService.list(orderWrapper);
        
        // 2. 分析用户偏好
        java.util.Set<Long> preferredScriptIds = new java.util.HashSet<>();
        java.util.Set<String> preferredTypes = new java.util.HashSet<>();
        
        for (Order order : userOrders) {
            Session session = sessionService.getById(order.getSessionId());
            if (session != null) {
                Script script = scriptService.getById(session.getScriptId());
                if (script != null) {
                    preferredScriptIds.add(script.getId());
                    preferredTypes.add(script.getType());
                }
            }
        }
        
        // 3. 获取所有进行中的拼场
        QueryWrapper<TeamUp> wrapper = new QueryWrapper<>();
        wrapper.eq("status", Constants.TEAM_UP_STATUS_ACTIVE);
        wrapper.gt("expected_time", LocalDateTime.now());
        List<TeamUp> allTeamUps = this.list(wrapper);
        
        // 4. 计算每个剧本的热度（被预约次数）
        Map<Long, Long> scriptPopularity = calculateScriptPopularity();
        
        // 5. 计算匹配分数并排序
        List<MatchResult> scoredTeamUps = allTeamUps.stream()
            .map(teamUp -> {
                Script script = scriptService.getById(teamUp.getScriptId());
                if (script == null) return null;

                double score = 0.0;

                // 偏好类型匹配 +10分
                if (preferredTypes.contains(script.getType())) {
                    score += 10;
                }
                // 偏好剧本匹配 +20分
                if (preferredScriptIds.contains(script.getId())) {
                    score += 20;
                }
                // 时间接近度（越接近当前时间+期望时间，分数越高）
                long hoursDiff = Math.abs(java.time.Duration.between(LocalDateTime.now(), teamUp.getExpectedTime()).toHours());
                if (hoursDiff < 24) {
                    score += 15;
                } else if (hoursDiff < 72) {
                    score += 10;
                } else if (hoursDiff < 168) {
                    score += 5;
                }

                // 剩余名额越多，分数越高（更容易加入）
                int remaining = teamUp.getTotalPlayers() - teamUp.getCurrentPlayers();
                if (remaining > 0) {
                    score += remaining * 2;
                }

                // 剧本热度加权（根据被预约次数）
                long popularity = scriptPopularity.getOrDefault(script.getId(), 0L);
                score += popularity * 0.5;  // 每被预约1次加0.5分

                return new MatchResult(teamUp, score, null);
            })
            .filter(Objects::nonNull)
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .limit(10)
            .collect(Collectors.toList());

        List<TeamUp> result;
        if (scoredTeamUps.isEmpty()) {
            result = allTeamUps.stream().limit(10).collect(Collectors.toList());
        } else {
            result = scoredTeamUps.stream().map(MatchResult::getTeamUp).collect(Collectors.toList());
        }

        fillScriptInfo(result);

        // 为推荐结果生成个性化推荐理由，并写入 ai_match_log
        for (MatchResult mr : scoredTeamUps) {
            TeamUp teamUp = mr.getTeamUp();
            Script script = scriptService.getById(teamUp.getScriptId());
            int remaining = Math.max(0, teamUp.getTotalPlayers() - teamUp.getCurrentPlayers());

            String reason = aiMatchReasonService.generateReason(userId, script, teamUp, remaining);
            if (reason == null || reason.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                appendReason(sb, "开场时间合适");
                if (remaining > 0) {
                    appendReason(sb, "剩余名额 " + remaining + " 个");
                }
                reason = sb.toString();
            }

            teamUp.setRecommendReason(reason);

            AiMatchLog log = new AiMatchLog();
            log.setUserId(userId);
            log.setTeamUpId(teamUp.getId());
            log.setMatchScore(java.math.BigDecimal.valueOf(mr.getScore()));
            log.setMatchReason(reason);
            aiMatchLogMapper.insert(log);
        }

        return result;
    }

    private Map<Long, Long> calculateScriptPopularity() {
        // 统计每个剧本被完成订单预约的次数作为热度
        QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
        orderWrapper.eq("order_status", Constants.ORDER_STATUS_COMPLETED);
        List<Order> completedOrders = orderMapper.selectList(orderWrapper);
        
        Map<Long, Long> popularity = new HashMap<>();
        for (Order order : completedOrders) {
            Session session = sessionService.getById(order.getSessionId());
            if (session != null) {
                Long scriptId = session.getScriptId();
                popularity.merge(scriptId, 1L, Long::sum);
            }
        }
        return popularity;
    }

    private void fillScriptInfo(List<TeamUp> teamUps) {
        if (teamUps == null || teamUps.isEmpty()) return;
        for (TeamUp teamUp : teamUps) {
            if (teamUp == null || teamUp.getScriptId() == null) continue;
            Script script = scriptService.getById(teamUp.getScriptId());
            if (script != null) {
                teamUp.setScriptName(script.getName());
            }
        }
    }

    private void appendReason(StringBuilder sb, String part) {
        if (sb.length() > 0) {
            sb.append("；");
        }
        sb.append(part);
    }

    private static class MatchResult {
        private final TeamUp teamUp;
        private final double score;
        private final String reason;

        public MatchResult(TeamUp teamUp, double score, String reason) {
            this.teamUp = teamUp;
            this.score = score;
            this.reason = reason;
        }

        public TeamUp getTeamUp() {
            return teamUp;
        }

        public double getScore() {
            return score;
        }

        public String getReason() {
            return reason;
        }
    }
}
