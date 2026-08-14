package com.indigold.notification_system.dto;

import java.time.LocalDateTime;

import com.indigold.notification_system.enums.Channel;
import com.indigold.notification_system.enums.DeliveryStatus;

public record NotificationHistoryResponse(
        Long id,
        Long userId,
        Channel channel,
        DeliveryStatus status,
        String errorMessage,
        String title,
        LocalDateTime createdAt
) {}
