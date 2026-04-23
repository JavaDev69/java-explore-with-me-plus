package ru.practicum.stats.client;

import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.StatsRequestDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.util.List;

/**
 * Клиент для взаимодействия с сервисом статистики посещений.
 * Предоставляет методы для:
 * - сохранения информации о хите (запросе к эндпоинту);
 * - получения статистики по посещениям с фильтрацией и группировкой.
 */
public interface Client {

    /**
     * Сохраняет информацию о хите — факте обращения к эндпоинту.
     *
     * @param hitDto DTO с данными о запросе. Обязательные поля:
     *               - app: название сервиса;
     *               - uri: URI запроса;
     *               - ip: IP‑адрес клиента;
     *               - timestamp: время запроса.
     * @throws IllegalArgumentException если какие‑либо обязательные поля отсутствуют или некорректны
     * @throws RuntimeException         если сервер вернул статус, отличный от 201 Created
     */
    void saveHit(EndpointHitDto hitDto);

    /**
     * Получает статистику по посещениям за указанный период.
     *
     * @param requestDto DTO с параметрами запроса. Обязательные поля:
     *                   - start: начальная дата и время периода;
     *                   - end: конечная дата и время периода (не может быть раньше start).
     *                   Опциональные параметры:
     *                   - uris: список URI для фильтрации (если не задан — статистика по всем URI);
     *                   - unique: флаг учёта только уникальных IP‑адресов.
     * @return список DTO со статистикой посещений. Если сервер вернул null, возвращается пустой список.
     * @throws IllegalArgumentException если параметры запроса некорректны
     * @throws RuntimeException         если произошла ошибка при выполнении HTTP‑запроса
     */
    List<ViewStatsDto> getStats(StatsRequestDto requestDto);
}

