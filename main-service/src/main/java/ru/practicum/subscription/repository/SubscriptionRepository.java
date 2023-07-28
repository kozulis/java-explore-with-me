package ru.practicum.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.subscription.model.Subscription;
import ru.practicum.subscription.utils.SubscriptionStatus;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Boolean existsByInitiatorIdAndUserForSubscribeId(Long userId, Long userForSubscribeId);

    List<Subscription> findAllByInitiatorId(Long uerId);

    List<Subscription> findAllByInitiatorIdAndStatus(Long userId, SubscriptionStatus status);

    Subscription findByInitiatorIdAndUserForSubscribeId(Long userId, Long userForSubscribeId);

    List<Subscription> findAllByUserForSubscribeId(Long userId);

}
