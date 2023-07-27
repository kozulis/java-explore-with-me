package ru.practicum.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.subscription.utils.SubscriptionStatus;
import ru.practicum.user.dto.UserShortDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriberShortDto {
    private Long id;
    private UserShortDto user;
    private SubscriptionStatus status;
}

