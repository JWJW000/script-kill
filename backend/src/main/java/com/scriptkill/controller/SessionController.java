package com.scriptkill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.scriptkill.common.Result;
import com.scriptkill.entity.Order;
import com.scriptkill.entity.Script;
import com.scriptkill.entity.Session;
import com.scriptkill.entity.User;
import com.scriptkill.service.OrderService;
import com.scriptkill.service.ScriptService;
import com.scriptkill.service.SessionService;
import com.scriptkill.common.Constants;
import com.scriptkill.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/session")
public class SessionController {
    @Autowired
    private SessionService sessionService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ScriptService scriptService;
    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<List<Session>> list(
            @RequestParam(required = false) Long scriptId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request) {

        String role = (String) request.getAttribute("role");
        List<Session> sessions;

        if ("ADMIN".equals(role)) {
            // 管理员查看全部场次（不限状态和时间）
            sessions = sessionService.listAllSessions();
        } else {
            // 玩家只看可预约的未来场次
            LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime) : null;
            LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime) : null;
            sessions = sessionService.listAvailableSessions(scriptId, start, end);
        }

        // 补全关联信息
        for (Session session : sessions) {
            enrichSession(session);
        }

        // 玩家端：标记当前用户是否已预约各场次
        if ("PLAYER".equals(role)) {
            Long userId = (Long) request.getAttribute("userId");
            if (userId != null) {
                for (Session session : sessions) {
                    QueryWrapper<Order> q = new QueryWrapper<>();
                    q.eq("session_id", session.getId());
                    q.eq("user_id", userId);
                    q.ne("order_status", 2); // 排除已取消
                    session.setUserBooked(orderService.count(q) > 0);
                }
            }
        }

        return Result.success(sessions);
    }

    @PostMapping
    public Result<?> save(@RequestBody Session session, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        // 新增场次时确保状态为「可预约」、当前人数为 0，否则玩家端列表不展示
        if (session.getStatus() == null) {
            session.setStatus(Constants.SESSION_STATUS_AVAILABLE);
        }
        if (session.getCurrentPlayers() == null) {
            session.setCurrentPlayers(0);
        }
        sessionService.save(session);
        return Result.success("保存成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Session session, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        session.setId(id);
        sessionService.updateById(session);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
        orderWrapper.eq("session_id", id);
        long orderCount = orderService.count(orderWrapper);
        if (orderCount > 0) {
            return Result.error("该场次下有 " + orderCount + " 条订单记录，无法直接删除。请先取消/退款所有订单，或改为下架处理");
        }
        sessionService.removeById(id);
        return Result.success("删除成功");
    }

    @PostMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        Session session = sessionService.getById(id);
        if (session == null) {
            return Result.error("场次不存在");
        }
        
        // 如果是将状态改为"已取消/下架"，需要自动退款
        if (status == Constants.SESSION_STATUS_CANCELLED) {
            try {
                sessionService.cancelSessionByAdmin(id);
                return Result.success("场次已下架，已支付订单已自动退款");
            } catch (Exception e) {
                return Result.error(e.getMessage());
            }
        }
        
        session.setStatus(status);
        sessionService.updateById(session);
        return Result.success("状态更新成功");
    }

    @GetMapping("/host/my-sessions")
    public Result<List<Session>> getHostSessions(HttpServletRequest request) {
        Long hostId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (!"HOST".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        List<Session> sessions = sessionService.listHostSessions(hostId);
        for (Session session : sessions) {
            enrichSession(session);
        }
        return Result.success(sessions);
    }

    @PostMapping("/{id}/confirm")
    public Result<?> confirmAttendance(@PathVariable Long id, HttpServletRequest request) {
        Long hostId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (!"HOST".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        try {
            sessionService.confirmAttendance(id, hostId);
            return Result.success("确认到场成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel-confirm")
    public Result<?> cancelConfirmAttendance(@PathVariable Long id, HttpServletRequest request) {
        Long hostId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (!"HOST".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        try {
            sessionService.cancelConfirmAttendance(id, hostId);
            return Result.success("已取消确认到场");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public Result<?> cancelSession(@PathVariable Long id, HttpServletRequest request) {
        Long hostId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (!"HOST".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        try {
            sessionService.cancelSession(id, hostId);
            return Result.success("取消成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/end")
    public Result<?> endSession(@PathVariable Long id, HttpServletRequest request) {
        Long hostId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (!"HOST".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        try {
            sessionService.endSession(id, hostId);
            return Result.success("场次已结束");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private void enrichSession(Session session) {
        if (session == null) return;
        try {
            if (session.getScriptId() != null) {
                Script script = scriptService.getById(session.getScriptId());
                if (script != null) {
                    session.setScriptName(script.getName());
                }
            }
            if (session.getHostId() != null) {
                User host = userService.getById(session.getHostId());
                if (host != null) {
                    session.setHostName(host.getRealName() != null ? host.getRealName() : host.getUsername());
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }
}
