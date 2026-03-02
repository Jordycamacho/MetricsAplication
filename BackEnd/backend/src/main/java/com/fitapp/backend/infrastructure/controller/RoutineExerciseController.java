package com.fitapp.backend.infrastructure.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitapp.backend.application.dto.routine.request.AddExerciseToRoutineRequest;
import com.fitapp.backend.application.dto.routine.response.RoutineExerciseResponse;
import com.fitapp.backend.application.ports.input.RoutineExerciseUseCase;
import com.fitapp.backend.application.ports.input.RoutineUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/routines/{routineId}/exercises")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Routine Exercises", description = "Gestión de ejercicios dentro de una rutina")
public class RoutineExerciseController {

        private final RoutineExerciseUseCase routineExerciseUseCase;
        private final RoutineUseCase routineUseCase;

        @Operation(summary = "Listar ejercicios de una rutina", description = "Devuelve todos los ejercicios de la rutina ordenados por posición")
        @ApiResponse(responseCode = "200", description = "Lista de ejercicios")
        @ApiResponse(responseCode = "404", description = "Rutina no encontrada")
        @GetMapping
        public ResponseEntity<List<RoutineExerciseResponse>> getRoutineExercises(
                        @PathVariable Long routineId,
                        @AuthenticationPrincipal Jwt jwt) {
                String userEmail = jwt.getClaimAsString("email");
                log.info("GET_EXERCISES_REQUEST | routineId={} | user={}", routineId, userEmail);

                List<RoutineExerciseResponse> exercises = routineExerciseUseCase.getRoutineExercises(routineId,
                                userEmail);

                log.info("GET_EXERCISES_RESPONSE | routineId={} | count={}", routineId, exercises.size());
                return ResponseEntity.ok(exercises);
        }

