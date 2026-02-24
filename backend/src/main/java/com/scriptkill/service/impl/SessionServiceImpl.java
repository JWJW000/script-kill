package com.scriptkill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scriptkill.common.Constants;
import com.scriptkill.entity.Session;
import com.scriptkill.mapper.SessionMapper;
import com.scriptkill.service.SessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SessionServiceImpl extends ServiceImpl<SessionMapper, Session> implements SessionService {
    @Override
    public List<Session> listAvailableSessions(Long scriptId, LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper<Session> wrapper = new QueryWrapper<>();
        wrapper.eq("status", Constants.SESSION_STATUS_AVAILABLE);
        wrapper.gt("session_time", LocalDateTime.now());
        
        if (scriptId != null) {
            wrapper.eq("script_id", scriptId);
        }
        if (startTime != null) {
            wrapper.ge("session_time", startTime);
        }
        if (endTime != null) {
            wrapper.le("session_time", endTime);
        }
        
        return this.list(wrapper);
    }

    @Override
    public List<Session> listAllSessions() {
        QueryWrapper<Session> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("session_time");
        return this.list(wrapper);
    }

    @Override
    @Transactional
    public void bookSession(Long sessionId, Integer playerCount) {
        Session session = this.getById(sessionId);
        if (session == null) {
            throw new RuntimeException("场次不存在");
        }
        if (session.getStatus() != Constants.SESSION_STATUS_AVAILABLE) {
            throw new RuntimeException("场次不可预约");
        }
        if (session.getCurrentPlayers() + playerCount > session.getMaxPlayers()) {
            throw new RuntimeException("预约人数超过剩余名额");
        }
        
        UpdateWrapper<Session> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", sessionId);
        updateWrapper.set("current_players", session.getCurrentPlayers() + playerCount);
        if (session.getCurrentPlayers() + playerCount >= session.getMaxPlayers()) {
            updateWrapper.set("status", Constants.SESSION_STATUS_FULL);
        }
        this.update(updateWrapper);
    }

    @Override
    public List<Session> listHostSessions(Long hostId) {
        QueryWrapper<Session> wrapper = new QueryWrapper<>();
        wrapper.eq("host_id", hostId);
        wrapper.orderByDesc("session_time");
        return this.list(wrapper);
    }

    @Override
    @Transactional
    public void confirmAttendance(Long sessionId, Long hostId) {
        Session session = this.getById(sessionId);
        if (session == null) {
            throw new RuntimeException("场次不存在");
        }
        if (!session.getHostId().equals(hostId)) {
            throw new RuntimeException("无权操作此场次");
        }
        session.setAttendanceConfirmed(1);
        this.updateById(session);
    }

    @Override
    @Transactional
    public void cancelConfirmAttendance(Long sessionId, Long hostId) {
        Session session = this.getById(sessionId);
        if (session == null) {
            throw new RuntimeException("场次不存在");
        }
        if (!session.getHostId().equals(hostId)) {
            throw new RuntimeException("无权操作此场次");
        }
        session.setAttendanceConfirmed(0);
        this.updateById(session);
    }

    @Override
    @Transactional
    public void cancelSession(Long sessionId, Long hostId) {
        Session session = this.getById(sessionId);
        if (session == null) {
            throw new RuntimeException("场次不存在");
        }
        if (!session.getHostId().equals(hostId)) {
            throw new RuntimeException("无权操作此场次");
        }
        if (session.getSessionTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("已开始的场次不能取消");
        }
        session.setStatus(Constants.SESSION_STATUS_CANCELLED);
        this.updateById(session);
    }
}
