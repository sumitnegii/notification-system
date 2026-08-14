package com.indigold.notification_system.provider;

import com.indigold.notification_system.entity.User;
import com.indigold.notification_system.enums.Channel;

public interface NotificationProvider {

    Channel getChannel();

    void send(
            User user,
            String title,
            String body
    );
}

// This is basically the Strategy Pattern. so that we can impelemt as many provider