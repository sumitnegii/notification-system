package com.indigold.notification_system.provider;

import com.indigold.notification_system.entity.User;
import com.indigold.notification_system.enums.Channel;
import org.springframework.stereotype.Component;

@Component
public class PushNotificationProvider
        implements NotificationProvider {

    @Override
    public Channel getChannel() {
        return Channel.PUSH;
    }

    @Override
    public void send(User user, String title, String body ) {

        System.out.println("Sending PUSH to token: " + user.getPushToken());

        System.out.println("Title: " + title);

        System.out.println( "Body: " + body);
    }
}