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
import ru.sesen.telegram.TelegramUser;
import ru.sesen.telegram.TelegramInitDataValidator;
import ru.sesen.user.User;
import ru.sesen.user.UserContext;
import ru.sesen.user.UserRepository;

import java.util.List;
import java.util.Map;

@Service
public class TelegramAuthService {
    private final UserRepository users;
    private final ProgressRepository progressRepository;
    private final LessonRepository lessons;
    private final TelegramInitDataValidator validator;
    private final UserService userService;
    private final SesenProperties properties;

    public TelegramAuthService(UserRepository users, ProgressRepository progressRepository,
                               LessonRepository lessons, TelegramInitDataValidator validator,
                               UserService userService, SesenProperties properties) {
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