package ru.practicum.main.event.service;

import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.EventShortDto;
import ru.practicum.main.event.dto.NewEventDto;
import ru.practicum.main.event.dto.UpdateEventUserRequest;

import java.util.List;

public interface EventService {

    List<EventShortDto> getPrivateEvents(Long userId, Integer from, Integer size);

    EventFullDto savePrivateEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getPrivateEventById(Long userId, Long eventId);

    EventFullDto updatePrivateEventById(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);
}
