package ru.practicum.events;

import ru.practicum.dto.events.EventFullDto;
import ru.practicum.dto.events.EventShortDto;
import ru.practicum.dto.events.Location;
import ru.practicum.user.UserMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static ru.practicum.categories.CategoryMapper.toCategoryDto;

public class EventsMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public static EventShortDto toShortEventDto(Event event, Long confirmedRequests) {
        EventShortDto dto = new EventShortDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(toCategoryDto(event.getCategory()));
        dto.setConfirmedRequests(confirmedRequests);
        dto.setEventDate(event.getEventDate());
        dto.setInitiator(new UserMapper().toShortDto(event.getInitiator()));
        dto.setPaid(event.getPaid());
        dto.setTitle(event.getTitle());
        dto.setViews(event.getViews());
        return dto;
    }

    public static EventFullDto toEventFullDto(Event event) {
        EventFullDto dto = new EventFullDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(toCategoryDto(event.getCategory()));
        dto.setConfirmedRequests(event.getConfirmedRequests());
        dto.setCreatedOn(format(event.getCreatedOn()));
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());
        dto.setInitiator(new UserMapper().toShortDto(event.getInitiator()));
        dto.setLocation(new Location(event.getLocationLat(), event.getLocationLon()));
        dto.setPaid(event.getPaid());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setPublishedOn(event.getPublishedOn() != null ? format(event.getPublishedOn()) : null);
        dto.setRequestModeration(event.getRequestModeration());
        dto.setState(event.getState().name());
        dto.setTitle(event.getTitle());
        dto.setViews(event.getViews());
        return dto;
    }

    private static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(FORMATTER) : null;
    }
}
