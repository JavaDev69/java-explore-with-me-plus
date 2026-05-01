package ru.practicum.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.error.exception.ConflictException;
import ru.practicum.error.exception.NotFoundException;
import ru.practicum.events.Event;
import ru.practicum.events.EventState;
import ru.practicum.events.EventsRepository;
import ru.practicum.request.ParticipationRequestDto;
import ru.practicum.requests.ParticipationRequest;
import ru.practicum.requests.RequestRepository;
import ru.practicum.requests.participation.ParticipationsRequestsService;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipationsRequestsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventsRepository eventsRepository;

    @Mock
    private RequestRepository requestRepository;

    @InjectMocks
    private ParticipationsRequestsService participationsRequestsService;

    @Test
    void createParticipationRequest_Success_AutoConfirm() {
        // Given
        Long userId = 1L;
        Long eventId = 100L;

        User requester = new User();
        requester.setId(userId);

        Event event = new Event();
        event.setId(eventId);
        event.setState(EventState.PUBLISHED);
        event.setParticipantLimit(10);
        event.setRequestModeration(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(requester));
        when(eventsRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.existsByEventIdAndRequesterId(eventId, userId)).thenReturn(false);
        when(requestRepository.countByEventIdAndStatus(eventId, EventState.CONFIRMED)).thenReturn(5L);

        ParticipationRequest savedRequest = new ParticipationRequest();
        savedRequest.setId(1000L);
        savedRequest.setStatus(EventState.CONFIRMED);
        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(savedRequest);

        // When
        ParticipationRequestDto result = participationsRequestsService.createParticipationRequest(userId, eventId);

        // Then
        assertNotNull(result);
        assertEquals(EventState.CONFIRMED, result.getStatus());
        verify(requestRepository, times(1)).save(any(ParticipationRequest.class));
        verify(userRepository, times(1)).findById(userId);
        verify(eventsRepository, times(1)).findById(eventId);
    }

    @Test
    void createParticipationRequest_Success_Pending() {
        // Given
        Long userId = 2L;
        Long eventId = 200L;

        User requester = new User();
        requester.setId(userId);

        Event event = new Event();
        event.setId(eventId);
        event.setState(EventState.PUBLISHED);
        event.setParticipantLimit(5);
        event.setRequestModeration(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(requester));
        when(eventsRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.existsByEventIdAndRequesterId(eventId, userId)).thenReturn(false);
        when(requestRepository.countByEventIdAndStatus(eventId, EventState.CONFIRMED)).thenReturn(3L);

        ParticipationRequest savedRequest = new ParticipationRequest();
        savedRequest.setId(2000L);
        savedRequest.setStatus(EventState.PENDING);
        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(savedRequest);

        // When
        ParticipationRequestDto result = participationsRequestsService.createParticipationRequest(userId, eventId);

        // Then
        assertNotNull(result);
        assertEquals(EventState.PENDING, result.getStatus());
    }

    @Test
    void createParticipationRequest_UserNotFound_ThrowsNotFoundException() {
        // Given
        Long userId = 999L;
        Long eventId = 100L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> participationsRequestsService.createParticipationRequest(userId, eventId));

        assertTrue(exception.getMessage().contains("User with id=" + userId));
    }

    @Test
    void createParticipationRequest_EventNotFound_ThrowsNotFoundException() {
        // Given
        Long userId = 1L;
        Long eventId = 999L;

        User requester = new User();
        requester.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(requester));
        when(eventsRepository.findById(eventId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> participationsRequestsService.createParticipationRequest(userId, eventId));

        assertTrue(exception.getMessage().contains("Event with id=" + eventId));
    }

    @Test
    void createParticipationRequest_RequesterIsInitiator_ThrowsConflictException() {
        // Given
        Long userId = 1L;
        Long eventId = 100L;

        User requester = new User();
        requester.setId(userId);

        Event event = new Event();
        event.setId(eventId);
        event.setState(EventState.PUBLISHED);
        event.setInitiator(requester); // инициатор = запрашивающий

        when(userRepository.findById(userId)).thenReturn(Optional.of(requester));
        when(eventsRepository.findById(eventId)).thenReturn(Optional.of(event));

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class,
                () -> participationsRequestsService.createParticipationRequest(userId, eventId));

        assertEquals("User cannot request participation in their own event", exception.getMessage());
    }

    @Test
    void createParticipationRequest_UnpublishedEvent_ThrowsConflictException() {
        // Given
        Long userId = 1L;
        Long eventId = 100L;

        User requester = new User();
        requester.setId(userId);

        Event event = new Event();
        event.setId(eventId);
        event.setState(EventState.PENDING); // не PUBLISHED

        when(userRepository.findById(userId)).thenReturn(Optional.of(requester));
        when(eventsRepository.findById(eventId)).thenReturn(Optional.of(event));

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class,
                () -> participationsRequestsService.createParticipationRequest(userId, eventId));

        assertEquals("Cannot participate in unpublished event", exception.getMessage());
    }

    @Test
    void createParticipationRequest_DuplicateRequest_ThrowsConflictException() {
        // Given
        Long userId = 1L;
        Long eventId = 100L;

        User requester = new User();
        requester.setId(userId);

        Event event = new Event();
        event.setId(eventId);
        event.setState(EventState.PUBLISHED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(requester));
        when(eventsRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.existsByEventIdAndRequesterId(eventId, userId)).thenReturn(true); // дубликат

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class,
                () -> participationsRequestsService.createParticipationRequest(userId, eventId));

        assertEquals("Duplicate participation request", exception.getMessage());
    }

    @Test
    void createParticipationRequest_ParticipantLimitReached_ThrowsConflictException() {
        // Given
        Long userId = 1L;
        Long eventId = 100L;

        User requester = new User();
        requester.setId(userId);

        Event event = new Event();
        event.setId(eventId);
        event.setState(EventState.PUBLISHED);
        event.setParticipantLimit(5); // лимит 5 участников
        event.setRequestModeration(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(requester));
        when(eventsRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.existsByEventIdAndRequesterId(eventId, userId)).thenReturn(false);
        when(requestRepository.countByEventIdAndStatus(eventId, EventState.CONFIRMED)).thenReturn(5L); // уже 5 подтверждённых заявок

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class,
                () -> participationsRequestsService.createParticipationRequest(userId, eventId));

        assertEquals("Event participant limit reached", exception.getMessage());
    }

    @Test
    void createParticipationRequest_NoLimit_Success() {
        // Given
        Long userId = 3L;
        Long eventId = 300L;

        User requester = new User();
        requester.setId(userId);

        Event event = new Event();
        event.setId(eventId);
        event.setState(EventState.PUBLISHED);
        event.setParticipantLimit(0); // без лимита
        event.setRequestModeration(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(requester));
        when(eventsRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.existsByEventIdAndRequesterId(eventId, userId)).thenReturn(false);
        // countByEventIdAndStatus может вернуть любое число — лимит не ограничен
        when(requestRepository.countByEventIdAndStatus(eventId, EventState.CONFIRMED)).thenReturn(100L);

        ParticipationRequest savedRequest = new ParticipationRequest();
        savedRequest.setId(3000L);
        savedRequest.setStatus(EventState.PENDING);
        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(savedRequest);

        // When
        ParticipationRequestDto result = participationsRequestsService.createParticipationRequest(userId, eventId);

        // Then
        assertNotNull(result);
        assertEquals(EventState.PENDING, result.getStatus());
        verify(requestRepository, times(1)).save(any(ParticipationRequest.class));
    }
}
