package ru.practicum.requests.participation;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.ParticipationRequestDto;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Slf4j
public class PrivateParticipationRequestController {

    private final ParticipationsRequestsService participationsRequestsService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createParticipationRequest(
            @PathVariable @Positive Long userId,
            @RequestParam @Positive Long eventId) {
        log.info("Получен запрос на создание заявки пользователя {} на участие в событии {}", userId, eventId);

        ParticipationRequestDto createdRequest = participationsRequestsService.createParticipationRequest(userId, eventId);

        log.info("Заявка успешно создана с ID: {} для пользователя {} на событие {}", createdRequest.getId(), userId, eventId);
        return createdRequest;
    }
}

