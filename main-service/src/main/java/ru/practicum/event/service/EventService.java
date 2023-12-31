package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.event.utils.EventState;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventShortDto> getPrivateEvents(Long userId, Integer from, Integer size);

    EventFullDto savePrivateEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getPrivateEventById(Long userId, Long eventId);

    EventFullDto updatePrivateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<EventFullDto> getAdminEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                      LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                        Integer size, HttpServletRequest request);

    EventFullDto getPublicEventById(Long id, HttpServletRequest request);
}
