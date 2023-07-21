package ru.practicum.main.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.UpdateEventAdminRequest;
import ru.practicum.main.event.service.EventService;
import ru.practicum.main.event.utils.EventState;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class EventAdminController {

    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getAdminEvents(@RequestParam(required = false) List<Long> users,
                                             @RequestParam(required = false) List<EventState> states,
                                             @RequestParam(required = false) List<Long> categories,
                                             @RequestParam(required = false) @DateTimeFormat(
                                                     pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                             @RequestParam(required = false) @DateTimeFormat(
                                                     pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                             @RequestParam(defaultValue = "0") @Positive Integer from,
                                             @RequestParam(defaultValue = "10") @PositiveOrZero Integer size) {
        log.info("Запрос на получение информации о событиях с параметрами: users {}, states {}, categories {}, " +
                "rangeStart {}, rangeEnd {}, from {}, size {}", users, states, categories, rangeStart, rangeEnd, from, size);
        return eventService.getAdminEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateAdminEvent(@PathVariable Long eventId,
                                         @RequestBody @Valid UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Получение запроса на редактирование администратором события с id = {}", eventId);
        return eventService.updateAdminEvent(eventId, updateEventAdminRequest);
    }
}
