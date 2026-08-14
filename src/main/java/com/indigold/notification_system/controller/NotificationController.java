package com.indigold.notification_system.controller;

import com.indigold.notification_system.dto.NotificationHistoryResponse;
import com.indigold.notification_system.dto.NotificationRequest;
import com.indigold.notification_system.service.NotificationService;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<String> sendNotification(
            @Valid @RequestBody NotificationRequest request
    ) {

        notificationService.sendNotification(request);

        return ResponseEntity.ok(
                "Notification processed successfully"
        );
    }


    // for history---
    @GetMapping("/history")
public ResponseEntity<List<NotificationHistoryResponse>> getNotificationHistory() {
    return ResponseEntity.ok(notificationService.getNotificationHistory());
}
}