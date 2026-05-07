package ru.practicum.moderation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.categories.Category;
import ru.practicum.events.Event;
import ru.practicum.events.EventState;
import ru.practicum.events.EventsRepository;
import ru.practicum.events.Location;
import ru.practicum.events.dto.RepairEventDto;
import ru.practicum.events.moderation.ModerationComment;
import ru.practicum.events.moderation.ModerationCommentRepository;
import ru.practicum.events.service.EventsServiceImpl;
import ru.practicum.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @InjectMocks
    private EventsServiceImpl eventService;

    @Mock
    private EventsRepository eventRepository;

    @Mock
    private ModerationCommentRepository moderationCommentRepository;

    private static final Long USER_ID = 1L;
    private static final int FROM = 0;
    private static final int SIZE = 10;

    @Test
    void shouldReturnRepairEventDtosWithCommentsWhenEventsExist() {
        // Given
        Pageable pageable = PageRequest.of(FROM, SIZE);
        List<Event> events = createTestEvents();
        Page<Event> eventPage = new PageImpl<>(events, pageable, events.size());

        List<ModerationComment> moderationComments = createTestModerationComments();


        // Mock репозиториев
        when(eventRepository.findUserModerationHistory(USER_ID, pageable)).thenReturn(eventPage);
        when(moderationCommentRepository.findLastCommentsByEventIds(anyList())).thenReturn(moderationComments);

        // When
        Page<RepairEventDto> result = eventService.getUserModerationHistory(USER_ID, FROM, SIZE);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);

        RepairEventDto firstEvent = result.getContent().get(0);
        RepairEventDto secondEvent = result.getContent().get(1);

        // Проверяем поля RepairEventDto
        assertThat(firstEvent.getId()).isEqualTo(1L);
        assertThat(firstEvent.getAnnotation()).isEqualTo("Test annotation 1");
        assertThat(firstEvent.getCategory()).isEqualTo(100L);
        assertThat(firstEvent.getState()).isEqualTo(EventState.CANCELED);

        // Проверяем комментарии модерации
        assertThat(firstEvent.getLastModerationCommentDto()).isNotNull();
        assertThat(firstEvent.getLastModerationCommentDto().getCommentText())
                .isEqualTo("First moderation comment");
        assertThat(secondEvent.getLastModerationCommentDto()).isNull();
    }

    @Test
    void shouldReturnEmptyPageWhenNoEventsFound() {
        // Given
        Pageable pageable = PageRequest.of(FROM, SIZE);
        Page<Event> emptyPage = Page.empty(pageable);

        when(eventRepository.findUserModerationHistory(USER_ID, pageable)).thenReturn(emptyPage);

        // When
        Page<RepairEventDto> result = eventService.getUserModerationHistory(USER_ID, FROM, SIZE);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void shouldHandleEventsWithoutModerationComments() {
        // Given
        Pageable pageable = PageRequest.of(FROM, SIZE);
        List<Event> events = createEventsWithoutComments();
        Page<Event> eventPage = new PageImpl<>(events, pageable, events.size());

        when(eventRepository.findUserModerationHistory(USER_ID, pageable)).thenReturn(eventPage);
        when(moderationCommentRepository.findLastCommentsByEventIds(anyList())).thenReturn(Collections.emptyList());

        // When
        Page<RepairEventDto> result = eventService.getUserModerationHistory(USER_ID, FROM, SIZE);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        RepairEventDto eventDto = result.getContent().get(0);
        assertThat(eventDto.getLastModerationCommentDto()).isNull();
    }

    @Test
    void shouldApplyCorrectPagination() {
        // Given
        Pageable pageable = PageRequest.of(1, 5); // Вторая страница, размер страницы — 5 элементов

        // Создаём достаточно данных для пагинации: всего 15 событий
        List<Event> allEvents = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            allEvents.add(createEvent((long) i + 1, USER_ID, EventState.CANCELED));
        }

        // Берём только элементы для второй страницы
        List<Event> pageEvents = allEvents.subList(5, 10);
        Page<Event> eventPage = new PageImpl<>(pageEvents, pageable, 15); // Всего 15 элементов

        when(eventRepository.findUserModerationHistory(USER_ID, pageable)).thenReturn(eventPage);
        when(moderationCommentRepository.findLastCommentsByEventIds(anyList()))
                .thenReturn(createTestModerationCommentsForEvents(pageEvents));

        // When
        Page<RepairEventDto> result = eventService.getUserModerationHistory(USER_ID, 1, 5);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(5); // На странице должно быть 5 элементов
        assertThat(result.getTotalElements()).isEqualTo(15); // Всего элементов — 15
        assertThat(result.getNumber()).isEqualTo(1); // Номер текущей страницы — 1 (вторая страница)
        assertThat(result.getSize()).isEqualTo(5); // Размер страницы — 5
        assertThat(result.getTotalPages()).isEqualTo(3); // Всего страниц: 15 / 5 = 3
    }


    @Test
    void shouldFilterEventsByUserIdAndState() {
        // Given
        Pageable pageable = PageRequest.of(FROM, SIZE);

        // Создаём события с разными состояниями и пользователями
        Event event1 = createEvent(1L, USER_ID, EventState.CANCELED);
        Event event2 = createEvent(2L, USER_ID + 1, EventState.REJECTED); // Другой пользователь
        Event event3 = createEvent(3L, USER_ID, EventState.PUBLISHED); // Другое состояние

        List<Event> allEvents = Arrays.asList(event1, event2, event3);
        Page<Event> eventPage = new PageImpl<>(allEvents, pageable, allEvents.size());

        when(eventRepository.findUserModerationHistory(USER_ID, pageable))
                .thenReturn(new PageImpl<>(Collections.singletonList(event1), pageable, 1));

        when(moderationCommentRepository.findLastCommentsByEventIds(anyList()))
                .thenReturn(Collections.emptyList());

        // When
        Page<RepairEventDto> result = eventService.getUserModerationHistory(USER_ID, FROM, SIZE);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    }

    // Вспомогательные методы для создания тестовых данных
    private List<Event> createTestEvents() {
        return Arrays.asList(
                createEvent(1L, USER_ID, EventState.CANCELED),
                createEvent(2L, USER_ID, EventState.REJECTED)
        );
    }

    private List<ModerationComment> createTestModerationComments() {
        ModerationComment comment1 = new ModerationComment();
        comment1.setId(101L);
        comment1.setCommentText("First moderation comment");
        comment1.setCreatedOn(LocalDateTime.now());

        Event event = new Event();
        event.setId(1L);
        comment1.setEvent(event);

        return Collections.singletonList(comment1);
    }

    private List<Event> createEventsWithoutComments() {
        return Collections.singletonList(createEvent(3L, USER_ID, EventState.CANCELED));
    }

    private Event createEvent(Long id, Long userId, EventState state) {
        User user = new User();
        user.setId(userId);

        Category category = new Category();
        category.setId(100L);

        Location location = new Location(55.751244F, 37.618423F);

        return Event.builder()
                .id(id)
                .annotation("Test annotation " + id)
                .category(category)
                .description("Test description " + id)
                .eventDate(LocalDateTime.now().plusDays(1))
                .locationLat(location.getLat())
                .locationLon(location.getLon())
                .paid(false)
                .participantLimit(0)
                .requestModeration(false)
                .title("Test event " + id)
                .state(state)
                .initiator(user)
                .build();
    }

    private List<ModerationComment> createTestModerationCommentsForEvents(List<Event> events) {
        return events.stream()
                .map(event -> {
                    ModerationComment comment = new ModerationComment();
                    comment.setId(event.getId() + 1000); // Уникальный ID
                    comment.setCommentText("Moderation comment for event " + event.getId());
                    comment.setCreatedOn(LocalDateTime.now());

                    comment.setEvent(event);
                    return comment;
                })
                .collect(Collectors.toList());
    }
}

