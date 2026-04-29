package ru.practicum.events.service;

import ru.practicum.dto.events.dto.EventFullDto;
import ru.practicum.dto.events.dto.EventShortDto;
import ru.practicum.dto.events.UpdateEventAdminRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface EventsService {

    List<EventShortDto> getPublishedEvents(
            String text,
            List<Long> categoryIds,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            boolean sortByViews,
            int from,
            int size
    );

    EventFullDto getPublishedEventById(Long id);

    List<EventFullDto> getEvents(
            List<Long> userIds,
            List<String> states,
            List<Long> categoryIds,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            int from,
            int size
    );

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request);
}
