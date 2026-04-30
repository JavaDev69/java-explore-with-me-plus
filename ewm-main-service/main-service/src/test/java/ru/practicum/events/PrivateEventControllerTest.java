package ru.practicum.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.categories.Category;
import ru.practicum.categories.CategoryRepository;
import ru.practicum.events.dto.UpdateEventUserRequest;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class PrivateEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventsRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User user;
    private Event event;
    private Category category;

    @BeforeEach
    void setUp() {
        // Создаём тестовую категорию
        category = Category.builder()
                .name("Test Category")
                .build();
        category = categoryRepository.save(category);

        // Создаём тестового пользователя
        user = User.builder()
                .name("Test User")
                .email("test@user.com")
                .build();
        user = userRepository.save(user);

        // Создаём тестовое событие в статусе PENDING
        event = Event.builder()
                .title("Test Event")
                .annotation("Test annotation")
                .description("Test description")
                .initiator(user)
                .state(EventState.PENDING)
                .eventDate(LocalDateTime.now().plusDays(2))
                .category(category)
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .locationLat(55.75f)
                .locationLon(37.62f)
                .confirmedRequests(5L)
                .createdOn(LocalDateTime.now())
                .views(0L)
                .build();
        event = eventRepository.save(event);
    }

    /**
     * Проверяет успешное обновление события с корректными данными → 200 OK.
     */
    @Test
    void shouldUpdateEventSuccessfully() throws Exception {
        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .annotation("Updated annotation with sufficient length to meet the minimum 20 characters requirement")
                .title("Updated title that meets the minimum 3 characters requirement")
                .stateAction(StateAction.CANCEL_REVIEW)
                .build();

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", user.getId(), event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(event.getId()))
                .andExpect(jsonPath("$.title").value("Updated title that meets the minimum 3 characters requirement"))
                .andExpect(jsonPath("$.annotation").value("Updated annotation with sufficient length to meet the minimum 20 characters requirement"))
                .andExpect(jsonPath("$.state").value("CANCELED"));
    }


    /**
     * Проверяет валидацию stateAction: не CANCEL_REVIEW → 403 Forbidden.
     */
    @Test
    void shouldReturnForbiddenWhenStateActionInvalid() throws Exception {
        // Given
        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT) // Неверный статус
                .build();

        // When & Then
        mockMvc.perform(patch("/users/{userId}/events/{eventId}", user.getId(), event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("status\":\"FORBIDDEN\",\"reason\":\"For the requested operation the conditions are not met.")));
    }

    /**
     * Проверяет ошибку «событие не найдено» → 404 Not Found.
     */
    @Test
    void shouldReturnNotFoundWhenEventDoesNotExist() throws Exception {
        // Given
        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .stateAction(StateAction.CANCEL_REVIEW)
                .build();

        // When & Then
        mockMvc.perform(patch("/users/{userId}/events/999", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("ID 999")));
    }

    /**
     * Проверяет ошибку «пользователь не инициатор» → 403 Forbidden.
     */
    @Test
    void shouldReturnForbiddenWhenUserIsNotInitiator() throws Exception {
        // Given: создаём другого пользователя
        User otherUser = User.builder().name("Other User").email("other@user.com").build();
        otherUser = userRepository.save(otherUser);

        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .stateAction(StateAction.CANCEL_REVIEW)
                .build();

        // When & Then
        mockMvc.perform(patch("/users/{userId}/events/{eventId}", otherUser.getId(), event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("FORBIDDEN\",\"reason\":\"For the requested operation the conditions are not met.")));
    }

    /**
     * Проверяет ошибку «статус события не PENDING/CANCELED» → 403 Forbidden.
     */
    @Test
    void shouldReturnForbiddenWhenEventStateInvalid() throws Exception {
        // Given: обновляем событие до статуса PUBLISHED
        event.setState(EventState.PUBLISHED);
        eventRepository.save(event);

        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .stateAction(StateAction.CANCEL_REVIEW)
                .build();

        // When & Then
        mockMvc.perform(patch("/users/{userId}/events/{eventId}", user.getId(), event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("status\":\"FORBIDDEN\",\"reason\":\"For the requested operation the conditi")));
    }

    /**
     * Проверяет частичное обновление: обновляются только не‑null поля.
     */
    @Test
    void shouldPartiallyUpdateEvent() throws Exception {
        // Given: только аннотация, остальные поля null
        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .annotation("Partially updated annotation")
                .stateAction(StateAction.CANCEL_REVIEW)
                .build();

        // When & Then
        mockMvc.perform(patch("/users/{userId}/events/{eventId}", user.getId(), event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.annotation").value("Partially updated annotation"))
                // Другие поля не должны измениться
                .andExpect(jsonPath("$.title").value("Test Event"));
    }

    /**
     * Проверяет ошибку при попытке обновить событие с несуществующей категорией.
     */
    @Test
    void shouldReturnNotFoundWhenCategoryDoesNotExist() throws Exception {
        // Given: указываем несуществующую категорию
        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .category(999L)
                .stateAction(StateAction.CANCEL_REVIEW)
                .build();

        // When & Then
        mockMvc.perform(patch("/users/{userId}/events/{eventId}", user.getId(), event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Category with id=999 was not found")));
    }

    /**
     * Проверяет обновление местоположения события.
     */
    @Test
    void shouldUpdateEventLocationSuccessfully() throws Exception {
        // Given
        Location newLocation = Location.builder()
                .lat(59.93f)
                .lon(30.34f)
                .build();

        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .location(newLocation)
                .stateAction(StateAction.CANCEL_REVIEW)
                .build();

        // When & Then
        mockMvc.perform(patch("/users/{userId}/events/{eventId}", user.getId(), event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location.lat").value(59.93f))
                .andExpect(jsonPath("$.location.lon").value(30.34f));
    }

    /**
     * Проверяет, что null‑поля в запросе не перезаписывают существующие значения.
     */
    @Test
    void shouldNotUpdateNullFields() throws Exception {
        // Given: только stateAction, все остальные поля null
        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .stateAction(StateAction.CANCEL_REVIEW)
                .build();

        // When & Then
        mockMvc.perform(patch("/users/{userId}/events/{eventId}", user.getId(), event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                // Проверяем, что все исходные значения сохранились
                .andExpect(jsonPath("$.title").value("Test Event"))
                .andExpect(jsonPath("$.annotation").value("Test annotation"))
                .andExpect(jsonPath("$.eventDate").value(Matchers.startsWith(event.getEventDate().toString().substring(0, 19))
                ));
    }
}
