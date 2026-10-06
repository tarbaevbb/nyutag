package ru.nyutag.lesson;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourseIdOrderByOrderIndexAsc(Long courseId);
    Optional<Lesson> findByCourseIdAndOrderIndex(Long courseId, int orderIndex);
}