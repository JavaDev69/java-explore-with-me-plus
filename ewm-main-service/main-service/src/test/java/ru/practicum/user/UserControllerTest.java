package ru.practicum.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import ru.practicum.dto.user.NewUserRequest;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
                .andExpect(jsonPath("$.timestamp").exists());
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
                .andExpect(jsonPath("$.timestamp").exists());
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
}
