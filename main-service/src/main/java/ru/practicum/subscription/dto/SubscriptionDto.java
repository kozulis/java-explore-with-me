package ru.practicum.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.subscription.utils.SubscriptionStatus;
import ru.practicum.user.model.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionDto {
    private Long id;
    private User user;
    private User userForSubscribe;
    private SubscriptionStatus status;
}
