package ru.practicum.stats.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.entity.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.AUTO_CONFIGURED)
class StatsRepositoryTest {

    @Autowired
    private StatsJpaRepository repository;

    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        start = LocalDateTime.of(2022, 9, 6, 0, 0, 0);
        end = LocalDateTime.of(2022, 9, 7, 23, 59, 59);

        repository.deleteAll();

        // Создаём тестовые данные с разными комбинациями IP и URI
        EndpointHit hit1 = new EndpointHit();
        hit1.setApp("ewm-main-service");
        hit1.setUri("/events/1");
        hit1.setIp("192.163.0.1");
        hit1.setTimestamp(LocalDateTime.of(2022, 9, 6, 11, 0, 25));

        EndpointHit hit2 = new EndpointHit();
        hit2.setApp("ewm-main-service");
        hit2.setUri("/events/1");
        hit2.setIp("192.163.0.2");
        hit2.setTimestamp(LocalDateTime.of(2022, 9, 6, 11, 0, 26));

        EndpointHit hit3 = new EndpointHit();
        hit3.setApp("ewm-main-service");
        hit3.setUri("/events/2");
        hit3.setIp("192.163.0.1");
        hit3.setTimestamp(LocalDateTime.of(2022, 9, 6, 12, 0, 0));

        EndpointHit hit4 = new EndpointHit();
        hit4.setApp("other-service");
        hit4.setUri("/events/1");
        hit4.setIp("192.163.0.3");
        hit4.setTimestamp(LocalDateTime.of(2022, 9, 6, 13, 0, 0));