        @Operation(summary = "Añadir ejercicio a rutina", description = "Añade un ejercicio con configuración de sesión, orden y parámetros objetivos")
        @ApiResponse(responseCode = "201", description = "Ejercicio añadido")
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
        @ApiResponse(responseCode = "404", description = "Rutina o ejercicio no encontrado")
        @PostMapping
        public ResponseEntity<RoutineExerciseResponse> addExerciseToRoutine(
                        @PathVariable Long routineId,
                        @Valid @RequestBody AddExerciseToRoutineRequest request,
                        @AuthenticationPrincipal Jwt jwt) {
                String userEmail = jwt.getClaimAsString("email");
                log.info("ADD_EXERCISE_REQUEST | routineId={} | exerciseId={} | session={} | user={}",
                                routineId, request.getExerciseId(), request.getSessionNumber(), userEmail);

                RoutineExerciseResponse response = routineExerciseUseCase.addExerciseToRoutine(routineId, request,
                                userEmail);

                // Actualizar timestamp de uso sin bloquear la respuesta
                try {
                        routineUseCase.markRoutineAsUsed(routineId, userEmail);
                } catch (Exception e) {
                        log.warn("MARK_USED_FAILED | routineId={} | error={}", routineId, e.getMessage());
                }

                log.info("ADD_EXERCISE_RESPONSE | routineId={} | exerciseId={} | position={}",
                                routineId, response.getExerciseId(), response.getPosition());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @Operation(summary = "Actualizar ejercicio en rutina", description = "Actualiza sesión, orden, descanso o día de un ejercicio ya añadido")
        @ApiResponse(responseCode = "200", description = "Ejercicio actualizado")
        @ApiResponse(responseCode = "404", description = "Rutina o ejercicio no encontrado")
        @PutMapping("/{exerciseId}")
        public ResponseEntity<RoutineExerciseResponse> updateExerciseInRoutine(
                        @PathVariable Long routineId,
                        @PathVariable Long exerciseId,
                        @Valid @RequestBody AddExerciseToRoutineRequest request,
                        @AuthenticationPrincipal Jwt jwt) {
                String userEmail = jwt.getClaimAsString("email");
                log.info("UPDATE_EXERCISE_REQUEST | routineId={} | exerciseId={} | user={}", routineId, exerciseId,
                                userEmail);

                RoutineExerciseResponse response = routineExerciseUseCase.updateExerciseInRoutine(
                                routineId, exerciseId, request, userEmail);

                try {
                        routineUseCase.markRoutineAsUsed(routineId, userEmail);
                } catch (Exception e) {
                        log.warn("MARK_USED_FAILED | routineId={} | error={}", routineId, e.getMessage());
                }

                log.info("UPDATE_EXERCISE_RESPONSE | routineId={} | exerciseId={}", routineId, exerciseId);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Eliminar ejercicio de rutina")
        @ApiResponse(responseCode = "204", description = "Ejercicio eliminado")
        @ApiResponse(responseCode = "404", description = "Rutina o ejercicio no encontrado")
        @DeleteMapping("/{exerciseId}")
        public ResponseEntity<Void> removeExerciseFromRoutine(
                        @PathVariable Long routineId,
                        @PathVariable Long exerciseId,
                        @AuthenticationPrincipal Jwt jwt) {
                String userEmail = jwt.getClaimAsString("email");
                log.info("REMOVE_EXERCISE_REQUEST | routineId={} | exerciseId={} | user={}", routineId, exerciseId,
                                userEmail);

                routineExerciseUseCase.removeExerciseFromRoutine(routineId, exerciseId, userEmail);

                try {
                        routineUseCase.markRoutineAsUsed(routineId, userEmail);
                } catch (Exception e) {
                        log.warn("MARK_USED_FAILED | routineId={} | error={}", routineId, e.getMessage());
                }

                log.info("REMOVE_EXERCISE_RESPONSE | routineId={} | exerciseId={}", routineId, exerciseId);
                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Ejercicios por número de sesión")
        @GetMapping("/session/{sessionNumber}")
        public ResponseEntity<List<RoutineExerciseResponse>> getExercisesBySession(
                        @PathVariable Long routineId,
                        @PathVariable Integer sessionNumber,
                        @AuthenticationPrincipal Jwt jwt) {
                String userEmail = jwt.getClaimAsString("email");
                log.debug("GET_BY_SESSION_REQUEST | routineId={} | session={} | user={}", routineId, sessionNumber,
                                userEmail);

                List<RoutineExerciseResponse> response = routineExerciseUseCase.getExercisesBySession(routineId,
                                sessionNumber, userEmail);

                log.debug("GET_BY_SESSION_RESPONSE | routineId={} | session={} | count={}", routineId, sessionNumber,
                                response.size());
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Ejercicios por día de la semana")
        @GetMapping("/day/{dayOfWeek}")
        public ResponseEntity<List<RoutineExerciseResponse>> getExercisesByDay(
                        @PathVariable Long routineId,
                        @PathVariable String dayOfWeek,
                        @AuthenticationPrincipal Jwt jwt) {
                String userEmail = jwt.getClaimAsString("email");
                log.debug("GET_BY_DAY_REQUEST | routineId={} | day={} | user={}", routineId, dayOfWeek, userEmail);

                List<RoutineExerciseResponse> response = routineExerciseUseCase.getExercisesByDay(routineId, dayOfWeek,
                                userEmail);

                log.debug("GET_BY_DAY_RESPONSE | routineId={} | day={} | count={}", routineId, dayOfWeek,
                                response.size());
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Reordenar ejercicios", description = "Actualiza el orden (position) de los ejercicios de la rutina")
        @ApiResponse(responseCode = "204", description = "Ejercicios reordenados")
        @PatchMapping("/reorder")
        public ResponseEntity<Void> reorderExercises(
                        @PathVariable Long routineId,
                        @RequestBody List<Long> exerciseIds,
                        @AuthenticationPrincipal Jwt jwt) {
                String userEmail = jwt.getClaimAsString("email");
                log.info("REORDER_REQUEST | routineId={} | count={} | user={}", routineId, exerciseIds.size(),
                                userEmail);

                routineExerciseUseCase.reorderExercises(routineId, exerciseIds, userEmail);

                try {
                        routineUseCase.markRoutineAsUsed(routineId, userEmail);
                } catch (Exception e) {
                        log.warn("MARK_USED_FAILED | routineId={} | error={}", routineId, e.getMessage());
                }

                log.info("REORDER_RESPONSE_OK | routineId={}", routineId);
                return ResponseEntity.noContent().build();
        }
}