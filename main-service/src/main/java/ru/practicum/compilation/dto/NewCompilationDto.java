package ru.practicum.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCompilationDto {
    private List<Long> events;
    private Boolean pinned = false;
    @NotBlank(message = "Поле title не должно быть пустым.")
    @Size(min = 1, max = 50, message = "Поле title должно содержать min 1 и max 50 символов.")
    private String title;
}
