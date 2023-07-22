package ru.practicum.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCategoryDto {

    @Size(min = 1, max = 50, message = "Поле name должно содержать min 1 и max 50 символов.")
    @NotBlank(message = "Поле name не должно быть пустым.")
    private String name;

}
