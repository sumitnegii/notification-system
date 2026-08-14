package com.indigold.notification_system.repository;

import com.indigold.notification_system.entity.User;
import com.indigold.notification_system.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPreferenceRepository
        extends JpaRepository<UserPreference, Long> {

    Optional<UserPreference> findByUser(User user);

    Optional<UserPreference> findByUserId(Long userId);
}


/*
*
*
test design---
notification request
       ↓
userId = 1
       ↓
find user
       ↓
find preferences for user 1
       ↓
check requested channels
       ↓
send / skip


* */