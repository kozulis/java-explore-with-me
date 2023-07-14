package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatDto;

import java.util.List;

@Slf4j
@Service
public class StatsClient {

    private final WebClient client;

    public StatsClient(@Value("${stats-server.url}") String serverUrl) {
        this.client = WebClient.create(serverUrl);
    }

    public void saveStats(HitDto hitDto) {
        this.client.post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(hitDto)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity()
                .doOnNext(voidResponseEntity -> log.info("Информация сохранена"))
                .block();
    }

    public ResponseEntity<List<StatDto>> getStats(String start, String end, List<String> uris, Boolean unique) {
        return this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stats")
                        .queryParam("start", start)
                        .queryParam("end", end)
                        .queryParam("uris", uris)
                        .queryParam("unique", unique)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntityList(StatDto.class)
                .doOnNext(listResponseEntity -> log.info(
                        "Запрос на получение статистики: start {}, end {}", start, end))
                .block();
    }

}
