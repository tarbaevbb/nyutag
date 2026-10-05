package ru.nyutag.progress;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgressRepository extends JpaRepository<UserLessonProgress, Long> {
    Optional<UserLessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);
    List<UserLessonProgress> findByUserId(Long userId);
    long countByUserIdAndStatus(Long userId, ProgressStatus status);
}