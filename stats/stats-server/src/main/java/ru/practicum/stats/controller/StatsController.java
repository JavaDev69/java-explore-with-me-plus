package ru.practicum.stats.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.StatsRequestDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.service.StatsService;
import ru.practicum.stats.service.StatsServiceImpl;

import java.util.List;

/**
 * Контроллер для обработки HTTP‑запросов, связанных со статистикой посещений.
 * Обрабатывает запросы на сохранение информации о хитах и получение статистики.
 * <p>
 * Основные функции:
 * - приём данных о хите через POST‑запрос и их сохранение;
 * - предоставление статистики по посещениям через GET‑запрос.
 *
 * @see StatsService — сервис для бизнес‑логики работы со статистикой
 * @see EndpointHitDto — DTO для передачи данных о хите
 * @see StatsRequestDto — DTO с параметрами запроса статистики
 * @see ViewStatsDto — DTO для представления статистических данных
 */

@Slf4j
@RestController
@RequestMapping("")
public class StatsController {
    private final StatsServiceImpl statsService;

    public StatsController(StatsServiceImpl statsService) {
        this.statsService = statsService;
    }

    /**
     * Сохраняет информацию о хите
     *
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
     *
     * @param requestDto DTO с параметрами запроса
     * @return список DTO со статистикой
     */
    @GetMapping("/stats")
    public ResponseEntity<List<ViewStatsDto>> getStats(@Validated StatsRequestDto requestDto) {
        List<ViewStatsDto> stats = statsService.getStats(requestDto);
        return ResponseEntity.ok(stats);
    }
}
