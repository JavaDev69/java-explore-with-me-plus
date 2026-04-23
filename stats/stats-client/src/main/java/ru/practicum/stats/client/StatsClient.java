package ru.practicum.stats.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.StatsRequestDto;
import ru.practicum.stats.dto.ViewStatsDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
     * @param hitDto DTO с данными о запросе
     */
    public void saveHit(EndpointHitDto hitDto) {
        log.debug("StatsClient: Отправка запроса saveHit на URL: {}/hit", serverUrl);
        log.debug("StatsClient: Тело запроса: {}", hitDto);

        if (hitDto.getApp() == null || hitDto.getUri() == null ||
                hitDto.getIp() == null || hitDto.getTimestamp() == null) {
            throw new IllegalArgumentException("Missing required fields in EndpointHitDto");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<EndpointHitDto> request = new HttpEntity<>(hitDto, headers);
            ResponseEntity<Void> response = restTemplate.postForEntity(serverUrl + "/hit", request, Void.class);
            log.debug("StatsClient: Получен ответ от сервера: статус {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("StatsClient: Ошибка при отправке запроса saveHit", e);
            throw e;
        }
    }

    /**
     * Получает статистику по посещениям
     * @param requestDto DTO с параметрами запроса
     * @return список DTO со статистикой
     */
    public List<ViewStatsDto> getStats(StatsRequestDto requestDto) {
        try {
            StringBuilder urlBuilder = new StringBuilder(serverUrl + "/stats?"); // ← добавлено /stats

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String startFormatted = requestDto.getStart().format(formatter);
            String endFormatted = requestDto.getEnd().format(formatter);

            urlBuilder.append("start=").append(encodeParam(startFormatted));
            urlBuilder.append("&end=").append(encodeParam(endFormatted));
            urlBuilder.append("&unique=").append(requestDto.isUnique());

            if (requestDto.getUris() != null && !requestDto.getUris().isEmpty()) {
                for (String uri : requestDto.getUris()) {
                    urlBuilder.append("&uris=").append(encodeParam(uri));
                }
            }

            return restTemplate.getForObject(urlBuilder.toString(), List.class);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching statistics", e);
        }
    }


    protected String encodeParam(String param) {
        return URLEncoder.encode(param, StandardCharsets.UTF_8);
    }
}
