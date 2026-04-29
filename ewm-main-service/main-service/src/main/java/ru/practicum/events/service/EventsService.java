package ru.practicum.events.service;

import ru.practicum.dto.events.dto.EventFullDto;
import ru.practicum.dto.events.dto.EventShortDto;
import ru.practicum.dto.events.UpdateEventAdminRequest;
import ru.practicum.dto.events.dto.NewEventDto;

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

    /**
     * Сохраняет новое событие, инициированное пользователем.
     *
     * @param newEventDto DTO с данными нового события
     * @param userId ID пользователя, создающего событие
     * @return DTO полного представления сохранённого события
     */
    EventFullDto saveEvent(NewEventDto newEventDto, Long userId);
}
