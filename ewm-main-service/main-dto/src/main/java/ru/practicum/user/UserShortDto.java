package ru.practicum.user;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO может использоваться для ответов
 */
@Data
@AllArgsConstructor
public class UserShortDto {

    private Long id;

    private String name;
}
