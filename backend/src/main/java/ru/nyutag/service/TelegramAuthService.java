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
import ru.nyutag.progress.ProgressStatus;
import ru.nyutag.telegram.TelegramUser;
import ru.nyutag.telegram.TelegramInitDataValidator;
import ru.nyutag.user.User;
import ru.nyutag.user.UserContext;
import ru.nyutag.user.UserRepository;

import java.util.List;
import java.util.Map;

@Service
public class TelegramAuthService {
    private final UserRepository users;
    private final ProgressRepository progressRepository;
    private final LessonRepository lessons;
    private final CourseRepository courses;
    private final TelegramInitDataValidator validator;
    private final UserService userService;
    private final NyutagProperties properties;

    public TelegramAuthService(UserRepository users, ProgressRepository progressRepository,
                               LessonRepository lessons, CourseRepository courses,
                               TelegramInitDataValidator validator,
                               UserService userService, NyutagProperties properties) {
        this.users = users;
        this.progressRepository = progressRepository;
        this.lessons = lessons;
        this.courses = courses;
        this.validator = validator;
        this.userService = userService;
        this.properties = properties;
    }

    @Transactional
    public User authenticate(String initData) {
        TelegramUser tg = validator.validate(initData);
        return userService.upsertFromTelegram(tg);
    }

    @Transactional
    public User authenticateDev() {
        if (!properties.getDevAuth().isEnabled()) {
            return null;
        }
        return userService.getOrCreateDevUser(properties.getDevAuth().getTelegramId());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProfile(Long userId) {
        User user = users.findById(userId).orElseThrow();
        List<UserLessonProgress> progresses = progressRepository.findByUserId(userId);
        long completed = progresses.stream()
                .filter(p -> p.getStatus() == ProgressStatus.COMPLETED)
                .count();
        List<Lesson> allLessons = courses.findByCode(properties.getCourseCode())
                .map(Course::getId)
                .map(courseId -> lessons.findByCourseIdOrderByOrderIndexAsc(courseId))
                .orElseGet(List::of);
        int totalLessons = allLessons.size();
        int progressPercent = totalLessons > 0 ? (int) ((completed * 100) / totalLessons) : 0;

        // "First launch" signal for the frontend: no recorded activity and no progress rows.
        boolean isNewUser = user.getLastActivityAt() == null && progresses.isEmpty();

        Map<String, Object> profile = new java.util.LinkedHashMap<>();
        profile.put("id", user.getId());
        profile.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
        profile.put("lastName", user.getLastName() != null ? user.getLastName() : "");
        profile.put("username", user.getUsername() != null ? user.getUsername() : "");
        profile.put("avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
        profile.put("level", user.getLevel());
        profile.put("xp", user.getXp());
        profile.put("streak", user.getStreak());
        profile.put("currentLesson", user.getCurrentLesson());
        profile.put("progressPercent", progressPercent);
        profile.put("isNewUser", isNewUser);
        return profile;
    }
}
