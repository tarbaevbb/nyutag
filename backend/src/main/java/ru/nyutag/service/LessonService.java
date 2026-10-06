package ru.nyutag.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nyutag.common.LessonUnavailableException;
import ru.nyutag.common.NotFoundException;
import ru.nyutag.config.NyutagProperties;
import ru.nyutag.course.Course;
import ru.nyutag.course.CourseRepository;
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
    private final CourseRepository courses;
    private final NyutagProperties properties;

    public LessonService(LessonRepository lessons, LessonItemRepository lessonItems,
                         ProgressRepository progressRepository, UserRepository userRepository,
                         CourseRepository courses, NyutagProperties properties) {
        this.lessons = lessons;
        this.lessonItems = lessonItems;
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.courses = courses;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public List<Lesson> findByCourseId(Long courseId) {
        if (courseId == null) {
            return List.of();
        }
        return lessons.findByCourseIdOrderByOrderIndexAsc(courseId);
    }

    @Transactional(readOnly = true)
    public Optional<Lesson> findById(Long lessonId) {
        return lessons.findById(lessonId);
    }

    @Transactional(readOnly = true)
    public Lesson requireLesson(Long lessonId) {
        return lessons.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Lesson not found: " + lessonId));
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

    /**
     * Resolves the course configured via {@code nyutag.course-code} instead of
     * guessing it from the user's progress rows.
     */
    @Transactional(readOnly = true)
    public Optional<Course> getCurrentCourse() {
        return courses.findByCode(properties.getCourseCode());
    }

    @Transactional(readOnly = true)
    public List<Lesson> getLessonsForUser(Long userId) {
        return getCurrentCourse()
                .map(course -> lessons.findByCourseIdOrderByOrderIndexAsc(course.getId()))
                .orElseGet(List::of);
    }

    /**
     * Effective status of a lesson for a user. Lesson 1 is always AVAILABLE for a
     * brand-new user with no progress row; the next lesson becomes AVAILABLE once the
     * previous one is COMPLETED. LOCKED otherwise.
     */
    @Transactional(readOnly = true)
    public ProgressStatus resolveStatus(Long userId, Lesson lesson) {
        Optional<UserLessonProgress> progress = progressRepository.findByUserIdAndLessonId(userId, lesson.getId());
        if (progress.isPresent() && progress.get().getStatus() != ProgressStatus.LOCKED) {
            return progress.get().getStatus();
        }
        if (lesson.getOrderIndex() <= 1) {
            return ProgressStatus.AVAILABLE;
        }
        if (isPreviousCompleted(userId, lesson)) {
            return ProgressStatus.AVAILABLE;
        }
        return ProgressStatus.LOCKED;
    }

    @Transactional(readOnly = true)
    public boolean isLessonAvailable(Long userId, Lesson lesson) {
        return resolveStatus(userId, lesson) != ProgressStatus.LOCKED;
    }

    @Transactional(readOnly = true)
    public void assertLessonAvailable(Long userId, Lesson lesson) {
        if (!isLessonAvailable(userId, lesson)) {
            throw new LessonUnavailableException("Lesson not available yet");
        }
    }

    private boolean isPreviousCompleted(Long userId, Lesson lesson) {
        if (lesson.getOrderIndex() <= 1) {
            return false;
        }
        return lessons.findByCourseIdAndOrderIndex(lesson.getCourseId(), lesson.getOrderIndex() - 1)
                .flatMap(prev -> progressRepository.findByUserIdAndLessonId(userId, prev.getId()))
                .map(prevProgress -> prevProgress.getStatus() == ProgressStatus.COMPLETED)
                .orElse(false);
    }

    @Transactional
    public UserLessonProgress startLesson(Long userId, Long lessonId) {
        Lesson lesson = requireLesson(lessonId);
        assertLessonAvailable(userId, lesson);

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
    public LessonCompletionResult completeLesson(Long userId, Long lessonId) {
        Lesson lesson = requireLesson(lessonId);
        assertLessonAvailable(userId, lesson);

        Optional<UserLessonProgress> existing = progressRepository.findByUserIdAndLessonId(userId, lessonId);
        UserLessonProgress p;

        if (existing.isPresent()) {
            p = existing.get();
            if (p.getStatus() == ProgressStatus.COMPLETED) {
                // Idempotent: do not grant XP/streak again
                Lesson nextLesson = findNextLesson(lesson);
                return new LessonCompletionResult(0, false, nextLesson);
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
        }

        // Grant completion XP (only on first completion)
        int xpReward = lesson.getXpReward() > 0 ? lesson.getXpReward() : properties.getLessons().getCompletionReward();
        p.setXpEarned(p.getXpEarned() + xpReward);

        // Update user XP and lastActivityAt
        User user = userRepository.findById(userId).orElseThrow();
        user.setXp(user.getXp() + xpReward);
        user.setLastActivityAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        // Unlock next lesson if exists
        Lesson nextLesson = findNextLesson(lesson);
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
        user.setCurrentLesson(Math.max(user.getCurrentLesson(), lesson.getOrderIndex() + 1));
        userRepository.save(user);

        progressRepository.save(p);
        return new LessonCompletionResult(xpReward, true, nextLesson);
    }

    @Transactional(readOnly = true)
    public Lesson findNextLesson(Lesson lesson) {
        return lessons.findByCourseIdAndOrderIndex(lesson.getCourseId(), lesson.getOrderIndex() + 1)
                .orElse(null);
    }

    public record LessonCompletionResult(int xpEarned, boolean firstCompletion, Lesson nextLesson) {
    }
}