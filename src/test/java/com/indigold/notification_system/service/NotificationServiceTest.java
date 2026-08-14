package com.indigold.notification_system.service;

import com.indigold.notification_system.dto.NotificationRequest;
import com.indigold.notification_system.entity.NotificationHistory;
import com.indigold.notification_system.entity.User;
import com.indigold.notification_system.entity.UserPreference;
import com.indigold.notification_system.enums.Channel;
import com.indigold.notification_system.enums.DeliveryStatus;
import com.indigold.notification_system.provider.NotificationProvider;
import com.indigold.notification_system.repository.NotificationHistoryRepository;
import com.indigold.notification_system.repository.UserPreferenceRepository;
import com.indigold.notification_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPreferenceRepository userPreferenceRepository;

    @Mock
    private NotificationHistoryRepository notificationHistoryRepository;

    @Mock
    private NotificationRouter notificationRouter;

    @Mock
    private NotificationProvider emailProvider;

    @Mock
    private NotificationProvider smsProvider;

    @Mock
    private NotificationProvider pushProvider;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void shouldSendNotificationSuccessfully() {
        User user = user();
        UserPreference preference = enabledPreference(user);
        NotificationRequest request = request(Channel.EMAIL);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(preference));
        when(notificationRouter.getProvider(Channel.EMAIL)).thenReturn(emailProvider);

        notificationService.sendNotification(request);

        verify(emailProvider).send(user, "Test Notification", "Hello from test");

        NotificationHistory history = captureSavedHistory();
        assertEquals(user, history.getUser());
        assertEquals(Channel.EMAIL, history.getChannel());
        assertEquals("Test Notification", history.getTitle());
        assertEquals("Hello from test", history.getBody());
        assertEquals(DeliveryStatus.SUCCESS, history.getStatus());
        assertNull(history.getErrorMessage());
    }

    @Test
    void shouldSkipNotificationWhenUserOptedOut() {
        User user = user();
        UserPreference preference = enabledPreference(user);
        preference.setSmsEnabled(false);
        NotificationRequest request = request(Channel.SMS);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(preference));

        notificationService.sendNotification(request);

        verify(notificationRouter, never()).getProvider(Channel.SMS);
        verifyNoInteractions(smsProvider);

        NotificationHistory history = captureSavedHistory();
        assertEquals(Channel.SMS, history.getChannel());
        assertEquals(DeliveryStatus.SKIPPED, history.getStatus());
        assertEquals("User has opted out of this channel", history.getErrorMessage());
    }

    @Test
    void shouldMarkNotificationAsFailedWhenProviderThrowsException() {
        User user = user();
        UserPreference preference = enabledPreference(user);
        NotificationRequest request = request(Channel.EMAIL);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(preference));
        when(notificationRouter.getProvider(Channel.EMAIL)).thenReturn(emailProvider);
        doThrow(new RuntimeException("Email provider failed"))
                .when(emailProvider)
                .send(user, "Test Notification", "Hello from test");

        notificationService.sendNotification(request);

        NotificationHistory history = captureSavedHistory();
        assertEquals(Channel.EMAIL, history.getChannel());
        assertEquals(DeliveryStatus.FAILED, history.getStatus());
        assertEquals("Email provider failed", history.getErrorMessage());
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        NotificationRequest request = request(Channel.EMAIL);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> notificationService.sendNotification(request)
        );

        assertEquals("User not found: 1", exception.getMessage());
        verifyNoInteractions(userPreferenceRepository);
        verifyNoInteractions(notificationHistoryRepository);
        verifyNoInteractions(notificationRouter);
    }

    @Test
    void shouldProcessMultipleChannelsIndependently() {
        User user = user();
        UserPreference preference = enabledPreference(user);
        preference.setSmsEnabled(false);
        NotificationRequest request = request(Channel.EMAIL, Channel.SMS, Channel.PUSH);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(preference));
        when(notificationRouter.getProvider(Channel.EMAIL)).thenReturn(emailProvider);
        when(notificationRouter.getProvider(Channel.PUSH)).thenReturn(pushProvider);

        notificationService.sendNotification(request);

        verify(emailProvider).send(user, "Test Notification", "Hello from test");
        verify(notificationRouter, never()).getProvider(Channel.SMS);
        verifyNoInteractions(smsProvider);
        verify(pushProvider).send(user, "Test Notification", "Hello from test");

        ArgumentCaptor<NotificationHistory> historyCaptor =
                ArgumentCaptor.forClass(NotificationHistory.class);
        verify(notificationHistoryRepository, times(3)).save(historyCaptor.capture());

        List<NotificationHistory> histories = historyCaptor.getAllValues();
        assertHistory(histories.get(0), Channel.EMAIL, DeliveryStatus.SUCCESS, null);
        assertHistory(
                histories.get(1),
                Channel.SMS,
                DeliveryStatus.SKIPPED,
                "User has opted out of this channel"
        );
        assertHistory(histories.get(2), Channel.PUSH, DeliveryStatus.SUCCESS, null);
    }

    private NotificationHistory captureSavedHistory() {
        ArgumentCaptor<NotificationHistory> historyCaptor =
                ArgumentCaptor.forClass(NotificationHistory.class);
        verify(notificationHistoryRepository).save(historyCaptor.capture());
        return historyCaptor.getValue();
    }

    private void assertHistory(
            NotificationHistory history,
            Channel channel,
            DeliveryStatus status,
            String errorMessage
    ) {
        assertEquals(channel, history.getChannel());
        assertEquals(status, history.getStatus());
        assertEquals(errorMessage, history.getErrorMessage());
    }

    private User user() {
        User user = new User("Test User", "test@example.com", "9999999999", "push-token");
        user.setId(1L);
        return user;
    }

    private UserPreference enabledPreference(User user) {
        UserPreference preference = new UserPreference(user);
        preference.setEmailEnabled(true);
        preference.setSmsEnabled(true);
        preference.setPushEnabled(true);
        preference.setInAppEnabled(true);
        return preference;
    }

    private NotificationRequest request(Channel... channels) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(1L);
        request.setTitle("Test Notification");
        request.setBody("Hello from test");
        request.setChannels(List.of(channels));
        return request;
    }
}
