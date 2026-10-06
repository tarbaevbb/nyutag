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
import ru.nyutag.user.User;
import ru.nyutag.user.UserRepository;

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
    private UserRepository userRepository;

    @Mock
    private NyutagProperties nyutagProperties;

    @InjectMocks
    private QuizService quizService;

    private LessonItem testItem;
    private User testUser;

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

        testUser = new User();
        testUser.setId(1L);
        testUser.setXp(0);

        NyutagProperties props = new NyutagProperties();
        when(nyutagProperties.getXp()).thenReturn(props.getXp());
    }

    @Test
    void submitAnswer_correct_awardsXpAndPersists() {
        when(lessonItems.findById(anyLong())).thenReturn(Optional.of(testItem));
        when(answerRepository.existsByUserIdAndLessonItemIdAndCorrectTrue(anyLong(), anyLong())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Map<String, Object> result = quizService.submitAnswer(1L, 1L, Map.of("selectedOptionIndex", 1));

        assertTrue((Boolean) result.get("correct"));
        assertEquals(1, result.get("correctOptionIndex"));
        assertEquals("Correct answer", result.get("correctAnswerText"));
        assertEquals("This is the explanation", result.get("explanation"));
        assertEquals(10, result.get("xpAwarded"));
        assertEquals(10, testUser.getXp());
        verify(answerRepository).save(any(Answer.class));
        verify(userRepository).save(testUser);
    }

    @Test
    void submitAnswer_correct_secondTime_noXp() {
        when(lessonItems.findById(anyLong())).thenReturn(Optional.of(testItem));
        when(answerRepository.existsByUserIdAndLessonItemIdAndCorrectTrue(anyLong(), anyLong())).thenReturn(true);

        Map<String, Object> result = quizService.submitAnswer(1L, 1L, Map.of("selectedOptionIndex", 1));

        assertTrue((Boolean) result.get("correct"));
        assertEquals(0, result.get("xpAwarded"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void submitAnswer_incorrect_noXp() {
        when(lessonItems.findById(anyLong())).thenReturn(Optional.of(testItem));

        Map<String, Object> result = quizService.submitAnswer(1L, 1L, Map.of("selectedOptionIndex", 0));

        assertFalse((Boolean) result.get("correct"));
        assertEquals(0, result.get("xpAwarded"));
        verify(userRepository, never()).save(any(User.class));
        verify(answerRepository).save(any(Answer.class));
    }

    @Test
    void submitAnswer_responseMsMissing_doesNotThrow() {
        when(lessonItems.findById(anyLong())).thenReturn(Optional.of(testItem));
        when(answerRepository.existsByUserIdAndLessonItemIdAndCorrectTrue(anyLong(), anyLong())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertDoesNotThrow(() -> quizService.submitAnswer(1L, 1L, Map.of("selectedOptionIndex", 1)));
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