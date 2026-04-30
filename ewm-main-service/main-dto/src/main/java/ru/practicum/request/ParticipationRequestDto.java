package ru.practicum.request;

import lombok.Builder;
import lombok.Data;
import ru.practicum.events.EventState;

import java.time.LocalDateTime;

@Builder
@Data
public class ParticipationRequestDto {
    private LocalDateTime created;
    private Long event;
    private Long id;
    private Long requester;
    private EventState status;
}
