package ru.practicum.main.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.category.repository.CategoryRepository;
import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.EventShortDto;
import ru.practicum.main.event.dto.NewEventDto;
import ru.practicum.main.event.dto.UpdateEventUserRequest;
import ru.practicum.main.event.mapper.EventMapper;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.event.utils.EventState;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

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

    @Override
    public EventFullDto updatePrivateEventById(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        checkUser(userId);
        Event event = checkEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            log.warn("Изменить данные события может только организатор.");
            throw new ConflictException("Изменить данные события может только организатор.");
        }


        if (event.getState().equals(EventState.PUBLISHED)) {
            log.warn("Нельзя изменить опубликованное событие.");
            throw new ConflictException("Нельзя изменить опубликованное событие.");
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
                    event.setState(EventState.CANCELED);
                    break;
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
            }
        });

        Optional.ofNullable(updateEventUserRequest.getTitle()).ifPresent(event::setTitle);

        log.info("Событие с id = {} обновлено пользователем с id = {}.", eventId, userId);
        return EventMapper.INSTANCE.toEventFullDto(eventRepository.save(event));
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

}
