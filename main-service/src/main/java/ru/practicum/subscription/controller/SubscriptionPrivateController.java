package ru.practicum.subscription.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.subscription.dto.SubscriberShortDto;
import ru.practicum.subscription.dto.SubscriptionDto;
import ru.practicum.subscription.dto.SubscriptionShortDto;
import ru.practicum.subscription.service.SubscriptionService;
import ru.practicum.subscription.utils.SubscriptionStatus;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping("/users/{userId}/subscription")
public class SubscriptionPrivateController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionDto addNewSubscriptionRequest(@PathVariable Long userId, @RequestParam Long userForSubscribeId) {
        log.info("Запрос от пользователя с id = {} на подписку событий от пользователя id = {}.", userId, userForSubscribeId);
        return subscriptionService.addNewSubscriptionRequest(userId, userForSubscribeId);
    }

    @GetMapping
    public List<SubscriptionShortDto> getPrivateSubscriptions(@PathVariable Long userId,
                                                              @RequestParam(required = false) SubscriptionStatus status) {
        log.info("Запрос на получение списка собственных подписок пользователя с id = {} .", userId);
        return subscriptionService.getPrivateSubscriptions(userId, status);
    }

    @GetMapping("/{subscriptionId}")
    public SubscriptionDto getPrivateSubscriptionById(@PathVariable Long userId, @PathVariable Long subscriptionId) {
        log.info("Запрос от пользователя с id = {} на получение подписки с id = {}.", userId, subscriptionId);
        return subscriptionService.getPrivateSubscriptionById(userId, subscriptionId);
    }

    @GetMapping("/event")
    public List<EventShortDto> getPrivateEventsByUserId(@PathVariable Long userId,
                                                        @RequestParam Long userForSubscribeId,
                                                        @RequestParam(required = false) @DateTimeFormat(
                                                                pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                        @RequestParam(required = false) @DateTimeFormat(
                                                                pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                        @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                        @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Запрос от пользователя с id = {} " +
                "на получение списка событий пользователя с id = {}.", userId, userId);
        return subscriptionService.getPrivateEventsByUserId(userId, userForSubscribeId, rangeStart, rangeEnd, from, size);
    }

    @GetMapping("/subscribers")
    public List<SubscriberShortDto> getPrivateSubscribers(@PathVariable(name = "userId") Long userId) {
        log.info("Запрос на получение списка подписок на пользователя с id = {}.", userId);
        return subscriptionService.getPrivateSubscribers(userId);
    }

    @PatchMapping("/{subscriptionId}")
    public SubscriberShortDto updatePrivateSubscriptionRequest(@PathVariable Long userId,
                                                               @PathVariable Long subscriptionId,
                                                               @RequestParam SubscriptionStatus status) {
        log.info("Запрос от пользователя с id = {} на изменение статуса {} подписки с id = {}.", userId, status, subscriptionId);
        return subscriptionService.updatePrivateSubscriptionRequest(userId, subscriptionId, status);
    }

    @DeleteMapping("/{subscriptionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePrivateSubscriptionRequest(@PathVariable Long userId, @PathVariable Long subscriptionId) {
        log.info("Запрос от пользователя с id = {} на удаление подписки с id = {}.", userId, subscriptionId);
        subscriptionService.deletePrivateSubscriptionRequest(userId, subscriptionId);
    }

}
