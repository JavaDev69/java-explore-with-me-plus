package ru.practicum.stats.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.StatsRequestDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class StatsClient {
    private final RestTemplate restTemplate;
    private final String serverUrl;

    public StatsClient(RestTemplate restTemplate, @Value("${stats.server.url}") String serverUrl) {
        this.restTemplate = restTemplate;
        this.serverUrl = serverUrl;
    }

    /**
     * Сохраняет информацию о хите
     */
    public void saveHit(EndpointHitDto hitDto) {
        validateEndpointHitDto(hitDto);
        log.debug("StatsClient: Отправка запроса saveHit на URL: {}/hit", serverUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EndpointHitDto> request = new HttpEntity<>(hitDto, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(serverUrl + "/hit", request, Void.class);

        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new RuntimeException("Expected 201 Created, but got: " + response.getStatusCode());
        }

        log.debug("StatsClient: Получен ответ от сервера: статус {}", response.getStatusCode());
    }

    /**
     * Получает статистику по посещениям
     */
    public List<ViewStatsDto> getStats(StatsRequestDto requestDto) {
        validateStatsRequestDto(requestDto);

        String url = buildStatsUrl(requestDto);
        List<ViewStatsDto> result = restTemplate.getForObject(url, List.class);

        return Objects.requireNonNullElse(result, List.of());
    }

    private void validateEndpointHitDto(EndpointHitDto hitDto) {
        if (hitDto == null) {
            throw new IllegalArgumentException("EndpointHitDto не может быть null");
        }
        if (isBlank(hitDto.getApp())) {
            throw new IllegalArgumentException("Поле 'app' обязательно для заполнения");
        }
        if (isBlank(hitDto.getUri())) {
            throw new IllegalArgumentException("Поле 'uri' обязательно для заполнения");
        }
        if (isBlank(hitDto.getIp())) {
            throw new IllegalArgumentException("Поле 'ip' обязательно для заполнения");
        }
        if (hitDto.getTimestamp() == null) {
            throw new IllegalArgumentException("Поле 'timestamp' обязательно для заполнения");
        }
        if (!isValidIpAddress(hitDto.getIp())) {
            throw new IllegalArgumentException("Некорректный формат IP‑адреса: " + hitDto.getIp());
        }
    }

    private void validateStatsRequestDto(StatsRequestDto requestDto) {
        if (requestDto == null) {
            throw new IllegalArgumentException("StatsRequestDto не может быть null");
        }
        if (requestDto.getStart() == null) {
            throw new IllegalArgumentException("Параметр 'start' обязателен для заполнения");
        }
        if (requestDto.getEnd() == null) {
            throw new IllegalArgumentException("Параметр 'end' обязателен для заполнения");
        }
        if (requestDto.getStart().isAfter(requestDto.getEnd())) {
            throw new IllegalArgumentException("Дата 'start' не может быть позже даты 'end'");
        }
        if (requestDto.getUris() != null) {
            for (String uri : requestDto.getUris()) {
                if (isBlank(uri)) {
                    throw new IllegalArgumentException("Элементы в списке 'uris' не могут быть пустыми или null");
                }
            }
        }
    }

    private String buildStatsUrl(StatsRequestDto requestDto) {
        StringBuilder urlBuilder = new StringBuilder(serverUrl + "/stats?");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        urlBuilder.append("start=").append(encodeParam(requestDto.getStart().format(formatter)));
        urlBuilder.append("&end=").append(encodeParam(requestDto.getEnd().format(formatter)));
        urlBuilder.append("&unique=").append(requestDto.isUnique());

        if (requestDto.getUris() != null) {
            for (String uri : requestDto.getUris()) {
                if (!isBlank(uri)) {
                    urlBuilder.append("&uris=").append(encodeParam(uri));
                }
            }
        }

        return urlBuilder.toString();
    }

    private boolean isValidIpAddress(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) return false;

        for (String part : parts) {
            try {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    protected String encodeParam(String param) {
        return URLEncoder.encode(param, StandardCharsets.UTF_8);
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
