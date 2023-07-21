package ru.practicum.main.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatDto;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.category.repository.CategoryRepository;
import ru.practicum.main.event.dto.*;
import ru.practicum.main.event.mapper.EventMapper;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.event.utils.EventSort;
import ru.practicum.main.event.utils.EventState;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StatsClient statsClient;

    @Override
    public List<EventShortDto> getPrivateEvents(Long userId, Integer from, Integer size) {
        checkUser(userId);
        PageRequest page = PageRequest.of(from, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, page);
        log.info("Получение списка событий пользователя с id = {}, from {}, size {}", userId, from, size);
        return events.stream()
                .map(EventMapper.INSTANCE::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto savePrivateEvent(Long userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            log.warn("Событие не должно начинаться раньше, чем через 2 часа от текущего времени.");
            throw new ConflictException("Field: eventDate. Error: Событие не должно начинаться раньше," +
                    " чем через 2 часа от текущего времени.");
        }
        User user = checkUser(userId);
        Category category = checkCategory(newEventDto.getCategory());
        Event event = EventMapper.INSTANCE.toEvent(newEventDto, category, user);
        log.info("Событие сохранено.");
        return EventMapper.INSTANCE.toEventFullDto(eventRepository.save((event)));
    }

    @Override
    public EventFullDto getPrivateEventById(Long userId, Long eventId) {
        checkUser(userId);
        Event event = checkEvent(eventId);
        log.info("Получение события с id = {} пользователя с id = {}", eventId, userId);
        return EventMapper.INSTANCE.toEventFullDto(event);
    }

    @Transactional
    @Override
    public EventFullDto updatePrivateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        checkUser(userId);
        Event event = checkEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            log.warn("Изменить данные события может только организатор.");
            throw new ConflictException("Изменить данные события может только организатор.");
        }

        Optional.ofNullable(updateEventUserRequest.getEventDate()).ifPresent(eventDate -> {
            if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                log.warn("Событие не должно начинаться раньше, чем через 2 часа от текущего времени.");
                throw new ConflictException("Событие не должно начинаться раньше," +
                        " чем через 2 часа от текущего времени.");
            }
            event.setEventDate(eventDate);
        });

        Optional.ofNullable(updateEventUserRequest.getAnnotation()).ifPresent(event::setAnnotation);

        Optional.ofNullable(updateEventUserRequest.getCategory()).ifPresent(catId -> {
            Category category = checkCategory(catId);
            event.setCategory(category);
        });

        Optional.ofNullable(updateEventUserRequest.getDescription()).ifPresent(event::setDescription);

        Optional.ofNullable(updateEventUserRequest.getLocation()).ifPresent(location -> {
            event.setLat(location.getLat());
            event.setLon(location.getLon());
        });

        Optional.ofNullable(updateEventUserRequest.getPaid()).ifPresent(event::setPaid);

        Optional.ofNullable(updateEventUserRequest.getParticipantLimit()).ifPresent(event::setParticipantLimit);

        Optional.ofNullable(updateEventUserRequest.getRequestModeration()).ifPresent(event::setRequestModeration);

        Optional.ofNullable(updateEventUserRequest.getStateAction()).ifPresent(userStateAction -> {
            switch (userStateAction) {
                case CANCEL_REVIEW:
                    if (event.getState().equals(EventState.PUBLISHED)) {
                        log.warn("Нельзя изменить опубликованное событие.");
                        throw new ConflictException("Нельзя изменить опубликованное событие.");
                    }
                    event.setState(EventState.CANCELED);
                    break;
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
                default:
                    log.warn("Указан неверный статус.");
                    throw new ConflictException("Указан неверный статус");
            }
        });

        Optional.ofNullable(updateEventUserRequest.getTitle()).ifPresent(event::setTitle);

        log.info("Событие с id = {} обновлено пользователем с id = {}.", eventId, userId);
        return EventMapper.INSTANCE.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventFullDto> getAdminEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {

        log.info("Получение информации о событиях с параметрами: users {}, states {}, categories {}, " +
                "rangeStart {}, rangeEnd {}, from {}, size {}", users, states, categories, rangeStart, rangeEnd, from, size);
        validateDates(rangeStart, rangeEnd);
        PageRequest page = PageRequest.of(from, size);
        //TODO Рабочий ли запрос(см репозиторий)?LM
        List<Event> events = eventRepository.findAllByInitiatorIdInAndStateInAndCategoryIdInAndEventDateIsAfter(
                users, states, categories, getRangeStart(rangeStart), page);

        if (rangeEnd != null) {
            events = getEventsBeforeRangeEnd(events, rangeEnd);
        }

        return events.stream().map(EventMapper.INSTANCE::toEventFullDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = checkEvent(eventId);

        Optional.ofNullable(updateEventAdminRequest.getEventDate()).ifPresent(eventDate -> {
            if (eventDate.isBefore(event.getPublishedOn().minusHours(1))) {
                log.warn("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации.");
                throw new ConflictException("дата начала изменяемого " +
                        "события должна быть не ранее чем за час от даты публикации");
            }
            event.setEventDate(eventDate);
        });

        Optional.ofNullable(updateEventAdminRequest.getAnnotation()).ifPresent(event::setAnnotation);

        Optional.ofNullable(updateEventAdminRequest.getCategory()).ifPresent(catId -> {
            Category category = checkCategory(catId);
            event.setCategory(category);
        });

        Optional.ofNullable(updateEventAdminRequest.getDescription()).ifPresent(event::setDescription);

        Optional.ofNullable(updateEventAdminRequest.getLocation()).ifPresent(location -> {
            event.setLat(location.getLat());
            event.setLon(location.getLon());
        });

        Optional.ofNullable(updateEventAdminRequest.getPaid()).ifPresent(event::setPaid);

        Optional.ofNullable(updateEventAdminRequest.getParticipantLimit()).ifPresent(event::setParticipantLimit);

        Optional.ofNullable(updateEventAdminRequest.getRequestModeration()).ifPresent(event::setRequestModeration);


        Optional.ofNullable(updateEventAdminRequest.getStateAction()).ifPresent(adminStateAction -> {
            switch (adminStateAction) {
                case REJECT_EVENT:
                    if (event.getState().equals(EventState.PUBLISHED)) {
                        log.warn("Нельзя отклонить опубликованное событие.");
                        throw new ConflictException("Нельзя отклонить опубликованное событие.");
                    }
                    event.setState(EventState.CANCELED);
                    break;
                case PUBLISH_EVENT:
                    if (!event.getState().equals(EventState.PENDING)) {
                        log.warn("Нельзя изменить событие, если оно в состоянии ожидания публикации.");
                        throw new ConflictException("Нельзя изменить событие, если оно в состоянии ожидания публикации.");
                    }
                    event.setState(EventState.PENDING);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                default:
                    log.warn("Указан неверный статус.");
                    throw new ConflictException("Указан неверный статус");
            }
        });

        Optional.ofNullable(updateEventAdminRequest.getTitle()).ifPresent(event::setTitle);

        log.info("Событие с id = {} обновлено администратором.", eventId);
        return EventMapper.INSTANCE.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                               EventSort sort, Integer from, Integer size, HttpServletRequest request) {
        validateDates(rangeStart, rangeEnd);
        log.info("Получение событий с возможностью фильтрации.");
        LocalDateTime now = LocalDateTime.now();
        if (rangeEnd == null) rangeEnd = now.plusYears(1);

        String sorting;
        if (sort.equals(EventSort.EVENT_DATE)) {
            sorting = "eventDate";
        } else if (sort.equals(EventSort.VIEWS)) {
            sorting = "views";
        } else {
            sorting = "id";
        }

        PageRequest page = PageRequest.of(from, size, Sort.by(sorting));
        List<Event> sortedEvents = eventRepository.getEventsSort(text, categories, paid,
                getRangeStart(rangeStart), rangeEnd, page);
        if (onlyAvailable) {
            sortedEvents.removeIf(event -> event.getParticipantLimit().equals(Math.toIntExact(event.getConfirmedRequests())));
        }
        if (sortedEvents.isEmpty()) return Collections.emptyList();
        String uri = request.getRequestURI();
        LocalDateTime startDate = sortedEvents.stream().map(Event::getCreatedOn).min(Comparator.naturalOrder()).orElse(now);
        saveStats(request);
        Long views = getViews(uri, startDate, now);
        sortedEvents.forEach(event -> event.setViews(views));
        return sortedEvents.stream()
                .map(EventMapper.INSTANCE::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getPublicEventById(Long id, HttpServletRequest request) {
        log.info("Получение подробной информации об опубликованном событии с id = {}", id);
        Event event = checkEvent(id);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            log.warn("Нельзя получить информацию о событии, которое не опубликовано.");
            throw new NotFoundException("Нельзя получить информацию о событии, которое не опубликовано.");
        }
        String uri = request.getRequestURI();
        saveStats(request);
        Long views = getViews(uri, event.getCreatedOn(), LocalDateTime.now());
        event.setViews(views);
        return EventMapper.INSTANCE.toEventFullDto(event);
    }

    private void saveStats(HttpServletRequest request) {
        HitDto hitDto = HitDto.builder()
                .app("main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();
        statsClient.saveStats(hitDto);
    }

    private Long getViews(String uri, LocalDateTime from, LocalDateTime to) {
        return Optional.ofNullable(statsClient.getStats(from.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        to.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), List.of(uri), true))
                .map(ResponseEntity::getBody)
                .stream()
                .flatMap(Collection::stream)
                .filter(statDto -> statDto.getUri().equals(uri))
                .mapToLong(StatDto::getHits)
                .sum();
    }

    private User checkUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
                    log.warn("Пользователь с id = {} не найден.", userId);
                    return new NotFoundException(String.format("Пользователь с id %d не найден.", userId));
                }
        );
    }

    private Category checkCategory(Long catId) {
        return categoryRepository.findById(catId).orElseThrow(() -> {
                    log.warn("Категория с id = {} не найдена.", catId);
                    return new NotFoundException(String.format("Категория с id %d не найдена.", catId));
                }
        );
    }

    private Event checkEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> {
                    log.warn("Событие с id = {} не найдена.", eventId);
                    return new NotFoundException(String.format("Событие с id %d не найдено.", eventId));
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

    private List<Event> getEventsBeforeRangeEnd(List<Event> events, LocalDateTime rangeEnd) {
        return events.stream().filter(event -> event.getEventDate().isBefore(rangeEnd)).collect(Collectors.toList());
    }

    private LocalDateTime getRangeStart(LocalDateTime rangeStart) {
        if (rangeStart == null) return LocalDateTime.now();
        return rangeStart;
    }

}
