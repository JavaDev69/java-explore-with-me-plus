package ru.practicum.comments;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByEvent_IdAndStatus(Long eventId, CommentStatus status, Pageable pageable);

    List<Comment> findByEvent_Id(Long eventId, Pageable pageable);

    boolean existsByEvent_IdAndAuthor_IdAndStatusIn(Long eventId, Long userId, List<CommentStatus> statuses);
}
