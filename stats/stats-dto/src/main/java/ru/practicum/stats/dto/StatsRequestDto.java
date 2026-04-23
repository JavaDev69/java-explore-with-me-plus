package ru.practicum.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsRequestDto {
    @NotNull(message = "Start date cannot be null")
    private LocalDateTime start;

    @NotNull(message = "End date cannot be null")
    private LocalDateTime end;

    private List<String> uris;
    private boolean unique;
}

