package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HitDto {
    @NotBlank(message = "app не может быть пустым")
    private String app;

    @NotBlank(message = "uri не может быть пустым")
    private String uri;

    @NotBlank(message = "ip не может быть пустым")
    private String ip;

    @NotBlank(message = "timestamp не может быть пустым")
    private String timestamp;
}
