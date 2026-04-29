package ru.practicum.dto.compilation;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCompilationRequest {

    @Size(min = 1, max = 50, message = "Заголовок подборки должен содержать от 1 до 50 символов")
    private String title;

    private Boolean pinned;

    @Size(max = 1000, message = "Описание подборки не должно превышать 1000 символов")
    private String description;

    @NotNull(message = "Список событий не может быть null (если передаётся — должен быть пустым списком для очистки)")
    private List<Long> events;
}
