package com.scriptkill.controller;

import com.scriptkill.common.Result;
import com.scriptkill.entity.Notification;
import com.scriptkill.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notification")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/list")
    public Result<List<Notification>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Notification> notifications = notificationService.listByUserId(userId);
        return Result.success(notifications);
    }

    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        int count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    @PostMapping("/{id}/read")
    public Result<?> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        notificationService.markAsRead(id);
        return Result.success("已标记为已读");
    }

    @PostMapping("/read-all")
    public Result<?> markAllAsRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        notificationService.markAllAsRead(userId);
        return Result.success("已标记全部为已读");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Notification notification = notificationService.getById(id);
        if (notification == null) {
            return Result.error("通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            return Result.error(403, "无权限删除");
        }
        notificationService.removeById(id);
        return Result.success("删除成功");
    }
}
