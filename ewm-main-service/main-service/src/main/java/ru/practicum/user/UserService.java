package ru.practicum.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.error.exception.NotFoundException;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserJpaRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Конфликт email предварительно не проверяется.
     * Для соответствия спецификации ловится ошибка базы данных DataIntegrityViolationException
     * и обрабатывается в GlobalExceptionHandler
     * @param request
     * @return
     */
    @Transactional
    public UserDto save(NewUserRequest request) {

        User user = userRepository.save(userMapper.toEntity(request));
        return userMapper.toDto(user);
    }

    @Transactional
    public void deleteById(Long id) {
        if (userRepository.deleteByIdAndReturnRow(id) == 0)
            throw new NotFoundException("Пользователь с id:"+id+" не существует");
    }
}
