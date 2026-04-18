package com.tournament.repository;

import com.tournament.model.Notification;
import com.tournament.model.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUser_UserIdOrderBySentTimeDesc(Integer userId);
    List<Notification> findByUser_UserIdAndStatusNotOrderBySentTimeDesc(Integer userId, NotificationStatus status);
    long countByUser_UserIdAndStatusNot(Integer userId, NotificationStatus status);
}
