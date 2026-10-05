package ru.nyutag.web;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.nyutag.common.LessonUnavailableException;
import ru.nyutag.common.NotFoundException;
import ru.nyutag.common.InvalidAnswerException;
import ru.nyutag.common.UnauthorizedException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(LessonUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleLessonUnavailable(LessonUnavailableException e) {
        return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(InvalidAnswerException.class)
    public ResponseEntity<Map<String, String>> handleInvalidAnswer(InvalidAnswerException e) {
        return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(UnauthorizedException e) {
        return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
    }
}