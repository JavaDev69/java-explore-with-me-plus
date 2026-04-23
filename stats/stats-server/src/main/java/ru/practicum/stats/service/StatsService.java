package ru.practicum.stats.service;

import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.StatsRequestDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.util.List;

/**
 * Интерфейс сервиса для работы со статистикой посещений.
 * Предоставляет методы для сохранения информации о хитах и получения статистики.
 */
public interface StatsService {

    /**
     * Сохраняет информацию о хите в хранилище данных.
     *
     * @param hitDto DTO с данными о запросе, не может быть null
     * @throws RuntimeException если произошла ошибка при сохранении данных
     */
    void saveHit(EndpointHitDto hitDto);

    /**
     * Получает статистику посещений за указанный период времени.
     * В зависимости от параметра unique возвращает:
     * - обычную статистику (количество всех хитов);
     * - уникальную статистику (количество хитов с уникальными IP‑адресами).
     *
     * @param requestDto DTO с параметрами запроса статистики, не может быть null
     * @return список объектов ViewStatsDto с информацией о статистике по URI
     * @throws IllegalArgumentException если параметры запроса некорректны
     */
    List<ViewStatsDto> getStats(StatsRequestDto requestDto);
}
