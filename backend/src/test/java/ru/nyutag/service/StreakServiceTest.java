package ru.nyutag.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.nyutag.config.NyutagProperties;
import ru.nyutag.user.User;
import ru.nyutag.user.UserRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StreakServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StreakService streakService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setTelegramId(123L);
        testUser.setFirstName("Test");
        testUser.setStreak(0);
        testUser.setXp(0);
        testUser.setCurrentLesson(1);
    }

    @Test
    void updateStreak_firstActivity() {
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(testUser));

        streakService.updateStreak(1L);

        assertEquals(1, testUser.getStreak());
        assertNotNull(testUser.getLastActivityAt());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateStreak_sameDay_noIncrement() {
        testUser.setStreak(3);
        testUser.setLastActivityAt(LocalDate.now(ZoneId.of("UTC")).atTime(1, 0).atZone(ZoneId.of("UTC")).toInstant());

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(testUser));

        streakService.updateStreak(1L);

        assertEquals(3, testUser.getStreak());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateStreak_yesterday_increment() {
        testUser.setStreak(5);
        LocalDate yesterday = LocalDate.now(ZoneId.of("UTC")).minusDays(1);
        testUser.setLastActivityAt(yesterday.atTime(12, 0).atZone(ZoneId.of("UTC")).toInstant());

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(testUser));

        streakService.updateStreak(1L);

        assertEquals(6, testUser.getStreak());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateStreak_gapReset() {
        testUser.setStreak(10);
        LocalDate threeDaysAgo = LocalDate.now(ZoneId.of("UTC")).minusDays(3);
        testUser.setLastActivityAt(threeDaysAgo.atTime(12, 0).atZone(ZoneId.of("UTC")).toInstant());

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(testUser));

        streakService.updateStreak(1L);

        assertEquals(1, testUser.getStreak());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateStreak_multipleLessonsSameDay() {
        testUser.setStreak(2);
        testUser.setLastActivityAt(Instant.now());

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(testUser));

        // First call
        streakService.updateStreak(1L);
        int streakAfterFirst = testUser.getStreak();

        // Second call same day
        streakService.updateStreak(1L);

        // Streak should not increase again
        assertEquals(streakAfterFirst, testUser.getStreak());
    }
}