package ru.sesen.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sesen.common.InvalidAnswerException;
import ru.sesen.common.NotFoundException;
import ru.sesen.config.SesenProperties;
import ru.sesen.lesson.LessonItem;
import ru.sesen.lesson.LessonItemRepository;
import ru.sesen.quiz.Answer;
import ru.sesen.quiz.AnswerRepository;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class QuizService {
    private final LessonItemRepository lessonItems;
    private final AnswerRepository answerRepository;
    private final SesenProperties properties;

    public QuizService(LessonItemRepository lessonItems, AnswerRepository answerRepository,
                       SesenProperties properties) {
        this.lessonItems = lessonItems;
        this.answerRepository = answerRepository;
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

        Integer selectedOptionIndex = (Integer) answerPayload.get("selectedOptionIndex");
        String selectedText = (String) answerPayload.get("selectedText");

        if (selectedOptionIndex == null && selectedText == null) {
            throw new InvalidAnswerException("No answer provided");
        }

        boolean isCorrect;
        if (selectedOptionIndex != null) {
            isCorrect = selectedOptionIndex.equals(item.getCorrectOptionIndex());
        } else {
            isCorrect = selectedText != null && selectedText.equals(item.getCorrectAnswerText());
        }

        long responseMs = (Long) answerPayload.getOrDefault("responseMs", 0L);

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

        int xpAwarded = isCorrect ? properties.getXp().getCorrectAnswer() : 0;

        return Map.of(
                "correct", isCorrect,
                "correctOptionIndex", item.getCorrectOptionIndex(),
                "correctAnswerText", item.getCorrectAnswerText(),
                "explanation", item.getExplanation(),
                "xpAwarded", xpAwarded
        );
    }
}