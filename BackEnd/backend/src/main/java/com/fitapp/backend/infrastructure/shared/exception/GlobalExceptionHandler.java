package com.fitapp.backend.infrastructure.shared.exception;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fitapp.backend.Exercise.domain.exception.ExerciseAlreadyExistsException;
import com.fitapp.backend.Exercise.domain.exception.ExerciseNotFoundException;
import com.fitapp.backend.Exercise.domain.exception.ExerciseOwnershipException;
import com.fitapp.backend.Exercise.domain.exception.ExerciseRatingException;
import com.fitapp.backend.auth.domain.exception.UserNotFoundException;
import com.fitapp.backend.category.domain.exception.CategoryDuplicateException;
import com.fitapp.backend.category.domain.exception.CategoryNotFoundException;
import com.fitapp.backend.category.domain.exception.CategoryOwnershipException;
import com.fitapp.backend.category.domain.exception.PredefinedCategoryException;
import com.fitapp.backend.parameter.domain.exception.UnsupportedParameterException;
import com.fitapp.backend.routinecomplete.routinesetemplate.domain.exception.SetParameterNotFoundException;
import com.fitapp.backend.routinecomplete.routinesetemplate.domain.exception.SetTemplateNotFoundException;
import com.fitapp.backend.routinecomplete.routinesetemplate.domain.exception.SetTemplateOwnershipException;
import com.fitapp.backend.routinecomplete.routinesetemplate.domain.exception.SetTemplatePositionConflictException;
import com.fitapp.backend.sport.domain.exception.PredefinedSportException;
import com.fitapp.backend.sport.domain.exception.SportNotFoundException;
import com.fitapp.backend.sport.domain.exception.SportOwnershipException;
import com.fitapp.backend.suscription.domain.exception.SubscriptionLimitException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

        // ── Sports ────────────────────────────────────────────────────────────────

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

        // ── Categories ────────────────────────────────────────────────────────────

        @ExceptionHandler(CategoryNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleCategoryNotFound(CategoryNotFoundException ex) {
                log.warn("CATEGORY_NOT_FOUND | {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                ErrorResponse.builder()
                                                .code("CATEGORY_NOT_FOUND")
                                                .message(ex.getMessage())
                                                .timestamp(Instant.now())
                                                .correlationId(MDC.get("correlationId"))
                                                .build());
        }

        @ExceptionHandler(CategoryOwnershipException.class)
        public ResponseEntity<ErrorResponse> handleCategoryOwnership(CategoryOwnershipException ex) {
                log.warn("CATEGORY_OWNERSHIP_VIOLATION | {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                                ErrorResponse.builder()
                                                .code("CATEGORY_FORBIDDEN")
                                                .message(ex.getMessage())
                                                .timestamp(Instant.now())
                                                .correlationId(MDC.get("correlationId"))
                                                .build());
        }

        @ExceptionHandler(CategoryDuplicateException.class)
        public ResponseEntity<ErrorResponse> handleCategoryDuplicate(CategoryDuplicateException ex) {
                log.warn("CATEGORY_DUPLICATE | {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                                ErrorResponse.builder()
                                                .code("CATEGORY_DUPLICATE")
                                                .message(ex.getMessage())
                                                .timestamp(Instant.now())
                                                .correlationId(MDC.get("correlationId"))
                                                .build());
        }

        @ExceptionHandler(PredefinedCategoryException.class)
        public ResponseEntity<ErrorResponse> handlePredefinedCategory(PredefinedCategoryException ex) {
                log.warn("PREDEFINED_CATEGORY_VIOLATION | {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                                ErrorResponse.builder()
                                                .code("CATEGORY_PREDEFINED")
                                                .message(ex.getMessage())
                                                .timestamp(Instant.now())
                                                .correlationId(MDC.get("correlationId"))
                                                .build());
        }

        // ── Exercises ─────────────────────────────────────────────────────────────

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

        // ── Validation & Fallback ─────────────────────────────────────────────────

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

        // ── Set Templates ─────────────────────────────────────────────────────────

        @ExceptionHandler(SetTemplateNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleSetTemplateNotFound(SetTemplateNotFoundException ex) {
                log.warn("SET_TEMPLATE_NOT_FOUND | {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                ErrorResponse.builder()
                                                .code("SET_TEMPLATE_NOT_FOUND")
                                                .message(ex.getMessage())
                                                .timestamp(Instant.now())
                                                .correlationId(MDC.get("correlationId"))
                                                .build());
        }

        @ExceptionHandler(SetTemplateOwnershipException.class)
        public ResponseEntity<ErrorResponse> handleSetTemplateOwnership(SetTemplateOwnershipException ex) {
                log.warn("SET_TEMPLATE_OWNERSHIP_VIOLATION | {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                                ErrorResponse.builder()
                                                .code("SET_TEMPLATE_FORBIDDEN")
                                                .message(ex.getMessage())
                                                .timestamp(Instant.now())
                                                .correlationId(MDC.get("correlationId"))
                                                .build());
        }

        @ExceptionHandler(SetParameterNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleSetParameterNotFound(SetParameterNotFoundException ex) {
                log.warn("SET_PARAMETER_NOT_FOUND | {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                ErrorResponse.builder()
                                                .code("SET_PARAMETER_NOT_FOUND")
                                                .message(ex.getMessage())
                                                .timestamp(Instant.now())
                                                .correlationId(MDC.get("correlationId"))
                                                .build());
        }

        @ExceptionHandler(SetTemplatePositionConflictException.class)
        public ResponseEntity<ErrorResponse> handleSetTemplatePositionConflict(
                        SetTemplatePositionConflictException ex) {
                log.warn("SET_TEMPLATE_POSITION_CONFLICT | {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                                ErrorResponse.builder()
                                                .code("SET_TEMPLATE_POSITION_CONFLICT")
                                                .message(ex.getMessage())
                                                .timestamp(Instant.now())
                                                .correlationId(MDC.get("correlationId"))
                                                .build());
        }

        @ExceptionHandler(UnsupportedParameterException.class)
        public ResponseEntity<ErrorResponse> handleUnsupportedParameter(UnsupportedParameterException ex) {
                log.warn("UNSUPPORTED_PARAMETER | {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                ErrorResponse.builder()
                                                .code("UNSUPPORTED_PARAMETER")
                                                .message(ex.getMessage())
                                                .timestamp(Instant.now())
                                                .correlationId(MDC.get("correlationId"))
                                                .build());
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
                log.error("ILLEGAL_ARGUMENT_FULL | error={} | message={} | cause={}",
                                e.getClass().getName(), e.getMessage(), e.getCause(), e);

                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                log.error("FULL_STACK:\n{}", sw.toString());

                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

        @ExceptionHandler(SubscriptionLimitException.class)
        public ResponseEntity<ErrorResponse> handleSubscriptionLimit(SubscriptionLimitException ex) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ErrorResponse.builder()
                                                .code("BAD_REQUEST")
                                                .message(ex.getMessage())
                                                .timestamp(Instant.now())
                                                .correlationId(MDC.get("correlationId"))
                                                .build());
        }
}