package com.fitapp.backend.domain.exception;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(SportNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSportNotFound(SportNotFoundException ex) {
        log.warn("SPORT_NOT_FOUND | {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.builder()
                        .code("SPORT_NOT_FOUND")
                        .message(ex.getMessage())
                        .timestamp(Instant.now())
                        .correlationId(MDC.get("correlationId"))
                        .build());
    }

    @ExceptionHandler(SportOwnershipException.class)
    public ResponseEntity<ErrorResponse> handleSportOwnership(SportOwnershipException ex) {
        log.warn("SPORT_OWNERSHIP_VIOLATION | {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorResponse.builder()
                        .code("SPORT_FORBIDDEN")
                        .message(ex.getMessage())
                        .timestamp(Instant.now())
                        .correlationId(MDC.get("correlationId"))
                        .build());
    }

    @ExceptionHandler(PredefinedSportException.class)
    public ResponseEntity<ErrorResponse> handlePredefinedSport(PredefinedSportException ex) {
        log.warn("PREDEFINED_SPORT_VIOLATION | {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorResponse.builder()
                        .code("SPORT_PREDEFINED")
                        .message(ex.getMessage())
                        .timestamp(Instant.now())
                        .correlationId(MDC.get("correlationId"))
                        .build());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        log.warn("USER_NOT_FOUND | {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.builder()
                        .code("USER_NOT_FOUND")
                        .message(ex.getMessage())
                        .timestamp(Instant.now())
                        .correlationId(MDC.get("correlationId"))
                        .build());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> ErrorResponse.FieldError.builder()
                        .field(e.getField())
                        .message(e.getDefaultMessage())
                        .build())
                .toList();

        log.warn("VALIDATION_ERROR | fields={}", fieldErrors.stream()
                .map(ErrorResponse.FieldError::getField).toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder()
                        .code("VALIDATION_ERROR")
                        .message("One or more fields are invalid")
                        .timestamp(Instant.now())
                        .correlationId(MDC.get("correlationId"))
                        .fieldErrors(fieldErrors)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("UNHANDLED_EXCEPTION | {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.builder()
                        .code("INTERNAL_ERROR")
                        .message("An unexpected error occurred")
                        .timestamp(Instant.now())
                        .correlationId(MDC.get("correlationId"))
                        .build());
    }

    @ExceptionHandler(ExerciseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleExerciseNotFound(ExerciseNotFoundException ex) {
        log.warn("EXERCISE_NOT_FOUND | {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.builder()
                        .code("EXERCISE_NOT_FOUND")
                        .message(ex.getMessage())
                        .timestamp(Instant.now())
                        .correlationId(MDC.get("correlationId"))
                        .build());
    }

    @ExceptionHandler(ExerciseOwnershipException.class)
    public ResponseEntity<ErrorResponse> handleExerciseOwnership(ExerciseOwnershipException ex) {
        log.warn("EXERCISE_OWNERSHIP | {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorResponse.builder()
                        .code("EXERCISE_FORBIDDEN")
                        .message(ex.getMessage())
                        .timestamp(Instant.now())
                        .correlationId(MDC.get("correlationId"))
                        .build());
    }

    @ExceptionHandler(ExerciseAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleExerciseDuplicate(ExerciseAlreadyExistsException ex) {
        log.warn("EXERCISE_DUPLICATE | {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.builder()
                        .code("EXERCISE_DUPLICATE")
                        .message(ex.getMessage())
                        .timestamp(Instant.now())
                        .correlationId(MDC.get("correlationId"))
                        .build());
    }

    @ExceptionHandler(ExerciseRatingException.class)
    public ResponseEntity<ErrorResponse> handleExerciseRating(ExerciseRatingException ex) {
        log.warn("EXERCISE_RATING_ERROR | {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder()
                        .code("EXERCISE_RATING_ERROR")
                        .message(ex.getMessage())
                        .timestamp(Instant.now())
                        .correlationId(MDC.get("correlationId"))
                        .build());
    }
}