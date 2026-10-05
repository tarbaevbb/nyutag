package ru.nyutag.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nyutag.config.NyutagProperties;
import ru.nyutag.lesson.Lesson;
import ru.nyutag.lesson.LessonRepository;
import ru.nyutag.lesson.LessonItem;
import ru.nyutag.lesson.LessonItemRepository;
import ru.nyutag.progress.UserLessonProgress;
import ru.nyutag.progress.ProgressRepository;
import ru.nyutag.progress.ProgressStatus;
import ru.nyutag.quiz.Answer;
import ru.nyutag.quiz.AnswerRepository;
import ru.nyutag.user.User;
import ru.nyutag.user.UserRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
public class StreakService {
    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessons;
    private final LessonItemRepository lessonItems;
    private final AnswerRepository answerRepository;
    private final NyutagProperties properties;

    public StreakService(ProgressRepository progressRepository, UserRepository userRepository,
                         LessonRepository lessons, LessonItemRepository lessonItems,
                         AnswerRepository answerRepository, NyutagProperties properties) {
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.lessons = lessons;
        this.lessonItems = lessonItems;
        this.answerRepository = answerRepository;
        this.properties = properties;
    }

    @Transactional
    public void updateStreak(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Instant now = Instant.now();
        LocalDate today = LocalDate.now(ZoneId.of("UTC"));

        if (user.getLastActivityAt() == null) {
            // First activity
            user.setStreak(1);
            user.setLastActivityAt(now);
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
            return;
        }

        LocalDate lastActivityDate = user.getLastActivityAt().atZone(ZoneId.of("UTC")).toLocalDate();

        if (lastActivityDate.isEqual(today)) {
            // Already active today, do not increment streak
            user.setLastActivityAt(now);
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
            return;
        }

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(lastActivityDate, today);

        if (daysBetween == 1) {
            user.setStreak(user.getStreak() + 1);
        } else if (daysBetween > 1) {
            user.setStreak(1);
        }

        user.setLastActivityAt(now);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStats(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        long totalAnswers = answerRepository.countByUserId(userId);
        long correctAnswers = answerRepository.countByUserIdAndCorrectTrue(userId);
        int correctPercent = totalAnswers > 0 ? (int) ((correctAnswers * 100) / totalAnswers) : 0;

        List<UserLessonProgress> progresses = progressRepository.findByUserId(userId);
        long completedLessons = progresses.stream()
                .filter(p -> p.getStatus() == ProgressStatus.COMPLETED)
                .count();

        return Map.of(
                "xp", user.getXp(),
                "streak", user.getStreak(),
                "completedLessons", completedLessons,
                "correctPercent", correctPercent
        );
    }
}