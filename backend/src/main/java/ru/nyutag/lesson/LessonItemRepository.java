package ru.nyutag.lesson;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonItemRepository extends JpaRepository<LessonItem, Long> {
    List<LessonItem> findByLessonIdOrderByOrderIndexAsc(Long lessonId);
    Optional<LessonItem> findByIdAndLessonId(Long id, Long lessonId);
}