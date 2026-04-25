package ru.practicum.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {

    User save(User user);

    /**
     * аннотация @Modifying для корректной обработки нативного запроса
     * явно указывает на изменение данных в базе
     *
     * Кастомный метод для уверенности что user действительно удален
     */
    @Modifying
    @Query("DELETE FROM User u WHERE u.id = :userId")
    int deleteByIdAndReturnRow(@Param("userId") Long userId);
}
