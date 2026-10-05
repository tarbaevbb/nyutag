package ru.nyutag.web;

import org.springframework.web.bind.annotation.*;
import ru.nyutag.common.LessonUnavailableException;
import ru.nyutag.common.NotFoundException;
import ru.nyutag.lesson.Lesson;
import ru.nyutag.lesson.LessonItem;
import ru.nyutag.progress.UserLessonProgress;
import ru.nyutag.service.LessonService;
import ru.nyutag.service.ProgressService;
import ru.nyutag.service.QuizService;
import ru.nyutag.service.StreakService;
import ru.nyutag.service.TelegramAuthService;
import ru.nyutag.user.User;
import ru.nyutag.user.UserContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class LessonController {
    private final LessonService lessonService;
    private final TelegramAuthService telegramAuthService;
    private final QuizService quizService;
    private final StreakService streakService;
    private final ProgressService progressService;

    public LessonController(LessonService lessonService, TelegramAuthService telegramAuthService,
                           QuizService quizService, StreakService streakService,
                           ProgressService progressService) {
        this.lessonService = lessonService;
        this.telegramAuthService = telegramAuthService;
        this.quizService = quizService;
        this.streakService = streakService;
        this.progressService = progressService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok", "app", "nyutag");
    }

    @GetMapping("/me")
    public Map<String, Object> me() {
        User user = UserContext.get();
        return telegramAuthService.getProfile(user.getId());
    }

    @GetMapping("/lessons")
    public List<Map<String, Object>> lessons() {
        User user = UserContext.get();
        List<Lesson> allLessons = lessonService.findByCourseId(
                lessonService.findByCourseId(Long.MIN_VALUE).stream()
                        .findFirst()
                        .map(Lesson::getCourseId)
                        .orElse(null));
        
        return allLessons.stream().map(lesson -> {
            UserLessonProgress progress = lessonService.getProgressForUserAndLesson(
                    user.getId(), lesson.getId()).orElse(null);
            
            Map<String, Object> m = Map.of(
                    "id", lesson.getId(),
                    "title", lesson.getTitle(),
                    "subtitle", lesson.getSubtitle(),
                    "orderIndex", lesson.getOrderIndex(),
                    "xpReward", lesson.getXpReward(),
                    "status", progress != null ? progress.getStatus().toString() : "LOCKED"
            );
            return m;
        }).toList();
    }

    @GetMapping("/lessons/{id}")
    public Map<String, Object> lesson(@PathVariable Long id) {
        User user = UserContext.get();
        Lesson lesson = lessonService.findById(id)
                .orElseThrow(() -> new NotFoundException("Lesson not found: " + id));

        UserLessonProgress progress = lessonService.getProgressForUserAndLesson(
                user.getId(), id).orElse(null);

        if (progress == null && lesson.getOrderIndex() > 1) {
            Optional<UserLessonProgress> prevProgressOpt = lessonService.getProgressForUserAndLesson(
                    user.getId(),
                    lessonService.findById(id - 1).map(l -> l.getId()).orElse(null));
            UserLessonProgress prevProgress = prevProgressOpt.orElse(null);
            if (prevProgress == null || prevProgress.getStatus() != ru.nyutag.progress.ProgressStatus.COMPLETED) {
                throw new LessonUnavailableException("Lesson not available yet");
            }
        }

        List<LessonItem> items = lessonService.findItemsByLessonId(id);
        
        // Strip sensitive fields from items
        List<Map<String, Object>> itemsDto = items.stream().map(item -> {
            Map<String, Object> m = Map.of(
                    "id", item.getId(),
                    "orderIndex", item.getOrderIndex(),
                    "type", item.getType().toString(),
                    "prompt", item.getPrompt(),
                    "promptTranslation", item.getPromptTranslation(),
                    "buryat", item.getBuryat(),
                    "russian", item.getRussian(),
                    "audioUrl", item.getAudioUrl()
            );
            if (item.getOptions() != null) {
                m.put("options", item.getOptions());
            }
            return m;
        }).toList();

        return Map.of(
                "id", lesson.getId(),
                "title", lesson.getTitle(),
                "subtitle", lesson.getSubtitle(),
                "items", itemsDto
        );
    }

    @PostMapping("/lessons/{id}/start")
    public Map<String, Object> startLesson(@PathVariable Long id) {
        User user = UserContext.get();
        UserLessonProgress progress = lessonService.startLesson(user.getId(), id);
        List<LessonItem> items = lessonService.findItemsByLessonId(id);

        return Map.of(
                "progress", Map.of("status", progress.getStatus().toString()),
                "items", items.stream().map(item -> {
                    Map<String, Object> m = Map.of(
                            "id", item.getId(),
                            "orderIndex", item.getOrderIndex(),
                            "type", item.getType().toString(),
                            "prompt", item.getPrompt(),
                            "buryat", item.getBuryat(),
                            "russian", item.getRussian(),
                            "audioUrl", item.getAudioUrl()
                    );
                    if (item.getOptions() != null) {
                        m.put("options", item.getOptions());
                    }
                    return m;
                }).toList()
        );
    }

    @PostMapping("/lessons/{lessonId}/items/{itemId}/answer")
    public Map<String, Object> submitAnswer(@PathVariable Long lessonId,
                                           @PathVariable Long itemId,
                                           @RequestBody Map<String, Object> answerPayload) {
        User user = UserContext.get();
        
        // Verify item belongs to lesson
        lessonService.findItemByIdAndLessonId(itemId, lessonId)
                .orElseThrow(() -> new NotFoundException("Item not found in this lesson"));

        return quizService.submitAnswer(user.getId(), itemId, answerPayload);
    }

    @PostMapping("/lessons/{id}/complete")
    public Map<String, Object> completeLesson(@PathVariable Long id) {
        User user = UserContext.get();
        UserLessonProgress progress = lessonService.completeLesson(user.getId(), id);
        
        // Update streak
        streakService.updateStreak(user.getId());

        // Get updated stats
        Map<String, Object> profile = telegramAuthService.getProfile(user.getId());
        
        // Get next lesson
        Lesson nextLesson = lessonService.findByCourseId(
                lessonService.findByCourseId(Long.MIN_VALUE).stream()
                        .findFirst()
                        .map(Lesson::getCourseId)
                        .orElse(null))
                .stream()
                .filter(l -> l.getOrderIndex() == lessonService.findById(id).map(Lesson::getOrderIndex).get() + 1)
                .findFirst()
                .orElse(null);

        return Map.of(
                "xp", profile.get("xp"),
                "streak", profile.get("streak"),
                "nextLessonId", nextLesson != null ? nextLesson.getId() : null,
                "lessonCompleted", true
        );
    }

    @GetMapping("/progress")
    public Map<String, Object> progress() {
        User user = UserContext.get();
        return progressService.getProgress(user.getId());
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        User user = UserContext.get();
        return streakService.getStats(user.getId());
    }
}