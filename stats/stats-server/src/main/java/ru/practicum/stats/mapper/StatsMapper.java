package ru.practicum.stats.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.entity.EndpointHit;

@Component
public class StatsMapper {

    public EndpointHit toEntity(EndpointHitDto dto) {
        EndpointHit entity = new EndpointHit();
        entity.setId(dto.getId());
        entity.setApp(dto.getApp());
        entity.setUri(dto.getUri());
        entity.setIp(dto.getIp());
        entity.setTimestamp(dto.getTimestamp());
        return entity;
    }

    public EndpointHitDto toDto(EndpointHit entity) {
        EndpointHitDto dto = new EndpointHitDto();
        dto.setId(entity.getId());
        dto.setApp(entity.getApp());
        dto.setUri(entity.getUri());
        dto.setIp(entity.getIp());
        dto.setTimestamp(entity.getTimestamp());
        return dto;
    }

    public ViewStatsDto toViewStatsDto(Object[] result) {
        ViewStatsDto dto = new ViewStatsDto();
        dto.setApp((String) result[0]);
        dto.setUri((String) result[1]);
        dto.setHits((Long) result[2]);
        return dto;
    }
}

