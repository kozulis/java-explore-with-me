package ru.practicum.subscription.service;

import ru.practicum.event.dto.EventShortDto;
import ru.practicum.subscription.dto.SubscriberShortDto;
import ru.practicum.subscription.dto.SubscriptionDto;
import ru.practicum.subscription.dto.SubscriptionShortDto;
import ru.practicum.subscription.utils.SubscriptionStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface SubscriptionService {

    SubscriptionDto addNewSubscriptionRequest(Long initiatorId, Long userForSubscribeId);

    List<SubscriptionShortDto> getPrivateSubscriptions(Long initiatorId, SubscriptionStatus status);

    SubscriptionDto getPrivateSubscriptionById(Long initiatorId, Long subscriptionId);

    List<EventShortDto> getPrivateEventsByUserId(Long initiatorId, Long userForSubscribeId, LocalDateTime rangeStart,
                                                 LocalDateTime rangeEnd, Integer from, Integer size);

    List<SubscriberShortDto> getPrivateSubscribers(Long initiatorId);

    SubscriberShortDto updatePrivateSubscriptionRequest(Long initiatorId, Long subscriptionId, SubscriptionStatus status);

    void deletePrivateSubscriptionRequest(Long initiatorId, Long subscriptionId);

}
