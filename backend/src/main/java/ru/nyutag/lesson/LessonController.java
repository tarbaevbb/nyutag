package ru.nyutag.web;

import org.springframework.web.bind.annotation.*;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        return lessonService.getLessonsForUser(user.getId()).stream().map(lesson -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", lesson.getId());
            m.put("title", lesson.getTitle());
            m.put("subtitle", lesson.getSubtitle());
            m.put("orderIndex", lesson.getOrderIndex());
            m.put("xpReward", lesson.getXpReward());
            m.put("status", lessonService.resolveStatus(user.getId(), lesson).toString());
            return m;
        }).toList();
    }

    @GetMapping("/lessons/{id}")
    public Map<String, Object> lesson(@PathVariable Long id) {
        User user = UserContext.get();
        Lesson lesson = lessonService.requireLesson(id);
        lessonService.assertLessonAvailable(user.getId(), lesson);

        List<Map<String, Object>> itemsDto = lessonService.findItemsByLessonId(id).stream()
                .map(this::toItemDto)
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", lesson.getId());
        result.put("title", lesson.getTitle());
        result.put("subtitle", lesson.getSubtitle());
        result.put("items", itemsDto);
        return result;
    }

    @PostMapping("/lessons/{id}/start")
    public Map<String, Object> startLesson(@PathVariable Long id) {
        User user = UserContext.get();
        UserLessonProgress progress = lessonService.startLesson(user.getId(), id);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("progress", Map.of("status", progress.getStatus().toString()));
        result.put("items", lessonService.findItemsByLessonId(id).stream().map(this::toItemDto).toList());
        return result;
    }

    @PostMapping("/lessons/{lessonId}/items/{itemId}/answer")
    public Map<String, Object> submitAnswer(@PathVariable Long lessonId,
                                           @PathVariable Long itemId,
                                           @RequestBody Map<String, Object> answerPayload) {
        User user = UserContext.get();

        lessonService.findItemByIdAndLessonId(itemId, lessonId)
                .orElseThrow(() -> new NotFoundException("Item not found in this lesson"));
        Lesson lesson = lessonService.requireLesson(lessonId);
        lessonService.assertLessonAvailable(user.getId(), lesson);

        return quizService.submitAnswer(user.getId(), itemId, answerPayload);
    }

    @PostMapping("/lessons/{id}/complete")
    public Map<String, Object> completeLesson(@PathVariable Long id) {
        User user = UserContext.get();
        LessonService.LessonCompletionResult completion = lessonService.completeLesson(user.getId(), id);

        // Update streak only on first completion
        if (completion.firstCompletion()) {
            streakService.updateStreak(user.getId());
        }

        Map<String, Object> profile = telegramAuthService.getProfile(user.getId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("xp", profile.get("xp"));
        result.put("xpEarned", completion.xpEarned());
        result.put("streak", profile.get("streak"));
        result.put("nextLessonId", completion.nextLesson() != null ? completion.nextLesson().getId() : null);
        result.put("lessonCompleted", true);
        return result;
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

    /**
     * Builds a null-safe item DTO. Uses a mutable map because several optional fields
     * can legitimately be null for a given item type.
     */
    private Map<String, Object> toItemDto(LessonItem item) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", item.getId());
        m.put("orderIndex", item.getOrderIndex());
        m.put("type", item.getType().toString());
        m.put("prompt", item.getPrompt());
        m.put("promptTranslation", item.getPromptTranslation());
        m.put("buryat", item.getBuryat());
        m.put("russian", item.getRussian());
        m.put("audioUrl", item.getAudioUrl());
        if (item.getOptions() != null) {
            m.put("options", item.getOptions());
        }
        return m;
    }
}