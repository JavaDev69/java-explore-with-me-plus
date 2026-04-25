package ru.practicum.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import ru.practicum.dto.user.NewUserRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static String pattern = "yyyy-MM-dd HH:mm:ss";
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
    /**
     * Тест 1: Успешное создание пользователя (201 Created)
     */
    @Test
    void createUser_Success() throws Exception {
        NewUserRequest request = new NewUserRequest();
        request.setEmail("ivan.petrov@practicummail.ru");
        request.setName("Иван Петров");

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ivan.petrov@practicummail.ru"))
                .andExpect(jsonPath("$.name").value("Иван Петров"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    /**
     * Тест 2: Дублирование email (409 Conflict)
     */
    @Test
    void createUser_DuplicateEmail_Conflict() throws Exception {
        // Первый запрос — успешный (создаём пользователя)
        NewUserRequest firstRequest = new NewUserRequest();
        firstRequest.setEmail("duplicate@example.com");
        firstRequest.setName("Duplicate User");

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(firstRequest)))
                .andExpect(status().isCreated());

        // Второй запрос с тем же email — должен вызвать ошибку 409
        NewUserRequest secondRequest = new NewUserRequest();
        secondRequest.setEmail("duplicate@example.com");
        secondRequest.setName("Another User");

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(secondRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"))
                .andExpect(jsonPath("$.reason").value("Integrity constraint has been violated."))
                .andExpect(jsonPath("$.message").value(containsString("constraint [uq_email]")))
                .andExpect(isValidTimestampFormat());
    }

    /**
     * Тест 3: Некорректный запрос (400 Bad Request)
     * Проверяем валидацию: пустой email и null name
     */
    @Test
    void createUser_InvalidRequest_BadRequest() throws Exception {
        NewUserRequest invalidRequest = new NewUserRequest();
        invalidRequest.setEmail("");
        invalidRequest.setName(null);

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."))
                .andExpect(jsonPath("$.message").value(containsString("Field: name")))
                .andExpect(jsonPath("$.message").value(containsString("Имя не может быть пустым")))
                .andExpect(jsonPath("$.message").value(containsString("Field: email")))
                .andExpect(jsonPath("$.message").value(containsString("Email не может быть пустым")))
                .andExpect(isValidTimestampFormat());
    }

    /**
     * Тест 4: Успешное удаление пользователя (204 No Content)
     */
    @Test
    void deleteUser_Success() throws Exception {
        // Сначала создаём пользователя
        NewUserRequest createRequest = new NewUserRequest();
        createRequest.setEmail("delete.user@practicummail.ru");
        createRequest.setName("Delete User");

        ResultActions createResult = mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("delete.user@practicummail.ru"))
                .andExpect(jsonPath("$.name").value("Delete User"))
                .andExpect(jsonPath("$.id").isNumber());

        // Извлекаем ID созданного пользователя
        MvcResult createMvcResult = createResult.andReturn();
        String responseContent = createMvcResult.getResponse().getContentAsString();
        JsonNode response = objectMapper.readTree(responseContent);
        Long userId = response.get("id").asLong();

        // Теперь удаляем созданного пользователя
        mockMvc.perform(delete("/admin/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                // Тело ответа должно быть пустым для 204 No Content
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().isEmpty()));
    }

    /**
     * Тест 5: Удаление несуществующего пользователя (404 Not Found)
     */
    @Test
    void deleteUser_UserNotFound_404() throws Exception {
        Long nonExistentUserId = 999999L;

        mockMvc.perform(delete("/admin/users/{userId}", nonExistentUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.reason").value("The required object was not found."))
                .andExpect(jsonPath("$.message").value(containsString("Пользователь с id:" + nonExistentUserId + " не существует")))
                .andExpect(isValidTimestampFormat());
    }

    /**
     * Тест 6: Удаление с некорректным ID (отрицательный) — 400 Bad Request
     */
    @Test
    void deleteUser_InvalidNegativeId_BadRequest() throws Exception {
        Long negativeUserId = -1L;

        mockMvc.perform(delete("/admin/users/{userId}", negativeUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."))
                .andExpect(jsonPath("$.message").value(containsString("must be greater than 0")))
                .andExpect(isValidTimestampFormat());
    }

    /**
     * Вспомогательный метод для сериализации объектов в JSON
     */
    private String asJsonString(Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Вспомогательный метод проверки формата LocalDateTime (yyyy-MM-dd HH:mm:ss) в теле ответа
     * @return
     */
    private static ResultMatcher isValidTimestampFormat() {
        return result -> {
            try {
                String responseContent = result.getResponse().getContentAsString();
                JsonNode response = objectMapper.readTree(responseContent);
                String timestamp = response.get("timestamp").asText();

                if (timestamp == null || timestamp.isEmpty()) {
                    fail("Поле \"timestamp\" отсутствует или пусто в ответе");
                }

                assertDoesNotThrow(
                        () -> LocalDateTime.parse(timestamp, formatter),
                        "Формат timestamp не соответствует шаблону " + pattern + ". Значение: " + timestamp
                );
            } catch (Exception e) {
                fail("Ошибка при проверке формата timestamp: " + e.getMessage());
            }
        };
    }
}
