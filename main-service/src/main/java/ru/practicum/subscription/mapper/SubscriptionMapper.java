package ru.practicum.subscription.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.subscription.dto.SubscriberShortDto;
import ru.practicum.subscription.dto.SubscriptionDto;
import ru.practicum.subscription.dto.SubscriptionShortDto;
import ru.practicum.subscription.model.Subscription;
import ru.practicum.user.mapper.UserMapper;

@Mapper(uses = {UserMapper.class, EventMapper.class})
public interface SubscriptionMapper {

    SubscriptionMapper INSTANCE = Mappers.getMapper(SubscriptionMapper.class);

    SubscriptionDto toSubscriptionDto(Subscription subscription);

    SubscriptionShortDto toSubscriptionShortDto(Subscription subscription);

    SubscriberShortDto toSubscriberShortDto(Subscription subscription);
}
