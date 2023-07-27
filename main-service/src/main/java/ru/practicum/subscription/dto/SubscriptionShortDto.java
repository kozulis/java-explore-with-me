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
public class SubscriptionShortDto {
    private Long id;
    private UserShortDto userForSubscribe;
    private SubscriptionStatus status;
}
