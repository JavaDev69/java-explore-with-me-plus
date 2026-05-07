package ru.practicum.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.events.EventState;
import ru.practicum.events.Location;
import ru.practicum.events.dto.moderation.ModerationCommentShortDto;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepairEventDto {
    private Long id;
    private String annotation;
    private Long category;
    private String description;
    private String eventDate;
    private Location location;
    private Boolean paid = false;
    private Integer participantLimit = 0;
    private Boolean requestModeration = true;
    private String title;
    private EventState state;
    private ModerationCommentShortDto lastModerationCommentDto;
}

