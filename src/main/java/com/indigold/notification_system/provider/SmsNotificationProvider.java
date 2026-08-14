package com.indigold.notification_system.provider;

import com.indigold.notification_system.entity.User;
import com.indigold.notification_system.enums.Channel;
import org.springframework.stereotype.Component;

@Component
public class SmsNotificationProvider
        implements NotificationProvider {

    @Override
    public Channel getChannel() {
        return Channel.SMS;
    }

    @Override
    public void send(User user, String title, String body) {

        System.out.println("Sending SMS to: " + user.getPhone());

        System.out.println("Message: " + body);
    }
}