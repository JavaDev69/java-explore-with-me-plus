package ru.practicum.stats.service;

import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.StatsRequestDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.entity.EndpointHit;
import ru.practicum.stats.mapper.StatsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.repository.StatsRepository;

import java.util.List;

@Service
@Transactional
public class StatsService {
    private final StatsRepository statsRepository;
    private final StatsMapper mapper;

    public StatsService(StatsRepository statsRepository, StatsMapper mapper) {
        this.statsRepository = statsRepository;
        this.mapper = mapper;
    }

    /**
     * Сохраняет хит в БД
     * @param hitDto входящий DTO
     */
    public void saveHit(EndpointHitDto hitDto) {
        EndpointHit entity = mapper.toEntity(hitDto);
        statsRepository.save(entity);
    }

    /**
     * Получает статистику в виде DTO
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

