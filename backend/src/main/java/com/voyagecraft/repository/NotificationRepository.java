package com.voyagecraft.repository;

import com.voyagecraft.entity.Notification;
import com.voyagecraft.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdAndDismissedFalseOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndReadStatusFalseAndDismissedFalseOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndNotificationTypeAndDismissedFalseOrderByCreatedAtDesc(Long userId, NotificationType type);

    long countByUserIdAndReadStatusFalseAndDismissedFalse(Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.readStatus = true WHERE n.user.id = :userId AND n.readStatus = false")
    void markAllAsRead(@Param("userId") Long userId);

    List<Notification> findByTripIdAndDismissedFalseOrderByCreatedAtDesc(Long tripId);

    int countByTripIdAndUserIdAndReadStatus(Long tripId, Long userId, boolean readStatus);
}
