package ru.practicum.events.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.events.dto.EventFullDto;
import ru.practicum.dto.events.dto.NewEventDto;
import ru.practicum.events.service.EventsService;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
public class PrivateEventController {

    private final EventsService eventsService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(
            @RequestBody NewEventDto newEventDto,
            @PathVariable Long userId) {

        log.info("Получен запрос на создание нового события для пользователя с ID: {}. Заголовок события: '{}'", userId, newEventDto.getTitle());
        log.debug("Полные данные события, полученные от клиента: {}", newEventDto);

        EventFullDto savedEvent = eventsService.saveEvent(newEventDto, userId);

        log.info("Событие успешно создано с ID: {} для пользователя с ID: {}", savedEvent.getId(), userId);
        log.debug("Полные данные сохранённого события: {}", savedEvent);

        return savedEvent;
    }
}
