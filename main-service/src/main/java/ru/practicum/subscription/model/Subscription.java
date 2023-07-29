package ru.practicum.subscription.model;

import lombok.*;
import ru.practicum.subscription.utils.SubscriptionStatus;
import ru.practicum.user.model.User;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "subscriptions")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne/*(fetch = FetchType.LAZY)*/
    @JoinColumn(name = "initiator")
    private User initiator;

    @ManyToOne/*(fetch = FetchType.LAZY)*/
    @JoinColumn(name = "user_for_subscribe")
    private User userForSubscribe;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;
}
