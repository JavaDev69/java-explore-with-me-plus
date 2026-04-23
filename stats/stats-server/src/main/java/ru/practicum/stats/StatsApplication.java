package ru.practicum.stats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс приложения для сервиса статистики.
 * Точка входа в приложение.
 *
 * При запуске:
 * - загружает конфигурацию из application.properties;
 * - сканирует компоненты (@Component, @Service, @Repository, @Controller);
 * - настраивает Spring Boot‑автоконфигурации;
 * - запускает встроенный сервер Tomcat;
 * - подключает базу данных (PostgreSQL в продакшене, H2 в тестах).
 */
@SpringBootApplication
public class StatsApplication {

    public static void main(String[] args) {
        SpringApplication.run(StatsApplication.class, args);
    }
}
