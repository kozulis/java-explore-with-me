package ru.practicum.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatDto;
import ru.practicum.server.service.StatService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class StatsController {
    private final StatService statService;

    @PostMapping("/hit")
    public ResponseEntity<String> saveStats(@Valid @RequestBody HitDto hitDto) {
        log.info("Запрос на сохранение информации {}", hitDto);
        statService.saveStats(hitDto);
        return new ResponseEntity<>("Информация сохранена", HttpStatus.CREATED);
    }


    @GetMapping("/stats")
    public List<StatDto> getStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("Запрос на получение статистики: start {}, end {}", start, end);
        return statService.getStats(start, end, uris, unique);
    }
}
