package ru.practicum.moderation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.categories.Category;
import ru.practicum.events.Event;
import ru.practicum.events.EventState;
import ru.practicum.events.EventsRepository;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.service.EventsService;
import ru.practicum.events.service.EventsServiceImpl;
import ru.practicum.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class EventServiceImplTest {

    @Autowired
    private EventsRepository eventRepository;

    @Autowired
    private TestEntityManager entityManager;

    private EventsService adminEventService;

    @BeforeEach
    void setUp() {
        adminEventService = new EventsServiceImpl(
                eventRepository,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    @AfterEach
    void tearDown() {
        entityManager.getEntityManager().createQuery("DELETE FROM Event").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Category").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM User").executeUpdate();
        entityManager.flush();
    }

    @Test
    void shouldGetEventsWithRequestModerationTrueAndStatePending() {
        // GIVEN — создаём и сохраняем тестовые данные
        User user1 = createAndSaveUser("User One", "user1@test.com");
        User user2 = createAndSaveUser("User Two", "user2@test.com");
        User user3 = createAndSaveUser("User Three", "user3@test.com");

        Category cat1 = createAndSaveCategory("Category A");
        Category cat2 = createAndSaveCategory("Category B");

        Event event1 = createAndSaveEvent(Boolean.TRUE, EventState.PENDING, "Event 1", user1, cat1);
        Event event2 = createAndSaveEvent(Boolean.TRUE, EventState.PENDING, "Event 2", user2, cat2);
        Event event3 = createAndSaveEvent(Boolean.FALSE, EventState.PUBLISHED, "Event 3", user3, cat1);

        entityManager.flush();

        // WHEN — вызов тестируемого метода
        Page<EventFullDto> result = adminEventService.getEventsForModeration(0, 10);

        // THEN — проверка результатов
        assertThat(result.getContent()).hasSize(2);

        List<String> titles = result.getContent().stream()
                .map(EventFullDto::getTitle)
                .collect(Collectors.toList());

        assertThat(titles).containsExactlyInAnyOrder("Event 1", "Event 2");
        assertThat(result.getTotalElements()).isEqualTo(2L);
    }

    /**
     * Вспомогательные методы
     */
    private User createUser(String name, String email) {
        return User.builder()
                .name(name)
                .email(email)
                .build();
    }

    private Category createCategory(String name) {
        return Category.builder()
                .name(name)
                .build();
    }

    private Event createEvent(Boolean requestModeration, EventState state, String title, User user, Category category) {
        return Event.builder()
                .initiator(user)
                .category(category)
                .requestModeration(requestModeration)
                .state(state)
                .title(title)
                .annotation("Test annotation for " + title)
                .description("Test description for " + title)
                .eventDate(LocalDateTime.now().plusDays(1))
                .paid(false)
                .confirmedRequests(0L)
                .participantLimit(0)
                .views(0L)
                .createdOn(LocalDateTime.now())
                .build();
    }

    private User createAndSaveUser(String name, String email) {
        return entityManager.persistAndFlush(
                createUser(name, email)
        );
    }

    private Category createAndSaveCategory(String name) {
        return entityManager.persistAndFlush(
                createCategory(name)
        );
    }

    private Event createAndSaveEvent(Boolean requestModeration, EventState state, String title, User user, Category category) {
        return entityManager.persistAndFlush(
                createEvent(requestModeration, state, title, user, category)
        );
    }
}
