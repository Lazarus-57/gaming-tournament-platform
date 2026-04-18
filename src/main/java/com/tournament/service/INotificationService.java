package com.tournament.service;

import com.tournament.model.Notification;
import java.util.List;

public interface INotificationService {
    List<Notification> getUserNotifications(Integer userId);
    List<Notification> getUnreadNotifications(Integer userId);
    long getUnreadCount(Integer userId);
    void markAsRead(Integer notificationId);
    void markAllAsRead(Integer userId);
    void sendNotification(Notification notification);
}
