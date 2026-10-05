package ru.nyutag.quiz;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "answers")
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    @Column(name = "lesson_item_id", nullable = false)
    private Long lessonItemId;

    @Column(name = "selected_option_index")
    private Integer selectedOptionIndex;

    @Column(name = "selected_text")
    private String selectedText;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "response_ms")
    private Integer responseMs;

    @Column(name = "answered_at", nullable = false)
    private Instant answeredAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

    public Long getLessonItemId() { return lessonItemId; }
    public void setLessonItemId(Long lessonItemId) { this.lessonItemId = lessonItemId; }

    public Integer getSelectedOptionIndex() { return selectedOptionIndex; }
    public void setSelectedOptionIndex(Integer selectedOptionIndex) { this.selectedOptionIndex = selectedOptionIndex; }

    public String getSelectedText() { return selectedText; }
    public void setSelectedText(String selectedText) { this.selectedText = selectedText; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }

    public Integer getResponseMs() { return responseMs; }
    public void setResponseMs(Integer responseMs) { this.responseMs = responseMs; }

    public Instant getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(Instant answeredAt) { this.answeredAt = answeredAt; }
}