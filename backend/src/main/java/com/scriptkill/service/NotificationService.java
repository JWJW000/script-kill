package com.scriptkill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scriptkill.entity.Notification;
import java.util.List;

public interface NotificationService extends IService<Notification> {
    /**
     * 获取用户的通知列表
     */
    List<Notification> listByUserId(Long userId);
    
    /**
     * 获取用户未读通知数量
     */
    int getUnreadCount(Long userId);
    
    /**
     * 标记通知为已读
     */
    void markAsRead(Long notificationId);
    
    /**
     * 标记所有通知为已读
     */
    void markAllAsRead(Long userId);
    
    /**
     * 发送通知
     */
    void sendNotification(Long userId, Integer type, String title, String content, Long orderId, Long teamUpId, Long sessionId);
    
    /**
     * 发送系统通知
     */
    void sendSystemNotification(Long userId, String title, String content);
}
