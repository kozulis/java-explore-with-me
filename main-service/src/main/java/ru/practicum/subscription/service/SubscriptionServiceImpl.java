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
    public SubscriptionDto addNewSubscriptionRequest(Long initiatorId, Long userForSubscribeId) {
        log.info("Запрос от пользователя с id = {} на подписку событий от пользователя id = {}.", initiatorId, userForSubscribeId);
        User user = checkUser(initiatorId);
        User userForSubscribe = checkUser(userForSubscribeId);
        if (initiatorId.equals(userForSubscribeId)) {
            log.warn("Пользователь не может подписаться на самого себя.");
            throw new ConflictException("Пользователь не может подписаться на самого себя.");
        }
        if (subscriptionRepository.existsByInitiatorIdAndUserForSubscribeId(initiatorId, userForSubscribeId)) {
            log.warn("Нельзя добавить повторный запрос.");
            throw new ConflictException("Нельзя добавить повторный запрос.");
        }
        Subscription subscription = Subscription.builder()
                .initiator(user)
                .userForSubscribe(userForSubscribe)
                .status(SubscriptionStatus.WAITING)
                .build();
        log.info("Создан запрос от пользователя с id = {} на подписку событий от пользователя id = {}.", initiatorId, userForSubscribeId);
        return SubscriptionMapper.INSTANCE.toSubscriptionDto(subscriptionRepository.save(subscription));
    }

    @Override
    public List<SubscriptionShortDto> getPrivateSubscriptions(Long initiatorId, SubscriptionStatus status) {
        log.info("Получение списка собственных подписок пользователя с id = {}.", initiatorId);
        checkUser(initiatorId);
        List<Subscription> subscriptionList;
        if (status == null) {
            subscriptionList = subscriptionRepository.findAllByInitiatorId(initiatorId);
        } else {
            subscriptionList = subscriptionRepository.findAllByInitiatorIdAndStatus(initiatorId, status);
        }
        log.info("Получен список собственных подписок пользователя с id = {}.", initiatorId);
        return subscriptionList.stream()
                .map(SubscriptionMapper.INSTANCE::toSubscriptionShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionDto getPrivateSubscriptionById(Long initiatorId, Long subscriptionId) {
        log.info("Получение подписки с id = {} от пользователя с id = {}.", subscriptionId, initiatorId);
        checkUser(initiatorId);
        Subscription subscription = checkSubscription(subscriptionId);
        log.info("Получена подписка с id = {} от пользователя с id = {}.", subscriptionId, initiatorId);
        return SubscriptionMapper.INSTANCE.toSubscriptionDto(subscription);
    }

    @Override
    public List<EventShortDto> getPrivateEventsByUserId(Long initiatorId, Long userForSubscribeId, LocalDateTime rangeStart,
                                                        LocalDateTime rangeEnd, Integer from, Integer size) {
        log.info("Получение подписчиком с id = {} списка событий пользователя с id = {}.", initiatorId, userForSubscribeId);
        checkUser(initiatorId);
        checkUser(userForSubscribeId);
        validateDates(rangeStart, rangeEnd);
        if (initiatorId.equals(userForSubscribeId)) {
            log.warn("Нельзя получить список собственных событий.");
            throw new ConflictException("Нельзя получить список собственных событий.");
        }
        List<Event> events;
        Subscription subscription = subscriptionRepository.findByInitiatorIdAndUserForSubscribeId(initiatorId, userForSubscribeId);
        if (subscription == null) {
            log.info("Подписка на пользователя с id = {} не найдена.", userForSubscribeId);
            throw new NotFoundException(String.format("Подписка на пользователя с id = %d не найдена.", userForSubscribeId));
        }
        if (!subscription.getStatus().equals(SubscriptionStatus.CONFIRMED)) {
            log.info("Нельзя получить список событий, подписка еще не подтверждена организатором.");
            throw new BadRequestException("Нельзя получить список событий, подписка еще не подтверждена организатором.");
        }
        PageRequest page = PageRequest.of(from, size);
        events = eventRepository.findSubscriptionEvents(userForSubscribeId, EventState.PUBLISHED, getRangeStart(rangeStart), page);
        if (rangeEnd != null) {
            events = getEventsBeforeRangeEnd(events, rangeEnd);
        }
        return events.stream().map(EventMapper.INSTANCE::toEventShortDto).collect(Collectors.toList());
    }

    @Override
    public List<SubscriberShortDto> getPrivateSubscribers(Long initiatorId) {
        log.info("Получение списка подписок на пользователя с id = {}", initiatorId);
        checkUser(initiatorId);
        log.info("Получен список подписок на пользователя с id = {}", initiatorId);
        return subscriptionRepository.findAllByUserForSubscribeId(initiatorId).stream()
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
    public void deletePrivateSubscriptionRequest(Long initiatorId, Long subscriptionId) {
        log.info("Удаление от пользователя с id = {} подписки с id = {}.", initiatorId, subscriptionId);
        checkUser(initiatorId);
        Subscription subscription = checkSubscription(subscriptionId);
        if (!initiatorId.equals(subscription.getInitiator().getId())) {
            throw new ConflictException("Можно удалять только собственные подписки.");
        }
        log.info("Подписка с id = {} удалена пользователем с id = {}.", subscriptionId, initiatorId);
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
