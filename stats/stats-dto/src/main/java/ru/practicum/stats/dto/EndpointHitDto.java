package ru.practicum.stats.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class EndpointHitDto {
    private Long id;

    @NotBlank(message = "App не может быть пуст")
    private String app;

    @NotBlank(message = "URI не может быть пуст")
    private String uri;

    @NotBlank(message = "IP address не может быть пуст")
    private String ip;

    @NotNull(message = "Timestamp  не может быть пуст")
    private LocalDateTime timestamp;
}

