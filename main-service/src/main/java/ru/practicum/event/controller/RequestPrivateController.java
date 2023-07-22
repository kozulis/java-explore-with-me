package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.ParticipationRequestDto;
import ru.practicum.event.service.RequestService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/requests")
public class RequestPrivateController {

    private final RequestService requestService;

    @GetMapping
    public List<ParticipationRequestDto> getPrivateEventRequests(@PathVariable Long userId) {
        log.info("Запрос на получение заявок от пользователя с id = {} на участие в чужих событиях.", userId);
        return requestService.getPrivateEventRequests(userId);
    }

    @PostMapping
    public ParticipationRequestDto addPrivateEventRequest(@PathVariable Long userId, @RequestParam Long eventId) {
        log.info("Запрос от пользователя с id = {} на добавление заявки на участие в событии с id = {}", userId, eventId);
        return requestService.addPrivateEventRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelPrivateEventRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        log.info("Запрос от пользователя с id = {} на отмену запроса с id = {} на участие в событии.", userId, requestId);
        return requestService.cancelPrivateEventRequest(userId, requestId);
    }
}
