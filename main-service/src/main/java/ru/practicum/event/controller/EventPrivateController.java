package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.service.EventService;
import ru.practicum.event.service.RequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping("/users/{userId}/events")
public class EventPrivateController {

    private final EventService eventService;
    private final RequestService requestService;

    @GetMapping
    public List<EventShortDto> getPrivateEvents(@PathVariable Long userId,
                                                @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Запрос на получение событий, добавленных пользователем с id = {}, from {}, size {}", userId, from, size);
        return eventService.getPrivateEvents(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addNewPrivateEvent(@PathVariable Long userId, @RequestBody @Valid NewEventDto newEventDto) {
        log.info("Запрос на добавление события от пользователя с id = {}", userId);
        return eventService.savePrivateEvent(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getPrivateEventById(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Запрос на получение данных события с id = {} от пользователя с id = {}", eventId, userId);
        return eventService.getPrivateEventById(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updatePrivateEventById(@PathVariable Long userId, @PathVariable Long eventId,
                                               @RequestBody @Valid UpdateEventUserRequest updateEventUserRequest) {
        log.info("Запрос на изменение данных события с id = {} от пользователя с id = {}", eventId, userId);
        return eventService.updatePrivateEvent(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Запрос на получение информации о запросах на участии в событии с id = {} " +
                "от пользователя с id = {}.", userId, eventId);
        return requestService.getEventRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateEventRequest(
            @PathVariable Long userId, @PathVariable Long eventId,
            @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        log.info("Запрос на изменение статуса заявок на участие в событии с id = {}" +
                " от пользователя с id = {}", eventId, userId);
        return requestService.updateEventRequest(userId, eventId, eventRequestStatusUpdateRequest);
    }

}
