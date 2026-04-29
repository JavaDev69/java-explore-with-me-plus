package ru.practicum.events.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.events.Location;
import ru.practicum.events.StateAction;

import java.time.LocalDateTime;

@Data
public class UpdateEventUserRequest {

    @Size(min = 20, max = 2000, message = "Аннотация должна быть от 20 до 2000 символов")
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000, message = "Описание должно быть от 20 до 7000 символов")
    private String description;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private Location location;

    private Boolean paid;

    @Min(value = 0, message = "Лимит участников минимум 0")
    @Max(value = Integer.MAX_VALUE, message = "Превышен допустимый лимит участников")
    private Integer participantLimit;

    private Boolean requestModeration;

    @NotNull(message = "Статус действия должен быть задан")
    private StateAction stateAction;

    @Size(min = 3, max = 120, message = "Заголовок должен быть от 3 до 120 символов")
    private String title;
}

