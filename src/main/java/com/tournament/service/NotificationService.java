package com.tournament.service;

import com.tournament.model.Notification;
import com.tournament.model.enums.NotificationStatus;
import com.tournament.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service // Singleton pattern via Spring container
@Transactional
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getUserNotifications(Integer userId) {
        return notificationRepository.findByUser_UserIdOrderBySentTimeDesc(userId);
    }

    public List<Notification> getUnreadNotifications(Integer userId) {
        return notificationRepository.findByUser_UserIdAndStatusNotOrderBySentTimeDesc(
            userId, NotificationStatus.READ);
    }

    public long getUnreadCount(Integer userId) {
        return notificationRepository.countByUser_UserIdAndStatusNot(userId, NotificationStatus.READ);
    }

    public void markAsRead(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.setStatus(NotificationStatus.READ);
        notification.setReadTime(java.time.LocalDateTime.now());
        notificationRepository.save(notification);
    }

    public void markAllAsRead(Integer userId) {
        List<Notification> unread = notificationRepository
            .findByUser_UserIdAndStatusNotOrderBySentTimeDesc(userId, NotificationStatus.READ);
        unread.forEach(n -> {
            n.setStatus(NotificationStatus.READ);
            n.setReadTime(java.time.LocalDateTime.now());
        });
        notificationRepository.saveAll(unread);
    }

    @Override
    public void sendNotification(Notification notification) {
        // Protected Variations: failures in delivery do not propagate to core flows.
        try {
            notification.setStatus(NotificationStatus.SENT);
            notification.setDeliveredTime(java.time.LocalDateTime.now());
            notificationRepository.save(notification);
        } catch (RuntimeException ex) {
            // Intentionally swallow to keep core system resilient
        }
    }
}
