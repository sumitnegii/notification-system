package com.indigold.notification_system.service;

import com.indigold.notification_system.dto.CreateUserRequest;
import com.indigold.notification_system.dto.CreateUserResponse;
import com.indigold.notification_system.dto.UserResponse;
import com.indigold.notification_system.entity.User;
import com.indigold.notification_system.entity.UserPreference;
import com.indigold.notification_system.repository.UserPreferenceRepository;
import com.indigold.notification_system.repository.UserRepository;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    public UserService(
            UserRepository userRepository,
            UserPreferenceRepository userPreferenceRepository
    ) {
        this.userRepository = userRepository;
        this.userPreferenceRepository = userPreferenceRepository;
    }

    public CreateUserResponse createUser(CreateUserRequest request) {
        User user = new User(
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                request.getPushToken()
        );

        User savedUser = userRepository.save(user);
        userPreferenceRepository.save(new UserPreference(savedUser));

        return new CreateUserResponse(
                savedUser.getId(),
                "User created successfully"
        );
    }

    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found: " + id)
                );

        UserPreference preference = userPreferenceRepository.findByUserId(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Preferences not found for user: " + id
                        )
                );

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getPushToken(),
                new UserResponse.PreferencesResponse(
                        preference.isEmailEnabled(),
                        preference.isSmsEnabled(),
                        preference.isPushEnabled(),
                        preference.isInAppEnabled()
                )
        );
    }

    public List<UserResponse> getAllUsers() {
    return userRepository.findAll()
            .stream()
            .map(user -> new UserResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getPushToken(), null
            ))
            .toList();
}

    
}