        repository.saveAll(List.of(hit1, hit2, hit3, hit4));
    }

    @Test
    void findStats_WithUrisFilter_ShouldReturnCorrectCount() {
        // Arrange
        List<String> uris = List.of("/events/1");

        // Act
        List<ViewStatsDto> result = repository.findStats(start, end, uris);

        // Assert
        assertThat(result).hasSize(1);
        ViewStatsDto stats = result.get(0);
        assertThat(stats.getUri()).isEqualTo("/events/1");
        assertThat(stats.getHits()).isEqualTo(3L); // 3 хита для /events/1 (все приложения)
    }

    @Test
    void findUniqueStats_WithUrisFilter_ShouldReturnUniqueCount() {
        // Arrange
        List<String> uris = List.of("/events/1");

        // Act
        List<ViewStatsDto> result = repository.findUniqueStats(start, end, uris);

        // Assert
        assertThat(result).hasSize(1);
        ViewStatsDto stats = result.get(0);
        assertThat(stats.getUri()).isEqualTo("/events/1");
        assertThat(stats.getHits()).isEqualTo(3L); // 3 уникальных IP для /events/1
    }

    @Test
    void findStats_WithoutUris_ShouldReturnAllStats() {
        // Arrange — null означает все URI
        List<String> uris = null;

        // Act
        List<ViewStatsDto> result = repository.findStats(start, end, uris);

        // Assert — ожидаем 2 записи: /events/1 и /events/2
        assertThat(result).hasSize(2);

        ViewStatsDto events1Stats = result.stream()
                .filter(s -> s.getUri().equals("/events/1"))
                .findFirst()
                .orElse(null);

        assertThat(events1Stats).isNotNull();
        assertThat(events1Stats.getApp()).isEqualTo("All Apps"); // или ''
        assertThat(events1Stats.getHits()).isEqualTo(3L); // все хиты для /events/1

        ViewStatsDto events2Stats = result.stream()
                .filter(s -> s.getUri().equals("/events/2"))
                .findFirst()
                .orElse(null);

        assertThat(events2Stats).isNotNull();
        assertThat(events2Stats.getApp()).isEqualTo("All Apps");
        assertThat(events2Stats.getHits()).isEqualTo(1L);
    }


    @Test
    void findUniqueStats_MultipleUris_ShouldFilterCorrectly() {
        // Arrange
        List<String> uris = List.of("/events/1", "/events/2");

        // Act
        List<ViewStatsDto> result = repository.findUniqueStats(start, end, uris);

        // Assert
        assertThat(result).hasSize(2);

        ViewStatsDto events1Stats = result.stream()
                .filter(s -> s.getUri().equals("/events/1"))
                .findFirst()
                .orElse(null);

        assertThat(events1Stats).isNotNull();
        assertThat(events1Stats.getHits()).isEqualTo(3L); // 3 уникальных IP для /events/1

        assertThat(events1Stats.getApp()).isEqualTo("All Apps");

        ViewStatsDto events2Stats = result.stream()
                .filter(s -> s.getUri().equals("/events/2"))
                .findFirst()
                .orElse(null);

        assertThat(events2Stats).isNotNull();
        assertThat(events2Stats.getHits()).isEqualTo(1L); // 1 уникальный IP для /events/2
        assertThat(events2Stats.getApp()).isEqualTo("All Apps");
    }


    @Test
    void findStats_OutsideTimeRange_ShouldReturnEmpty() {
        // Arrange — временной диапазон не пересекается с данными
        LocalDateTime startOutside = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        LocalDateTime endOutside = LocalDateTime.of(2023, 1, 2, 23, 59, 59);
        List<String> uris = List.of("/events/1");

        // Act
        List<ViewStatsDto> result = repository.findStats(startOutside, endOutside, uris);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findUniqueStats_SameIpMultipleHits_ShouldCountAsOne() {
        // Arrange — создаём несколько хитов с одинаковым IP
        repository.deleteAll(); // очищаем перед тестом

        EndpointHit sameIpHit1 = new EndpointHit();
        sameIpHit1.setApp("ewm-main-service");
        sameIpHit1.setUri("/events/3");
        sameIpHit1.setIp("192.163.0.5");
        sameIpHit1.setTimestamp(LocalDateTime.of(2022, 9, 6, 14, 0, 0));

        EndpointHit sameIpHit2 = new EndpointHit();
        sameIpHit2.setApp("ewm-main-service");
        sameIpHit2.setUri("/events/3");
        sameIpHit2.setIp("192.163.0.5"); // тот же IP
        sameIpHit2.setTimestamp(LocalDateTime.of(2022, 9, 6, 15, 0, 0));

        repository.saveAll(List.of(sameIpHit1, sameIpHit2));

        List<String> uris = List.of("/events/3");

        // Act
        List<ViewStatsDto> result = repository.findUniqueStats(start, end, uris);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUri()).isEqualTo("/events/3");
        assertThat(result.get(0).getHits()).isEqualTo(1L); // несмотря на 2 хита, уникальный IP — 1
    }

    @Test
    void findStats_WithMultipleUris_ShouldReturnSeparateStats() {
        // Arrange
        List<String> uris = List.of("/events/1", "/events/2");

        // Act
        List<ViewStatsDto> result = repository.findStats(start, end, uris);

        // Assert
        assertThat(result).hasSize(2);

        ViewStatsDto events1Stats = result.stream()
                .filter(s -> s.getUri().equals("/events/1"))
                .findFirst()
                .orElse(null);

        assertThat(events1Stats).isNotNull();
        assertThat(events1Stats.getHits()).isEqualTo(3L);

        ViewStatsDto events2Stats = result.stream()
                .filter(s -> s.getUri().equals("/events/2"))
                .findFirst()
                .orElse(null);

        assertThat(events2Stats).isNotNull();
        assertThat(events2Stats.getHits()).isEqualTo(1L);
    }

    @Test
    void findUniqueStats_WithSameIpDifferentUris_ShouldCountSeparately() {
        // Arrange — один IP, но разные URI
        repository.deleteAll(); // очищаем перед тестом

        EndpointHit hit1 = new EndpointHit();
        hit1.setApp("ewm-main-service");
        hit1.setUri("/events/4");
        hit1.setIp("192.163.0.6");
        hit1.setTimestamp(LocalDateTime.of(2022, 9, 6, 16, 0, 0));

        EndpointHit hit2 = new EndpointHit();
        hit2.setApp("ewm-main-service");
        hit2.setUri("/events/5");
        hit2.setIp("192.163.0.6"); // тот же IP
        hit2.setTimestamp(LocalDateTime.of(2022, 9, 6, 17, 0, 0));

        repository.saveAll(List.of(hit1, hit2));

        List<String> uris = List.of("/events/4", "/events/5");

        // Act
        List<ViewStatsDto> result = repository.findUniqueStats(start, end, uris);

        // Assert — каждый URI должен учитываться отдельно
        assertThat(result).hasSize(2);

        ViewStatsDto events4Stats = result.stream()
                .filter(s -> s.getUri().equals("/events/4"))
                .findFirst()
                .orElse(null);

        assertThat(events4Stats).isNotNull();
        assertThat(events4Stats.getHits()).isEqualTo(1L);

        ViewStatsDto events5Stats = result.stream()
                .filter(s -> s.getUri().equals("/events/5"))
                .findFirst()
                .orElse(null);

        assertThat(events5Stats).isNotNull();
        assertThat(events5Stats.getHits()).isEqualTo(1L);
    }
}

