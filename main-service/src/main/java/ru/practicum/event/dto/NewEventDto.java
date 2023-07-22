package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.event.model.Location;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewEventDto {

    @Size(min = 20, max = 2000, message = "Поле annotation должно содержать min 20 и max 2000 символов.")
    @NotBlank(message = "Поле annotation не должно быть пустым.")
    private String annotation;

    @NotBlank(message = "Поле category не должно быть пустым.")
    private Long category;

    @Size(min = 20, max = 7000, message = "Поле description должно содержать min 20 и max 7000 символов.")
    @NotBlank(message = "Поле description не должно быть пустым.")
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "Поле eventDate не должно быть пустым.")
    private LocalDateTime eventDate;

    @NotNull(message = "Поле location не должно быть пустым.")
    private Location location;

    private Boolean paid;

    @PositiveOrZero(message = "Поле participantLimit должно быть больше и равняться 0.")
    private Integer participantLimit;

    private Boolean requestModeration;

    @Size(min = 3, max = 120, message = "Поле title должно содержать min 3 и max 120 символов.")
    @NotBlank(message = "Поле title не должно быть пустым.")
    private String title;

}
