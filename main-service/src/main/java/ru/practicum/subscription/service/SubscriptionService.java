package ru.practicum.subscription.service;

import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.subscription.dto.SubscriberShortDto;
import ru.practicum.subscription.dto.SubscriptionDto;
import ru.practicum.subscription.dto.SubscriptionShortDto;
import ru.practicum.subscription.utils.SubscriptionStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface SubscriptionService {

    SubscriptionDto addNewSubscriptionRequest(Long userId, Long userForSubscribeId);

    List<SubscriptionShortDto> getPrivateSubscriptions(Long userId, SubscriptionStatus status);

    SubscriptionDto getPrivateSubscriptionById(Long userId, Long subscriptionId);

    List<EventShortDto> getPrivateEventsByUserId(Long userId, Long userForSubscribeId, LocalDateTime rangeStart,
                                                 LocalDateTime rangeEnd, Integer from, Integer size);

    List<SubscriberShortDto> getPrivateSubscribers(Long userId);

    SubscriberShortDto updatePrivateSubscriptionRequest(Long userId, Long subscriptionId, SubscriptionStatus status);

    void deletePrivateSubscriptionRequest(Long userId, Long subscriptionId);

}
