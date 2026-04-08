package com.scriptkill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scriptkill.entity.Session;
import java.time.LocalDateTime;
import java.util.List;

public interface SessionService extends IService<Session> {
    List<Session> listAvailableSessions(Long scriptId, LocalDateTime startTime, LocalDateTime endTime);
    List<Session> listAllSessions();
    void bookSession(Long sessionId, Integer playerCount);
    List<Session> listHostSessions(Long hostId);
    void confirmAttendance(Long sessionId, Long hostId);
    void cancelConfirmAttendance(Long sessionId, Long hostId);
    void cancelSession(Long sessionId, Long hostId);
    void cancelSessionByAdmin(Long sessionId);
    void endSession(Long sessionId, Long hostId);
}
