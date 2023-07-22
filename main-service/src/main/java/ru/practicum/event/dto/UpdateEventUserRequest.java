package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.event.model.Location;
import ru.practicum.event.utils.UserStateAction;

import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEventUserRequest {

    @Size(min = 20, max = 2000, message = "Поле annotation должно содержать min 20 и max 2000 символов.")
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000, message = "Поле description должно содержать min 20 и max 7000 символов.")
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private Location location;

    private Boolean paid;

    @PositiveOrZero(message = "Поле participantLimit должно быть больше и равняться 0.")
    private Integer participantLimit;

    private Boolean requestModeration;

    private UserStateAction stateAction;

    @Size(min = 3, max = 120, message = "Поле title должно содержать min 3 и max 120 символов.")
    private String title;

}
