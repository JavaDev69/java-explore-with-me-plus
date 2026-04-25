package ru.practicum.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * DTO может использоваться для обновления и ответов
 */
@Data
@AllArgsConstructor
public class UserDto {

    @NotNull(message = "Идентификатор не может быть null")
    @Positive(message = "Идентификатор должен быть положительным числом (больше 0)")
    private Long id;

    @NotBlank(message = "Имя не может быть пустым")
    private String name;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен быть в корректном формате")
    private String email;
}
