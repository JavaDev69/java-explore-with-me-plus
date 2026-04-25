package ru.practicum.dto.user;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * DTO для создания нового пользователя
 */
@Data
public class NewUserRequest {

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен быть в корректном формате")
    @Size(min = 6, max = 254, message = "Длина email должна быть от 6 до 254 символов")
    private String email;

    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 250, message = "Длина имени должна быть от 2 до 250 символов")
    private String name;
}

