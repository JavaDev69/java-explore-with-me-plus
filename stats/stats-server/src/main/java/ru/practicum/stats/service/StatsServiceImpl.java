package ru.practicum.stats.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.StatsRequestDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.entity.EndpointHit;
import ru.practicum.stats.mapper.StatsMapper;
import ru.practicum.stats.repository.StatsJpaRepository;

import java.util.List;

/**
 * Реализация сервиса для работы со статистикой посещений.
 * Содержит бизнес‑логику сохранения хитов и формирования статистических отчётов.
 * <p>
 * Особенности:
 * - использует {@link StatsJpaRepository} для взаимодействия с БД;
 * - применяет {@link StatsMapper} для преобразования DTO в сущности и обратно;
 * - поддерживает два режима статистики: обычная и уникальная (по IP);
 * - все операции выполняются в транзакциях ({@link Transactional}).
 */

@Service
@Transactional
public class StatsServiceImpl {
    private final StatsJpaRepository statsRepository;
    private final StatsMapper mapper;

    public StatsServiceImpl(StatsJpaRepository statsRepository, StatsMapper mapper) {
        this.statsRepository = statsRepository;
        this.mapper = mapper;
    }

    /**
     * Сохраняет хит в БД
     *
     * @param hitDto входящий DTO
     */
    public void saveHit(EndpointHitDto hitDto) {
        EndpointHit entity = mapper.toEntity(hitDto);
        statsRepository.save(entity);
    }

    /**
     * Получает статистику в виде DTO
     *
     * @param requestDto параметры запроса в виде DTO
     * @return список ViewStatsDto
     */
    public List<ViewStatsDto> getStats(StatsRequestDto requestDto) {
        if (requestDto.isUnique()) {
            return statsRepository.findUniqueStats(
                            requestDto.getStart(),
                            requestDto.getEnd(),
                            requestDto.getUris()
                    ).stream()
                    .toList();
        } else {
            return statsRepository.findStats(
                            requestDto.getStart(),
                            requestDto.getEnd(),
                            requestDto.getUris()
                    ).stream()
                    .toList();
        }
    }
}

