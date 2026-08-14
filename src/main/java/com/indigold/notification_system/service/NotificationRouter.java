package com.indigold.notification_system.service;

import com.indigold.notification_system.enums.Channel;
import com.indigold.notification_system.provider.NotificationProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NotificationRouter {

    private final Map<Channel, NotificationProvider> providers;

    public NotificationRouter(List<NotificationProvider> providerList ) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(
                        NotificationProvider::getChannel,
                        Function.identity()
                ));
    }

    public NotificationProvider getProvider(Channel channel) {

        NotificationProvider provider = providers.get(channel);

        if (provider == null) {
            throw new IllegalArgumentException(
                    "No provider found for channel: " + channel
            );
        }

        return provider;
    }
}