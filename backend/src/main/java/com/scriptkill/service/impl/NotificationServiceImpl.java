package com.scriptkill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scriptkill.entity.Notification;
import com.scriptkill.mapper.NotificationMapper;
import com.scriptkill.service.NotificationService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {
    
    @Override
    public List<Notification> listByUserId(Long userId) {
        QueryWrapper<Notification> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        List<Notification> notifications = this.list(wrapper);
        // 填充类型名称
        for (Notification n : notifications) {
            n.setTypeName(getTypeName(n.getType()));
        }
        return notifications;
    }
    
    @Override
    public int getUnreadCount(Long userId) {
        QueryWrapper<Notification> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("is_read", 0);
        return (int) this.count(wrapper);
    }
    
    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = this.getById(notificationId);
        if (notification != null) {
            notification.setIsRead(1);
            this.updateById(notification);
        }
    }
    
    @Override
    public void markAllAsRead(Long userId) {
        QueryWrapper<Notification> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("is_read", 0);
        Notification update = new Notification();
        update.setIsRead(1);
        this.update(update, wrapper);
    }
    
    @Override
    public void sendNotification(Long userId, Integer type, String title, String content, Long orderId, Long teamUpId, Long sessionId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setOrderId(orderId);
        notification.setTeamUpId(teamUpId);
        notification.setSessionId(sessionId);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        notification.setUpdateTime(LocalDateTime.now());
        notification.setTypeName(getTypeName(type));
        this.save(notification);
    }
    
    @Override
    public void sendSystemNotification(Long userId, String title, String content) {
        sendNotification(userId, 1, title, content, null, null, null);
    }
    
    private String getTypeName(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case 1: return "系统通知";
            case 2: return "拼场通知";
            case 3: return "订单通知";
            case 4: return "场次通知";
            case 5: return "退款通知";
            default: return "未知";
        }
    }
}
