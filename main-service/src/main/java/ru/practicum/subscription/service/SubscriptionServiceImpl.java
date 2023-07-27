package ru.practicum.subscription.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.utils.EventState;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.subscription.dto.SubscriberShortDto;
import ru.practicum.subscription.dto.SubscriptionDto;
import ru.practicum.subscription.dto.SubscriptionShortDto;
import ru.practicum.subscription.mapper.SubscriptionMapper;
import ru.practicum.subscription.model.Subscription;
import ru.practicum.subscription.repository.SubscriptionRepository;
import ru.practicum.subscription.utils.SubscriptionStatus;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public SubscriptionDto addNewSubscriptionRequest(Long userId, Long userForSubscribeId) {
        log.info("Запрос от пользователя с id = {} на подписку событий от пользователя id = {}.", userId, userForSubscribeId);
        User user = checkUser(userId);
        User userForSubscribe = checkUser(userForSubscribeId);
        if (userId.equals(userForSubscribeId)) {
            log.warn("Пользователь не может подписаться на самого себя.");
            throw new BadRequestException("Пользователь не может подписаться на самого себя.");
        }
        if (subscriptionRepository.existsByUserIdAndUserForSubscribeId(userId, userForSubscribeId)) {
            log.warn("Нельзя добавить повторный запрос.");
            throw new ConflictException("Нельзя добавить повторный запрос.");
        }
        Subscription subscription = Subscription.builder()
                .user(user)
                .userForSubscribe(userForSubscribe)
                .status(SubscriptionStatus.WAITING)
                .build();
        log.info("Создан запрос от пользователя с id = {} на подписку событий от пользователя id = {}.", userId, userForSubscribeId);
        return SubscriptionMapper.INSTANCE.toSubscriptionDto(subscriptionRepository.save(subscription));
    }

    @Override
    public List<SubscriptionShortDto> getPrivateSubscriptions(Long userId, SubscriptionStatus status) {
        log.info("Получение списка собственных подписок пользователя с id = {}.", userId);
        checkUser(userId);
        List<Subscription> subscriptionList;
        if (status == null) {
            subscriptionList = subscriptionRepository.findAllByUserId(userId);
        } else {
            subscriptionList = subscriptionRepository.findAllByUserIdAndStatus(userId, status);
        }
        log.info("Получен список собственных подписок пользователя с id = {}.", userId);
        return subscriptionList.stream()
                .map(SubscriptionMapper.INSTANCE::toSubscriptionShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionDto getPrivateSubscriptionById(Long userId, Long subscriptionId) {
        log.info("Получение подписки с id = {} от пользователя с id = {}.", subscriptionId, userId);
        checkUser(userId);
        Subscription subscription = checkSubscription(subscriptionId);
        log.info("Получена подписка с id = {} от пользователя с id = {}.", subscriptionId, userId);
        return SubscriptionMapper.INSTANCE.toSubscriptionDto(subscription);
    }

    @Override
    public List<EventShortDto> getPrivateEventsByUserId(Long userId, Long userForSubscribeId, LocalDateTime rangeStart,
                                                        LocalDateTime rangeEnd, Integer from, Integer size) {
        log.info("Получение подписчиком с id = {} списка событий пользователя с id = {}.", userId, userForSubscribeId);
        checkUser(userId);
        checkUser(userForSubscribeId);
        validateDates(rangeStart, rangeEnd);
        if (userId.equals(userForSubscribeId)) {
            log.warn("Пользователь не может подписаться на самого себя.");
            throw new BadRequestException("Пользователь не может подписаться на самого себя.");
        }
        List<Event> events;
        Subscription subscription = subscriptionRepository.findByUserIdAndUserForSubscribeId(userId, userForSubscribeId);
        if (subscription == null) {
            return Collections.emptyList();
        } else {
            if (!subscription.getStatus().equals(SubscriptionStatus.CONFIRMED)) {
                log.info("Нельзя получить список событий, подписка еще не подтверждена организатором.");
                throw new BadRequestException("Нельзя получить список событий, подписка еще не подтверждена организатором.");
            }
            PageRequest page = PageRequest.of(from, size);
            events = eventRepository.findSubscriptionEvents(userForSubscribeId, EventState.PUBLISHED, getRangeStart(rangeStart), page);
            if (rangeEnd != null) {
                events = getEventsBeforeRangeEnd(events, rangeEnd);
            }
        }
        return events.stream().map(EventMapper.INSTANCE::toEventShortDto).collect(Collectors.toList());
    }

    @Override
    public List<SubscriberShortDto> getPrivateSubscribers(Long userId) {
        log.info("Получение списка подписок на пользователя с id = {}", userId);
        checkUser(userId);
        log.info("Получен список подписок на пользователя с id = {}", userId);
        return subscriptionRepository.findAllByUserForSubscribeId(userId).stream()
                .map(SubscriptionMapper.INSTANCE::toSubscriberShortDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SubscriberShortDto updatePrivateSubscriptionRequest(Long userId, Long subscriptionId, SubscriptionStatus status) {
        log.info("Изменение статуса {} подписки с id = {} от пользователя с id = {}.", userId, status, subscriptionId);
        checkUser(userId);
        Subscription subscription = checkSubscription(subscriptionId);
        if (!userId.equals(subscription.getUserForSubscribe().getId())) {
            log.info("Пользователь с id = {} не найден в заявке на подписку.", userId);
            throw new ConflictException(String.format("Пользователь с id = %d не найден в заявке на подписку.", userId));
        }
        if (status.equals(subscription.getStatus())) {
            log.info("У заявки id = {} уже установлен текущий статус.", subscriptionId);
            throw new ConflictException(String.format("У заявки id = %d уже установлен текущий статус.", subscriptionId));
        }
        subscription.setStatus(status);
        log.info("Статус подписки с id = {} от пользователя с id = {} изменен на {}", subscriptionId, userId, status);
        return SubscriptionMapper.INSTANCE.toSubscriberShortDto(subscriptionRepository.save(subscription));
    }

    @Override
    @Transactional
    public void deletePrivateSubscriptionRequest(Long userId, Long subscriptionId) {
        log.info("Удаление от пользователя с id = {} подписки с id = {}.", userId, subscriptionId);
        checkUser(userId);
        Subscription subscription = checkSubscription(subscriptionId);
        if (!userId.equals(subscription.getUser().getId())) {
            throw new ConflictException("Можно удалять только собственные подписки.");
        }
        log.info("Подписка с id = {} удалена пользователем с id = {}.", subscriptionId, userId);
        subscriptionRepository.deleteById(subscriptionId);
    }


    private User checkUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
                    log.warn("Пользователь с id = {} не найден.", userId);
                    return new NotFoundException(String.format("Пользователь с id %d не найден.", userId));
                }
        );
    }

    private Subscription checkSubscription(Long subscriptionId) {
        return subscriptionRepository.findById(subscriptionId).orElseThrow(() -> {
                    log.warn("Подписка с id = {} не найдена.", subscriptionId);
                    return new NotFoundException(String.format("Подписка с id %d не найдена.", subscriptionId));
                }
        );
    }

    private void validateDates(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null) {
            if (rangeEnd.isBefore(rangeStart)) {
                throw new BadRequestException("rangeEnd не может быть раньше rangeStart.");
            }
        }
    }

    private LocalDateTime getRangeStart(LocalDateTime rangeStart) {
        if (rangeStart == null) return LocalDateTime.now();
        return rangeStart;
    }

    private List<Event> getEventsBeforeRangeEnd(List<Event> events, LocalDateTime rangeEnd) {
        return events.stream().filter(event -> event.getEventDate().isBefore(rangeEnd)).collect(Collectors.toList());
    }
}
