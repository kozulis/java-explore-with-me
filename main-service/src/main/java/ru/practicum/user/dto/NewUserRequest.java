package ru.practicum.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewUserRequest {

    @Size(min = 6, max = 254, message = "Поле email должно содержать min 6 и max 254 символов.")
    @Email(message = "Некорректный email.")
    @NotBlank(message = "Поле email не должно быть пустым.")
    private String email;

    @Size(min = 2, max = 250, message = "Поле name должно содержать min 2 и max 250 символов.")
    @NotBlank(message = "Поле name не должно быть пустым.")
    private String name;

}
