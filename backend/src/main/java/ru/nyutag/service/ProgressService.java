package ru.nyutag.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nyutag.config.NyutagProperties;
import ru.nyutag.course.Course;
import ru.nyutag.course.CourseRepository;
import ru.nyutag.lesson.Lesson;
import ru.nyutag.lesson.LessonRepository;
import ru.nyutag.progress.UserLessonProgress;
import ru.nyutag.progress.ProgressRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProgressService {
    private final ProgressRepository progressRepository;
    private final LessonRepository lessons;
    private final CourseRepository courses;
    private final NyutagProperties properties;

    public ProgressService(ProgressRepository progressRepository, LessonRepository lessons,
                           CourseRepository courses, NyutagProperties properties) {
        this.progressRepository = progressRepository;
        this.lessons = lessons;
        this.courses = courses;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProgress(Long userId) {
        List<UserLessonProgress> progresses = progressRepository.findByUserId(userId);
        List<Lesson> allLessons = courses.findByCode(properties.getCourseCode())
                .map(Course::getId)
                .map(courseId -> lessons.findByCourseIdOrderByOrderIndexAsc(courseId))
                .orElseGet(List::of);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("progress", progresses);
        result.put("lessons", allLessons);
        return result;
    }
}