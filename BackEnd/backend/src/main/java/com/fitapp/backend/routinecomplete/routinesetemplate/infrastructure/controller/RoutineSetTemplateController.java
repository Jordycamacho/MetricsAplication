package com.fitapp.backend.routinecomplete.routinesetemplate.infrastructure.controller;

import com.fitapp.backend.application.dto.RoutineSetTemplate.request.BulkUpdateSetParametersRequest;
import com.fitapp.backend.application.dto.RoutineSetTemplate.request.CreateSetTemplateRequest;
import com.fitapp.backend.application.dto.RoutineSetTemplate.request.UpdateSetTemplateRequest;
import com.fitapp.backend.application.dto.RoutineSetTemplate.response.RoutineSetTemplateResponse;
import com.fitapp.backend.infrastructure.persistence.mapper.SetTemplateMapper;
import com.fitapp.backend.routinecomplete.routinesetemplate.aplication.service.RoutineSetTemplateServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/routine-set-templates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Routine Set Templates", description = "Endpoints for managing exercise set templates in routines")
public class RoutineSetTemplateController {

        private final RoutineSetTemplateServiceImpl setTemplateService;
        private final SetTemplateMapper setTemplateMapper;

        @Operation(summary = "Create set template", description = "Creates a new set template for a routine exercise")
        @PostMapping
        public ResponseEntity<RoutineSetTemplateResponse> createSetTemplate(
                        @Valid @RequestBody CreateSetTemplateRequest request,
                        @AuthenticationPrincipal Jwt jwt) {

                log.info("CREATE_SET_TEMPLATE_REQUEST | user={} | routineExercise={} | position={}",
                                getEmailFromJwt(jwt), request.getRoutineExerciseId(), request.getPosition());

                var setTemplate = setTemplateService.createSetTemplate(request, getEmailFromJwt(jwt));
                var response = setTemplateMapper.toResponse(setTemplate);

                log.info("CREATE_SET_TEMPLATE_SUCCESS | id={} | user={}", response.getId(), getEmailFromJwt(jwt));
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @Operation(summary = "Update set template", description = "Updates an existing set template")
        @PutMapping("/{id}")
        public ResponseEntity<RoutineSetTemplateResponse> updateSetTemplate(
                        @PathVariable Long id,
                        @Valid @RequestBody UpdateSetTemplateRequest request,
                        @AuthenticationPrincipal Jwt jwt) {

                log.info("UPDATE_SET_TEMPLATE_REQUEST | id={} | user={}", id, getEmailFromJwt(jwt));

                var setTemplate = setTemplateService.updateSetTemplate(id, request, getEmailFromJwt(jwt));
                var response = setTemplateMapper.toResponse(setTemplate);

                log.info("UPDATE_SET_TEMPLATE_SUCCESS | id={} | user={}", id, getEmailFromJwt(jwt));
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Bulk save workout sets", description = "Botón flotante: actualiza todos los parámetros de los sets de la sesión en un solo request.")
        @PatchMapping("/bulk-save")
        public ResponseEntity<Void> bulkUpdateSetParameters(
                        @Valid @RequestBody BulkUpdateSetParametersRequest request,
                        @AuthenticationPrincipal Jwt jwt) {

                log.info("BULK_SAVE_REQUEST | user={} | sets={}", getEmailFromJwt(jwt), request.getSetResults().size());

                setTemplateService.bulkUpdateSetParameters(request, getEmailFromJwt(jwt));

                log.info("BULK_SAVE_SUCCESS | user={}", getEmailFromJwt(jwt));
                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Get set template by ID", description = "Retrieves a specific set template by its ID")
        @GetMapping("/{id}")
        public ResponseEntity<RoutineSetTemplateResponse> getSetTemplate(
                        @PathVariable Long id,
                        @AuthenticationPrincipal Jwt jwt) {

                log.debug("GET_SET_TEMPLATE_REQUEST | id={} | user={}", id, getEmailFromJwt(jwt));

                var setTemplate = setTemplateService.getSetTemplateById(id, getEmailFromJwt(jwt));
                var response = setTemplateMapper.toResponse(setTemplate);

                log.debug("GET_SET_TEMPLATE_SUCCESS | id={} | user={}", id, getEmailFromJwt(jwt));
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Get set templates by routine exercise", description = "Retrieves all set templates for a specific routine exercise")
        @GetMapping("/by-routine-exercise/{routineExerciseId}")
        public ResponseEntity<List<RoutineSetTemplateResponse>> getSetTemplatesByRoutineExercise(
                        @PathVariable Long routineExerciseId,
                        @AuthenticationPrincipal Jwt jwt) {

                log.debug("GET_SET_TEMPLATES_BY_ROUTINE_EXERCISE_REQUEST | routineExercise={} | user={}",
                                routineExerciseId, getEmailFromJwt(jwt));

                var setTemplates = setTemplateService.getSetTemplatesByRoutineExercise(routineExerciseId,
                                getEmailFromJwt(jwt));
                var responses = setTemplates.stream()
                                .map(setTemplateMapper::toResponse)
                                .collect(Collectors.toList());

                log.debug("GET_SET_TEMPLATES_BY_ROUTINE_EXERCISE_SUCCESS | count={} | routineExercise={}",
                                responses.size(), routineExerciseId);
                return ResponseEntity.ok(responses);
        }

        @Operation(summary = "Get set templates by group", description = "Retrieves all set templates for a specific routine exercise and group")
        @GetMapping("/by-routine-exercise/{routineExerciseId}/group/{groupId}")
        public ResponseEntity<List<RoutineSetTemplateResponse>> getSetTemplatesByGroup(
                        @PathVariable Long routineExerciseId,
                        @PathVariable String groupId,
                        @AuthenticationPrincipal Jwt jwt) {

                log.debug("GET_SET_TEMPLATES_BY_GROUP_REQUEST | routineExercise={} | group={} | user={}",
                                routineExerciseId, groupId, getEmailFromJwt(jwt));

                var setTemplates = setTemplateService.getSetTemplatesByGroup(routineExerciseId, groupId,
                                getEmailFromJwt(jwt));
                var responses = setTemplates.stream()
                                .map(setTemplateMapper::toResponse)
                                .collect(Collectors.toList());

                log.debug("GET_SET_TEMPLATES_BY_GROUP_SUCCESS | count={} | routineExercise={} | group={}",
                                responses.size(), routineExerciseId, groupId);
                return ResponseEntity.ok(responses);
        }

        @Operation(summary = "Delete set template", description = "Deletes a specific set template")
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteSetTemplate(
                        @PathVariable Long id,
                        @AuthenticationPrincipal Jwt jwt) {

                log.info("DELETE_SET_TEMPLATE_REQUEST | id={} | user={}", id, getEmailFromJwt(jwt));

                setTemplateService.deleteSetTemplate(id, getEmailFromJwt(jwt));

                log.info("DELETE_SET_TEMPLATE_SUCCESS | id={} | user={}", id, getEmailFromJwt(jwt));
                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Delete set templates by routine exercise", description = "Deletes all set templates for a specific routine exercise")
        @DeleteMapping("/by-routine-exercise/{routineExerciseId}")
        public ResponseEntity<Void> deleteSetTemplatesByRoutineExercise(
                        @PathVariable Long routineExerciseId,
                        @AuthenticationPrincipal Jwt jwt) {

                log.info("DELETE_SET_TEMPLATES_BY_ROUTINE_EXERCISE_REQUEST | routineExercise={} | user={}",
                                routineExerciseId, getEmailFromJwt(jwt));

                setTemplateService.deleteSetTemplatesByRoutineExercise(routineExerciseId, getEmailFromJwt(jwt));

                log.info("DELETE_SET_TEMPLATES_BY_ROUTINE_EXERCISE_SUCCESS | routineExercise={} | user={}",
                                routineExerciseId, getEmailFromJwt(jwt));
                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Reorder set templates", description = "Reorders set templates for a specific routine exercise")
        @PatchMapping("/reorder/{routineExerciseId}")
        public ResponseEntity<RoutineSetTemplateResponse> reorderSetTemplates(
                        @PathVariable Long routineExerciseId,
                        @RequestBody List<Long> setTemplateIds,
                        @AuthenticationPrincipal Jwt jwt) {

                log.info("REORDER_SET_TEMPLATES_REQUEST | routineExercise={} | count={} | user={}",
                                routineExerciseId, setTemplateIds.size(), getEmailFromJwt(jwt));

                var reorderedSet = setTemplateService.reorderSetTemplates(routineExerciseId, setTemplateIds,
                                getEmailFromJwt(jwt));
                var response = setTemplateMapper.toResponse(reorderedSet);

                log.info("REORDER_SET_TEMPLATES_SUCCESS | routineExercise={} | count={} | user={}",
                                routineExerciseId, setTemplateIds.size(), getEmailFromJwt(jwt));
                return ResponseEntity.ok(response);
        }

        private String getEmailFromJwt(Jwt jwt) {
                return jwt != null ? jwt.getClaimAsString("email") : "anonymous";
        }
}