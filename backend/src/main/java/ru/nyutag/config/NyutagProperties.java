package ru.nyutag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "nyutag")
public class NyutagProperties {

    private final Xp xp = new Xp();
    private final Lessons lessons = new Lessons();
    private final DevAuth devAuth = new DevAuth();
    private final Telegram telegram = new Telegram();
    private final Cors cors = new Cors();
    private String courseCode = "BURYAT_A1";
    private long authMaxAgeSeconds = 86400;

    public Xp getXp() { return xp; }
    public Lessons getLessons() { return lessons; }
    public DevAuth getDevAuth() { return devAuth; }
    public Telegram getTelegram() { return telegram; }
    public Cors getCors() { return cors; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public long getAuthMaxAgeSeconds() { return authMaxAgeSeconds; }
    public void setAuthMaxAgeSeconds(long authMaxAgeSeconds) { this.authMaxAgeSeconds = authMaxAgeSeconds; }

    public static class Xp {
        private int correctAnswer = 10;
        public int getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(int correctAnswer) { this.correctAnswer = correctAnswer; }
    }

    public static class Lessons {
        private int completionReward = 20;
        public int getCompletionReward() { return completionReward; }
        public void setCompletionReward(int completionReward) { this.completionReward = completionReward; }
    }

    public static class DevAuth {
        private boolean enabled = false;
        private long telegramId = 1L;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public long getTelegramId() { return telegramId; }
        public void setTelegramId(long telegramId) { this.telegramId = telegramId; }
    }

    public static class Telegram {
        private String botToken = "";
        public String getBotToken() { return botToken; }
        public void setBotToken(String botToken) { this.botToken = botToken; }
    }

    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:5173"));
        public List<String> getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(List<String> allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    }
}