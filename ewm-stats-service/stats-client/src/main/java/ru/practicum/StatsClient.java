package ru.practicum;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsClient {
    void hit(EndpointHit hit);

    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
