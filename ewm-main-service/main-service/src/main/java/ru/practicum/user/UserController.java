package ru.practicum.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;

@RestController
@RequestMapping("/admin/users")
@Slf4j
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    /**
     * Создаёт нового пользователя через административный API
     *
     * @param request DTO с данными нового пользователя (имя и email)
     * @return ResponseEntity с UserDto и HTTP‑статусом 201 (CREATED)
     */

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public UserDto createUser(@Valid @RequestBody NewUserRequest request) {
        log.info("Получен запрос на создание пользователя: {}", request);
        log.debug("Пользователь {}", request);
        UserDto dto = userService.save(request);

        log.info("Пользователь успешно создан с ID: {}", dto.getId());
        return dto;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{userId}")
    public void delete(@PathVariable @Positive Long userId) {
        log.info("Получен запрос на удаление пользователя с ID: {}", userId);
        userService.deleteById(userId);
        log.info("Пользователь с ID {} успешно удалён", userId);
    }
}

