package com.indigold.notification_system.provider;

import com.indigold.notification_system.entity.User;
import com.indigold.notification_system.enums.Channel;
import org.springframework.stereotype.Component;

@Component
public class InAppNotificationProvider
        implements NotificationProvider {

    @Override
    public Channel getChannel() {
        return Channel.IN_APP;
    }

    @Override
    public void send(User user, String title, String body) {

        System.out.println("Creating IN-APP notification for user: " + user.getId());

        System.out.println("Title: " + title);

        System.out.println("Body: " + body);
    }
}