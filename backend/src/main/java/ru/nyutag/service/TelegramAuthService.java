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
    private final TelegramInitDataValidator validator;
    private final UserService userService;
    private final NyutagProperties properties;

    public TelegramAuthService(UserRepository users, ProgressRepository progressRepository,
                               LessonRepository lessons, TelegramInitDataValidator validator,
                               UserService userService, NyutagProperties properties) {
        this.users = users;
        this.progressRepository = progressRepository;
        this.lessons = lessons;
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
        List<Lesson> allLessons = lessons.findByCourseIdOrderByOrderIndexAsc(
                progresses.stream().findFirst()
                        .map(p -> lessons.findById(p.getLessonId()))
                        .map(o -> o.map(Lesson::getCourseId).orElse(null))
                        .orElse(null));
        int totalLessons = allLessons.size();
        int progressPercent = totalLessons > 0 ? (int) ((completed * 100) / totalLessons) : 0;

        return Map.of(
                "id", user.getId(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "username", user.getUsername(),
                "avatarUrl", user.getAvatarUrl(),
                "level", user.getLevel(),
                "xp", user.getXp(),
                "streak", user.getStreak(),
                "currentLesson", user.getCurrentLesson(),
                "progressPercent", progressPercent
        );
    }
}