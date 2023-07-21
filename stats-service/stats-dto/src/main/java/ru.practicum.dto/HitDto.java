package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
