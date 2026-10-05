package ru.sesen.quiz;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByUserIdAndLessonId(Long userId, Long lessonId);
    List<Answer> findByUserId(Long userId);
    long countByUserIdAndCorrectTrue(Long userId);
    long countByUserId(Long userId);
}