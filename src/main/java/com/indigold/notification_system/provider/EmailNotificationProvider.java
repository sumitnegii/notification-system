package com.indigold.notification_system.provider;

import com.indigold.notification_system.entity.User;
import com.indigold.notification_system.enums.Channel;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationProvider
        implements NotificationProvider {

    @Override
    public Channel getChannel() {
        return Channel.EMAIL;
    }

    @Override
    public void send(User user, String title,  String body ) {

        // throw new RuntimeException("Email provider is currently unavailable"); //passed test

        System.out.println("Sending EMAIL to: " + user.getEmail());

        System.out.println("Subject: " + title);

        System.out.println("Body: " + body);
    }
}