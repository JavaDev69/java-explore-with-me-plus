package ru.practicum.events.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.StatsClient;
import ru.practicum.dto.EndpointHit;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.service.EventsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventsController {

    private final EventsService eventService;
    private final StatsClient statsClient;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEvents(
            @RequestParam(required = false)
            @Size(max = 1000, message = "Text length must be less than or equal to 1000 characters")
            String text,

            @RequestParam(required = false)
            List<@Positive(message = "Category IDs must be positive numbers") Long> categories,

            @RequestParam(required = false) Boolean paid,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime rangeStart,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime rangeEnd,

            @RequestParam(defaultValue = "false") Boolean onlyAvailable,

            @RequestParam(defaultValue = "EVENT_DATE")
            @Pattern(regexp = "EVENT_DATE|VIEWS|RATING", message = "Sort must be either 'EVENT_DATE', 'VIEWS' or 'RATING'")
            String sort,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "From must be greater than or equal to 0")
            Integer from,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Size must be greater than 0")
            @Max(value = 1000, message = "Size must be less than or equal to 1000")
            Integer size,

            HttpServletRequest request
    ) {
        saveHit(request);

        List<EventShortDto> events = eventService.getPublishedEvents(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable,
                sort, from, size
        );

        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventFullDto> getEventById(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        saveHit(request);

        EventFullDto event = eventService.getPublishedEventById(id);
        return ResponseEntity.ok(event);
    }

    private void saveHit(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(ip) || "127.0.0.1".equals(ip)) {
            ip = "121.0.0.1";
        }

        EndpointHit hit = EndpointHit.builder()
                .app("ewm-main-service")
                .uri(request.getRequestURI())
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build();
        statsClient.hit(hit);
    }
}