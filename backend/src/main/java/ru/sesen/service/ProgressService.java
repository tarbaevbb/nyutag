package ru.sesen.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sesen.common.NotFoundException;
import ru.sesen.config.SesenProperties;
import ru.sesen.lesson.Lesson;
import ru.sesen.lesson.LessonRepository;
import ru.sesen.lesson.LessonItem;
import ru.sesen.lesson.LessonItemRepository;
import ru.sesen.progress.UserLessonProgress;
import ru.sesen.progress.ProgressRepository;
import ru.sesen.progress.ProgressStatus;
import ru.sesen.quiz.AnswerRepository;
import ru.sesen.user.User;
import ru.sesen.user.UserRepository;

import java.util.List;
import java.util.Map;

@Service
public class ProgressService {
    private final ProgressRepository progressRepository;
    private final LessonRepository lessons;
    private final LessonItemRepository lessonItems;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final SesenProperties properties;

    public ProgressService(ProgressRepository progressRepository, LessonRepository lessons,
                           LessonItemRepository lessonItems, AnswerRepository answerRepository,
                           UserRepository userRepository, SesenProperties properties) {
        this.progressRepository = progressRepository;
        this.lessons = lessons;
        this.lessonItems = lessonItems;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProgress(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<UserLessonProgress> progresses = progressRepository.findByUserId(userId);
        
        List<Lesson> allLessons = lessons.findByCourseIdOrderByOrderIndexAsc(
                progresses.stream().findFirst()
                        .map(p -> lessons.findById(p.getLessonId()))
                        .map(o -> o.map(Lesson::getCourseId).orElse(null))
                        .orElse(null));

        return Map.of("progress", progresses, "lessons", allLessons);
    }
}