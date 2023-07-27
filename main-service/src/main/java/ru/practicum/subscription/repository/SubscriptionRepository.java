package ru.practicum.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.subscription.model.Subscription;
import ru.practicum.subscription.utils.SubscriptionStatus;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Boolean existsByUserIdAndUserForSubscribeId(Long userId, Long userForSubscribeId);

    List<Subscription> findAllByUserId(Long uerId);

    List<Subscription> findAllByUserIdAndStatus(Long userId, SubscriptionStatus status);

    Subscription findByUserIdAndUserForSubscribeId(Long userId, Long userForSubscribeId);

    List<Subscription> findAllByUserForSubscribeId(Long userId);

}
