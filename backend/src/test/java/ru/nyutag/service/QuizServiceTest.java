package ru.nyutag.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.nyutag.common.InvalidAnswerException;
import ru.nyutag.common.NotFoundException;
import ru.nyutag.config.NyutagProperties;
import ru.nyutag.lesson.LessonItem;
import ru.nyutag.lesson.LessonItemRepository;
import ru.nyutag.quiz.Answer;
import ru.nyutag.quiz.AnswerRepository;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class QuizServiceTest {

    @Mock
    private LessonItemRepository lessonItems;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private NyutagProperties nyutagProperties;

    @InjectMocks
    private QuizService quizService;

    private LessonItem testItem;

    @BeforeEach
    void setUp() {
        testItem = new LessonItem();
        testItem.setId(1L);
        testItem.setLessonId(1L);
        testItem.setType(ru.nyutag.lesson.LessonType.MULTIPLE_CHOICE);
        testItem.setPrompt("Test question");
        testItem.setCorrectOptionIndex(1);
        testItem.setCorrectAnswerText("Correct answer");
        testItem.setExplanation("This is the explanation");

        NyutagProperties.Xp xp = new NyutagProperties().getXp();
        when(nyutagProperties.getXp()).thenReturn(xp);
    }

    @Test
    void submitAnswer_correct() {
        when(lessonItems.findById(anyLong())).thenReturn(Optional.of(testItem));

        Map<String, Object> result = quizService.submitAnswer(1L, 1L, Map.of("selectedOptionIndex", 1));

        assertTrue((Boolean) result.get("correct"));
        assertEquals(1, result.get("correctOptionIndex"));
        assertEquals("Correct answer", result.get("correctAnswerText"));
        assertEquals("This is the explanation", result.get("explanation"));
        int expectedXp = nyutagProperties.getXp().getCorrectAnswer();
        assertEquals(expectedXp, result.get("xpAwarded"));
        verify(answerRepository).save(any(Answer.class));
    }

    @Test
    void submitAnswer_incorrect() {
        when(lessonItems.findById(anyLong())).thenReturn(Optional.of(testItem));

        Map<String, Object> result = quizService.submitAnswer(1L, 1L, Map.of("selectedOptionIndex", 0));

        assertFalse((Boolean) result.get("correct"));
        assertEquals(0, result.get("xpAwarded"));
        verify(answerRepository).save(any(Answer.class));
    }

    @Test
    void submitAnswer_noAnswer() {
        when(lessonItems.findById(anyLong())).thenReturn(Optional.of(testItem));

        assertThrows(InvalidAnswerException.class, () ->
                quizService.submitAnswer(1L, 1L, Map.of()));
    }

    @Test
    void submitAnswer_itemNotFound() {
        lenient().when(lessonItems.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                quizService.submitAnswer(1L, 999L, Map.of("selectedOptionIndex", 0)));
    }
}