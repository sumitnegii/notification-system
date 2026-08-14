package com.indigold.notification_system.service;

import com.indigold.notification_system.dto.CreateUserRequest;
import com.indigold.notification_system.dto.CreateUserResponse;
import com.indigold.notification_system.dto.UserResponse;
import com.indigold.notification_system.entity.User;
import com.indigold.notification_system.entity.UserPreference;
import com.indigold.notification_system.repository.UserPreferenceRepository;
import com.indigold.notification_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPreferenceRepository userPreferenceRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUserWithDefaultPreferences() {
        CreateUserRequest request = createUserRequest();
        User savedUser = user();

        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenReturn(savedUser);

        CreateUserResponse response = userService.createUser(request);

        assertEquals(1L, response.getUserId());
        assertEquals("User created successfully", response.getMessage());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("Rahul", userCaptor.getValue().getName());
        assertEquals("rahul@example.com", userCaptor.getValue().getEmail());
        assertEquals("+919888888888", userCaptor.getValue().getPhone());
        assertEquals("test-push-token", userCaptor.getValue().getPushToken());

        ArgumentCaptor<UserPreference> preferenceCaptor =
                ArgumentCaptor.forClass(UserPreference.class);
        verify(userPreferenceRepository).save(preferenceCaptor.capture());
        UserPreference preference = preferenceCaptor.getValue();
        assertEquals(savedUser, preference.getUser());
        assertTrue(preference.isEmailEnabled());
        assertTrue(preference.isSmsEnabled());
        assertTrue(preference.isPushEnabled());
        assertTrue(preference.isInAppEnabled());
    }

    @Test
    void shouldGetUserWithPreferences() {
        User user = user();
        UserPreference preference = new UserPreference(user);
        preference.setSmsEnabled(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userPreferenceRepository.findByUserId(1L))
                .thenReturn(Optional.of(preference));

        UserResponse response = userService.getUser(1L);

        assertEquals(1L, response.getId());
        assertEquals("Rahul", response.getName());
        assertEquals("rahul@example.com", response.getEmail());
        assertEquals("+919888888888", response.getPhone());
        assertEquals("test-push-token", response.getPushToken());
        assertTrue(response.getPreferences().isEmail());
        assertFalse(response.getPreferences().isSms());
        assertTrue(response.getPreferences().isPush());
        assertTrue(response.getPreferences().isInApp());
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUser(99L)
        );

        assertEquals("User not found: 99", exception.getMessage());
        verifyNoInteractions(userPreferenceRepository);
    }

    @Test
    void shouldThrowExceptionWhenPreferencesDoNotExist() {
        User user = user();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userPreferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUser(1L)
        );

        assertEquals("Preferences not found for user: 1", exception.getMessage());
    }

    private CreateUserRequest createUserRequest() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Rahul");
        request.setEmail("rahul@example.com");
        request.setPhone("+919888888888");
        request.setPushToken("test-push-token");
        return request;
    }

    private User user() {
        User user = new User(
                "Rahul",
                "rahul@example.com",
                "+919888888888",
                "test-push-token"
        );
        user.setId(1L);
        return user;
    }
}
