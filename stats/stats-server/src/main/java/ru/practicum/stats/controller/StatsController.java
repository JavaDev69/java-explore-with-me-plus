package ru.practicum.stats.controller;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.StatsRequestDto;
import ru.practicum.stats.dto.ViewStatsDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.service.StatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("")
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /**
     * Сохраняет информацию о хите
     * @param hitDto DTO с данными о запросе
     * @return HTTP 201 Created
     */
    @PostMapping("/hit")
    public ResponseEntity<Void> saveHit(@RequestBody @Validated EndpointHitDto hitDto) {
        try {
            statsService.saveHit(hitDto);
            return ResponseEntity.status(201).build();
        } catch (Exception e) {
            log.error("Error saving hit", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Получает статистику по посещениям
     * @param requestDto DTO с параметрами запроса
     * @return список DTO со статистикой
     */
    @GetMapping("/stats")
    public ResponseEntity<List<ViewStatsDto>> getStats(@Validated StatsRequestDto requestDto) {
        List<ViewStatsDto> stats = statsService.getStats(requestDto);
        return ResponseEntity.ok(stats);
    }
}
