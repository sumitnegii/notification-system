package com.indigold.notification_system.service;

import com.indigold.notification_system.dto.NotificationHistoryResponse;
import com.indigold.notification_system.dto.NotificationRequest;
import com.indigold.notification_system.entity.User;
import com.indigold.notification_system.entity.UserPreference;
import com.indigold.notification_system.entity.NotificationHistory;
import com.indigold.notification_system.enums.Channel;
import com.indigold.notification_system.enums.DeliveryStatus;
import com.indigold.notification_system.provider.NotificationProvider;
import com.indigold.notification_system.repository.NotificationHistoryRepository;
import com.indigold.notification_system.repository.UserPreferenceRepository;
import com.indigold.notification_system.repository.UserRepository;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final NotificationHistoryRepository notificationHistoryRepository;
    private final NotificationRouter notificationRouter;

    public NotificationService(
            UserRepository userRepository,
            UserPreferenceRepository userPreferenceRepository,
            NotificationHistoryRepository notificationHistoryRepository,
            NotificationRouter notificationRouter
    ) {
        this.userRepository = userRepository;
        this.userPreferenceRepository = userPreferenceRepository;
        this.notificationHistoryRepository = notificationHistoryRepository;
        this.notificationRouter = notificationRouter;
    }

    public void sendNotification(NotificationRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + request.getUserId()
                        )
                );

        UserPreference preference =
                userPreferenceRepository.findByUserId(user.getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Preferences not found for user: "
                                                + user.getId()
                                )
                        );

        for (Channel channel : request.getChannels()) {

            if (!isChannelEnabled(preference, channel)) {

                saveHistory(
                        user,
                        request,
                        channel,
                        DeliveryStatus.SKIPPED,
                        "User has opted out of this channel"
                );

                continue;
            }

            sendThroughProvider(
                    user,
                    request,
                    channel
            );
        }
    }

    private boolean isChannelEnabled(
            UserPreference preference,
            Channel channel
    ) {

        return switch (channel) {

            case EMAIL ->
                    preference.isEmailEnabled();

            case SMS ->
                    preference.isSmsEnabled();

            case PUSH ->
                    preference.isPushEnabled();

            case IN_APP ->
                    preference.isInAppEnabled();
        };
    }

    private void sendThroughProvider(
            User user,
            NotificationRequest request,
            Channel channel
    ) {

        NotificationProvider provider =
                notificationRouter.getProvider(channel);

        try {

            provider.send(
                    user,
                    request.getTitle(),
                    request.getBody()
            );

            saveHistory(
                    user,
                    request,
                    channel,
                    DeliveryStatus.SUCCESS,
                    null
            );

        } catch (Exception exception) {

            saveHistory(
                    user,
                    request,
                    channel,
                    DeliveryStatus.FAILED,
                    exception.getMessage()
            );
        }
    }

    private void saveHistory(
            User user,
            NotificationRequest request,
            Channel channel,
            DeliveryStatus status,
            String errorMessage
    ) {

        NotificationHistory history =
                new NotificationHistory(
                        user,
                        channel,
                        request.getTitle(),
                        request.getBody(),
                        status,
                        errorMessage
                );

        notificationHistoryRepository.save(history);
    }



    public List<NotificationHistoryResponse> getNotificationHistory() {
    return notificationHistoryRepository.findAll()
            .stream()
            .map(history -> new NotificationHistoryResponse(
                    history.getId(),
                    history.getUser().getId(),
                    history.getChannel(),
                    history.getStatus(),
                    history.getErrorMessage(),
                    history.getTitle(),
                    history.getCreatedAt()
            ))
            .toList();
}
}