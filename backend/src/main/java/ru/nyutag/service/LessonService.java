package ru.nyutag.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nyutag.common.LessonUnavailableException;
import ru.nyutag.common.NotFoundException;
import ru.nyutag.config.NyutagProperties;
import ru.nyutag.lesson.*;
import ru.nyutag.progress.ProgressStatus;
import ru.nyutag.progress.UserLessonProgress;
import ru.nyutag.progress.ProgressRepository;
import ru.nyutag.user.User;
import ru.nyutag.user.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class LessonService {
    private final LessonRepository lessons;
    private final LessonItemRepository lessonItems;
    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final NyutagProperties properties;

    public LessonService(LessonRepository lessons, LessonItemRepository lessonItems,
                         ProgressRepository progressRepository, UserRepository userRepository,
                         NyutagProperties properties) {
        this.lessons = lessons;
        this.lessonItems = lessonItems;
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public List<Lesson> findByCourseId(Long courseId) {
        return lessons.findByCourseIdOrderByOrderIndexAsc(courseId);
    }

    @Transactional(readOnly = true)
    public Optional<Lesson> findById(Long lessonId) {
        return lessons.findById(lessonId);
    }

    @Transactional(readOnly = true)
    public List<LessonItem> findItemsByLessonId(Long lessonId) {
        return lessonItems.findByLessonIdOrderByOrderIndexAsc(lessonId);
    }

    @Transactional(readOnly = true)
    public Optional<LessonItem> findItemByIdAndLessonId(Long itemId, Long lessonId) {
        return lessonItems.findByIdAndLessonId(itemId, lessonId);
    }

    @Transactional(readOnly = true)
    public Optional<UserLessonProgress> getProgressForUserAndLesson(Long userId, Long lessonId) {
        return progressRepository.findByUserIdAndLessonId(userId, lessonId);
    }

    @Transactional
    public UserLessonProgress startLesson(Long userId, Long lessonId) {
        Lesson lesson = lessons.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Lesson not found: " + lessonId));

        Optional<UserLessonProgress> existing = progressRepository.findByUserIdAndLessonId(userId, lessonId);
        if (existing.isPresent()) {
            UserLessonProgress p = existing.get();
            if (p.getStatus() == ProgressStatus.COMPLETED) {
                return p;
            }
            p.setStatus(ProgressStatus.IN_PROGRESS);
            p.setProgress(p.getProgress() + 1);
            p.setUpdatedAt(Instant.now());
            return progressRepository.save(p);
        }

        UserLessonProgress p = new UserLessonProgress();
        p.setUserId(userId);
        p.setLessonId(lessonId);
        p.setStatus(ProgressStatus.IN_PROGRESS);
        p.setProgress(1);
        p.setUpdatedAt(Instant.now());
        return progressRepository.save(p);
    }

    @Transactional
    public UserLessonProgress completeLesson(Long userId, Long lessonId) {
        Lesson lesson = lessons.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Lesson not found: " + lessonId));

        Optional<UserLessonProgress> existing = progressRepository.findByUserIdAndLessonId(userId, lessonId);
        UserLessonProgress p;
        boolean isFirstCompletion = false;

        if (existing.isPresent()) {
            p = existing.get();
            if (p.getStatus() == ProgressStatus.COMPLETED) {
                // Idempotent: do not grant XP/streak again
                return p;
            }
            p.setStatus(ProgressStatus.COMPLETED);
            p.setCompletedAt(Instant.now());
            p.setUpdatedAt(Instant.now());
        } else {
            p = new UserLessonProgress();
            p.setUserId(userId);
            p.setLessonId(lessonId);
            p.setStatus(ProgressStatus.COMPLETED);
            p.setProgress(0);
            p.setCompletedAt(Instant.now());
            p.setUpdatedAt(Instant.now());
            isFirstCompletion = true;
        }

        // Grant completion XP (only on first completion)
        int xpReward = lesson.getXpReward() > 0 ? lesson.getXpReward() : properties.getLessons().getCompletionReward();
        if (isFirstCompletion) {
            p.setXpEarned(p.getXpEarned() + xpReward);
        }

        // Update user XP and lastActivityAt
        User user = userRepository.findById(userId).orElseThrow();
        user.setXp(user.getXp() + (isFirstCompletion ? xpReward : 0));
        user.setLastActivityAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        // Unlock next lesson if exists
        Lesson nextLesson = lessons.findByCourseIdOrderByOrderIndexAsc(lesson.getCourseId())
                .stream()
                .filter(l -> l.getOrderIndex() == lesson.getOrderIndex() + 1)
                .findFirst()
                .orElse(null);

        if (nextLesson != null) {
            Optional<UserLessonProgress> nextProgress = progressRepository.findByUserIdAndLessonId(userId, nextLesson.getId());
            if (nextProgress.isEmpty() || nextProgress.get().getStatus() == ProgressStatus.LOCKED) {
                UserLessonProgress np = nextProgress.orElseGet(() -> {
                    UserLessonProgress n = new UserLessonProgress();
                    n.setUserId(userId);
                    n.setLessonId(nextLesson.getId());
                    return n;
                });
                np.setStatus(ProgressStatus.AVAILABLE);
                np.setUpdatedAt(Instant.now());
                progressRepository.save(np);
            }
        }

        // Update user's current lesson
        user.setCurrentLesson((int) Math.max(user.getCurrentLesson(), lesson.getOrderIndex() + 1));
        userRepository.save(user);

        return progressRepository.save(p);
    }

    @Transactional(readOnly = true)
    public List<Lesson> getLessonsForUser(Long userId) {
        String courseCode = properties.getCourseCode();
        return lessons.findByCourseIdOrderByOrderIndexAsc(
                lessons.findByCourseIdOrderByOrderIndexAsc(Long.MIN_VALUE)
                        .stream()
                        .findFirst()
                        .map(Lesson::getCourseId)
                        .orElse(null));
    }
}