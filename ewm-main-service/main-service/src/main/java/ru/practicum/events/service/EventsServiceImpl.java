package ru.practicum.events.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.StatsClient;
import ru.practicum.categories.Category;
import ru.practicum.categories.CategoryRepository;
import ru.practicum.dto.ViewStats;
import ru.practicum.dto.events.EventFullDto;
import ru.practicum.dto.events.EventShortDto;
import ru.practicum.dto.events.EventState;
import ru.practicum.dto.events.UpdateEventAdminRequest;
import ru.practicum.error.exception.ConflictException;
import ru.practicum.error.exception.NotFoundException;
import ru.practicum.events.Event;
import ru.practicum.events.EventsMapper;
import ru.practicum.events.EventsRepository;
import ru.practicum.requests.RequestRepository;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.events.EventsMapper.toEventFullDto;
import static ru.practicum.events.EventsMapper.toShortEventDto;

@Service
@RequiredArgsConstructor
@Transactional
public class EventsServiceImpl implements EventsService {
    private final EventsRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;
    private final EntityManager entityManager;

    @Override
    public List<EventShortDto> getPublishedEvents(
            String text,
            List<Long> categoryIds,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            boolean sortByViews,
            int from,
            int size
    ) {
        Pageable pageable = PageRequest.of(from / size, size);

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }

        Specification<Event> spec = Specification.where(EventSpecification.hasStatePublished())
                .and(EventSpecification.hasTextInAnnotationOrDescription(text))
                .and(EventSpecification.belongsToCategories(categoryIds))
                .and(EventSpecification.isPaid(paid))
                .and(EventSpecification.isWithinRange(rangeStart, rangeEnd));

        List<Event> events = eventRepository.findAll(spec, pageable).getContent();

        if (Boolean.TRUE.equals(onlyAvailable)) {
            events.removeIf(event -> event.getParticipantLimit() > 0 &&
                    event.getConfirmedRequests() >= event.getParticipantLimit());
        }

        if (sortByViews) {
            events.sort((e1, e2) -> Long.compare(e2.getViews(), e1.getViews()));
        }

        Map<Long, Long> requestCounts = getRequestCounts(events.stream().map(Event::getId).toList());

        return events.stream()
                .map(event -> toShortEventDto(event, requestCounts.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getPublishedEventById(Long id) {
        Event event = eventRepository.findByIdAndState(id, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));

        long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), EventState.CONFIRMED);
        event.setConfirmedRequests(confirmedRequests);

        setViewsToEvents(List.of(event));

        return toEventFullDto(event);
    }

    @Override
    public List<EventFullDto> getEvents(
            List<Long> userIds,
            List<String> states,
            List<Long> categoryIds,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            int from,
            int size
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = cb.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);

        List<Predicate> predicates = new ArrayList<>();

        if (userIds != null && !userIds.isEmpty()) {
            predicates.add(root.get("initiator").get("id").in(userIds));
        }

        if (states != null && !states.isEmpty()) {
            List<EventState> stateEnums = states.stream()
                    .map(EventState::valueOf)
                    .collect(Collectors.toList());
            predicates.add(root.get("state").in(stateEnums));
        }

        if (categoryIds != null && !categoryIds.isEmpty()) {
            predicates.add(root.get("category").get("id").in(categoryIds));
        }

        if (rangeStart != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
        }
        if (rangeEnd != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }

        query.select(root).where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(root.get("eventDate")));

        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Event> events = entityManager.createQuery(query)
                .setFirstResult((int) pageRequest.getOffset())
                .setMaxResults(pageRequest.getPageSize())
                .getResultList();

        Map<Long, Long> confirmedRequests = getConfirmedRequestsMap(events);

        setViewsToEvents(events);

        return events.stream()
                .peek(event -> event.setConfirmedRequests(confirmedRequests.getOrDefault(event.getId(), 0L)))
                .map(EventsMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case PUBLISH_EVENT -> {
                    if (!event.getState().equals(EventState.PENDING)) {
                        throw new ConflictException("Cannot publish event in state: " + event.getState());
                    }
                    if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                        throw new ConflictException("Event must be at least 1 hour after current time to be published");
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
                case REJECT_EVENT -> {
                    if (event.getState().equals(EventState.PUBLISHED)) {
                        throw new ConflictException("Cannot reject published event");
                    }
                    event.setState(EventState.CANCELED);
                }
            }
        }

        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + request.getCategory() + " was not found"));
            event.setCategory(category);
        }
        if (request.getLocation() != null) {
            event.setLocationLat(request.getLocation().getLat());
            event.setLocationLon(request.getLocation().getLon());
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }

        Event saved = eventRepository.save(event);
        saved.setConfirmedRequests(requestRepository.countByEventIdAndStatus(saved.getId(), EventState.CONFIRMED));

        setViewsToEvent(saved);

        return EventsMapper.toEventFullDto(saved);
    }

    private void setViewsToEvent(Event event) {
        List<ViewStats> stats = getStats(List.of("/events/" + event.getId()));
        long views = stats.stream().findFirst().map(ViewStats::getHits).orElse(0L);
        event.setViews(views);
    }

    private List<ViewStats> getStats(List<String> uris) {
        LocalDateTime start = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.now();

        try {
            return statsClient.getStats(start, end, uris, true);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private Map<Long, Long> getConfirmedRequestsMap(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        List<Object[]> results = requestRepository.countConfirmedRequestsByEventIds(eventIds, EventState.CONFIRMED);

        return results.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue()
                ));
    }

    private void setViewsToEvents(List<Event> events) {
        if (events.isEmpty()) return;

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        LocalDateTime start = LocalDateTime.now().minusYears(1);
        LocalDateTime end = LocalDateTime.now();

        List<ViewStats> stats = statsClient.getStats(start, end, uris, true);

        Map<String, Long> viewsMap = stats.stream()
                .collect(Collectors.toMap(
                        ViewStats::getUri,
                        ViewStats::getHits
                ));

        events.forEach(event -> {
            String uri = "/events/" + event.getId();
            Long views = viewsMap.getOrDefault(uri, 0L);
            event.setViews(views);
        });
    }

    private Map<Long, Long> getRequestCounts(List<Long> eventIds) {
        return requestRepository.countConfirmedRequestsByEventIds(eventIds, EventState.CONFIRMED).stream()
                .collect(Collectors.toMap(
                        r -> ((Number) r[0]).longValue(),
                        r -> ((Number) r[1]).longValue()
                ));
    }
}
