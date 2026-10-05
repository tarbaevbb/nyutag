package ru.nyutag.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nyutag.common.NotFoundException;
import ru.nyutag.config.NyutagProperties;
import ru.nyutag.lesson.Lesson;
import ru.nyutag.lesson.LessonRepository;
import ru.nyutag.lesson.LessonItem;
import ru.nyutag.lesson.LessonItemRepository;
import ru.nyutag.progress.UserLessonProgress;
import ru.nyutag.progress.ProgressRepository;
import ru.nyutag.progress.ProgressStatus;
import ru.nyutag.quiz.AnswerRepository;
import ru.nyutag.user.User;
import ru.nyutag.user.UserRepository;

import java.util.List;
import java.util.Map;

@Service
public class ProgressService {
    private final ProgressRepository progressRepository;
    private final LessonRepository lessons;
    private final LessonItemRepository lessonItems;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final NyutagProperties properties;

    public ProgressService(ProgressRepository progressRepository, LessonRepository lessons,
                           LessonItemRepository lessonItems, AnswerRepository answerRepository,
                           UserRepository userRepository, NyutagProperties properties) {
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