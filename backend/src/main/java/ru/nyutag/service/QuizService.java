package ru.nyutag.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nyutag.common.InvalidAnswerException;
import ru.nyutag.common.NotFoundException;
import ru.nyutag.config.NyutagProperties;
import ru.nyutag.lesson.LessonItem;
import ru.nyutag.lesson.LessonItemRepository;
import ru.nyutag.quiz.Answer;
import ru.nyutag.quiz.AnswerRepository;
import ru.nyutag.user.User;
import ru.nyutag.user.UserRepository;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class QuizService {
    private final LessonItemRepository lessonItems;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final NyutagProperties properties;

    public QuizService(LessonItemRepository lessonItems, AnswerRepository answerRepository,
                       UserRepository userRepository, NyutagProperties properties) {
        this.lessonItems = lessonItems;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public LessonItem getLessonItem(Long itemId) {
        return lessonItems.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Lesson item not found: " + itemId));
    }

    @Transactional
    public Map<String, Object> submitAnswer(Long userId, Long itemId, Map<String, Object> answerPayload) {
        LessonItem item = lessonItems.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Lesson item not found: " + itemId));

        Integer selectedOptionIndex = asInteger(answerPayload.get("selectedOptionIndex"));
        String selectedText = answerPayload.get("selectedText") != null
                ? String.valueOf(answerPayload.get("selectedText"))
                : null;

        if (selectedOptionIndex == null && (selectedText == null || selectedText.isBlank())) {
            throw new InvalidAnswerException("No answer provided");
        }

        boolean isCorrect;
        if (selectedOptionIndex != null) {
            isCorrect = selectedOptionIndex.equals(item.getCorrectOptionIndex());
        } else {
            isCorrect = selectedText != null && selectedText.equals(item.getCorrectAnswerText());
        }

        long responseMs = asLong(answerPayload.get("responseMs"), 0L);

        // XP is awarded only for the first correct answer to a given item, so replaying
        // the same item cannot farm XP repeatedly (spec §15).
        boolean xpAlreadyEarned = isCorrect
                && answerRepository.existsByUserIdAndLessonItemIdAndCorrectTrue(userId, itemId);

        Answer answer = new Answer();
        answer.setUserId(userId);
        answer.setLessonId(item.getLessonId());
        answer.setLessonItemId(itemId);
        answer.setSelectedOptionIndex(selectedOptionIndex);
        answer.setSelectedText(selectedText);
        answer.setCorrect(isCorrect);
        answer.setResponseMs((int) responseMs);
        answer.setAnsweredAt(Instant.now());
        answerRepository.save(answer);

        int xpAwarded = (isCorrect && !xpAlreadyEarned) ? properties.getXp().getCorrectAnswer() : 0;
        if (xpAwarded > 0) {
            User user = userRepository.findById(userId).orElseThrow();
            user.setXp(user.getXp() + xpAwarded);
            user.setLastActivityAt(Instant.now());
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("correct", isCorrect);
        response.put("correctOptionIndex", item.getCorrectOptionIndex());
        response.put("correctAnswerText", item.getCorrectAnswerText());
        response.put("explanation", item.getExplanation());
        response.put("xpAwarded", xpAwarded);
        return response;
    }

    private Integer asInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.intValue();
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private long asLong(Object value, long fallback) {
        if (value instanceof Number number) return number.longValue();
        if (value != null) {
            try {
                return Long.parseLong(String.valueOf(value).trim());
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return fallback;
    }
}