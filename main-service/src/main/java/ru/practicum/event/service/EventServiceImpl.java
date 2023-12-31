package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsClient;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatDto;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.utils.EventSort;
import ru.practicum.event.utils.EventState;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

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
        log.info("Получение списка событий пользователя с id = {}, from {}, size {}", userId, from, size);
        checkUser(userId);
        PageRequest page = PageRequest.of(from, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, page);
        return events.stream()
                .map(EventMapper.INSTANCE::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto savePrivateEvent(Long userId, NewEventDto newEventDto) {
        log.info("Добавление события от пользователя с id = {}", userId);
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            log.warn("Событие не должно начинаться раньше, чем через 2 часа от текущего времени.");
            throw new BadRequestException("Field: eventDate. Error: Событие не должно начинаться раньше," +
                    " чем через 2 часа от текущего времени.");
        }
        User user = checkUser(userId);
        Category category = checkCategory(newEventDto.getCategory());
        EventState state = EventState.PENDING;
        Long confirmedRequests = 0L;
        Event event = eventRepository.save(EventMapper.INSTANCE.toEvent(newEventDto, category,
                user, state, confirmedRequests));
        log.info("Событие сохранено.");
        return EventMapper.INSTANCE.toEventFullDto(event);
    }

    @Override
    public EventFullDto getPrivateEventById(Long userId, Long eventId) {
        log.info("Получение события с id = {} пользователя с id = {}", eventId, userId);
        checkUser(userId);
        Event event = checkEvent(eventId);
        return EventMapper.INSTANCE.toEventFullDto(event);
    }

    @Transactional
    @Override
    public EventFullDto updatePrivateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        log.info("Изменение данных события с id = {} от пользователя с id = {}", eventId, userId);
        checkUser(userId);
        Event event = checkEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            log.warn("Изменить данные события может только организатор.");
            throw new ConflictException("Изменить данные события может только организатор.");
        }

        Optional.ofNullable(updateEventUserRequest.getEventDate()).ifPresent(eventDate -> {
            if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                log.warn("Событие не должно начинаться раньше, чем через 2 часа от текущего времени.");
                throw new BadRequestException("Событие не должно начинаться раньше," +
                        " чем через 2 часа от текущего времени.");
            }
            event.setEventDate(eventDate);
        });

        if (event.getState().equals(EventState.PUBLISHED)) {
            log.warn("Нельзя изменить опубликованное событие.");
            throw new ConflictException("Нельзя изменить опубликованное событие.");
        }

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

        if (updateEventUserRequest.getStateAction() != null) {
            switch (updateEventUserRequest.getStateAction()) {
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
                default:
                    log.warn("Указан неверный статус.");
                    throw new ConflictException("Указан неверный статус");
            }
        }

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
        List<Event> events = eventRepository.findAdminEvents(
                users, states, categories, getRangeStart(rangeStart), page);

        if (rangeEnd != null) {
            events = getEventsBeforeRangeEnd(events, rangeEnd);
        }

        return events.stream().map(EventMapper.INSTANCE::toEventFullDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Редактирование администратором события с id = {}", eventId);
        Event event = checkEvent(eventId);

        Optional.ofNullable(updateEventAdminRequest.getEventDate()).ifPresent(eventDate -> {
            if (eventDate.isBefore(LocalDateTime.now())) {
                log.warn("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации.");
                throw new BadRequestException("дата начала изменяемого " +
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

        if (updateEventAdminRequest.getStateAction() != null) {
            switch (updateEventAdminRequest.getStateAction()) {
                case REJECT_EVENT:
                    if (event.getState().equals(EventState.PUBLISHED)) {
                        log.warn("Нельзя отклонить опубликованное событие.");
                        throw new ConflictException("Нельзя отклонить опубликованное событие.");
                    }
                    event.setState(EventState.CANCELED);
                    break;
                case PUBLISH_EVENT:
                    if (event.getState() != (EventState.PENDING)) {
                        log.warn("Событие должно быть в состоянии ожидания публикации.");
                        throw new ConflictException("Событие должно быть в состоянии ожидания публикации.");
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                default:
                    log.warn("Указан неверный статус.");
                    throw new ConflictException("Указан неверный статус");
            }
        }

        Optional.ofNullable(updateEventAdminRequest.getTitle()).ifPresent(event::setTitle);

        log.info("Событие с id = {} обновлено администратором.", eventId);
        return EventMapper.INSTANCE.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                               String sort, Integer from, Integer size, HttpServletRequest request) {
        validateDates(rangeStart, rangeEnd);
        log.info("Получение событий с возможностью фильтрации.");
        LocalDateTime now = LocalDateTime.now();
        EventState state = EventState.PUBLISHED;
        String sorting;

        if (sort.equals(EventSort.EVENT_DATE.name())) {
            sorting = "eventDate";
        } else if (sort.equals(EventSort.VIEWS.name())) {
            sorting = "views";
        } else {
            sorting = "id";
        }

        PageRequest page = PageRequest.of(from, size, Sort.by(sorting));
        List<Event> sortedEvents = eventRepository.getEventsSort(text, state, categories, paid,
                getRangeStart(rangeStart), page);

        if (rangeEnd == null) rangeEnd = now.plusYears(2);

        sortedEvents = getEventsBeforeRangeEnd(sortedEvents, rangeEnd);

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
        if (event.getState() == null || !event.getState().equals(EventState.PUBLISHED)) {
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
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
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
