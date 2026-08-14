package com.indigold.notification_system.repository;

import com.indigold.notification_system.entity.NotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationHistoryRepository
        extends JpaRepository<NotificationHistory, Long> {
}