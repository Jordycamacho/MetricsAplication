package com.fitapp.backend.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitapp.backend.application.dto.RoutineSetParameter.response.RoutineSetParameterResponse;
import com.fitapp.backend.application.dto.RoutineSetTemplate.response.RoutineSetTemplateResponse;
import com.fitapp.backend.application.dto.routine.request.AddExerciseToRoutineRequest;
import com.fitapp.backend.application.dto.routine.response.RoutineExerciseParameterResponse;
import com.fitapp.backend.application.dto.routine.response.RoutineExerciseResponse;
import com.fitapp.backend.application.ports.input.RoutineExerciseUseCase;
import com.fitapp.backend.application.ports.output.ExercisePersistencePort;
import com.fitapp.backend.application.ports.output.RoutineExercisePersistencePort;
import com.fitapp.backend.application.ports.output.RoutinePersistencePort;
import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.domain.model.RoutineExerciseModel;
import com.fitapp.backend.domain.model.RoutineExerciseParameterModel;
import com.fitapp.backend.domain.model.RoutineModel;
import com.fitapp.backend.domain.model.RoutineSetParameterModel;
import com.fitapp.backend.domain.model.RoutineSetTemplateModel;
import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.converter.RoutineConverter;
import com.fitapp.backend.infrastructure.persistence.entity.ExerciseEntity;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineEntity;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineExerciseEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;
import com.fitapp.backend.infrastructure.persistence.repository.RoutineRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutineExerciseServiceImpl implements RoutineExerciseUseCase {

        private final RoutinePersistencePort routinePersistencePort;
        private final RoutineExercisePersistencePort routineExercisePersistencePort;
        private final UserPersistencePort userPersistencePort;
        private final ExercisePersistencePort exercisePersistencePort;
        private final RoutineConverter routineConverter;
        private final RoutineRepository routineRepository;

        @Override
        @Transactional
        public RoutineExerciseResponse addExerciseToRoutine(Long routineId, AddExerciseToRoutineRequest request,
                        String userEmail) {
                log.info("Iniciando agregado de ejercicio a rutina: rutina={}, usuario={}", routineId, userEmail);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> {
                                        log.error("Usuario no encontrado: {}", userEmail);
                                        return new RuntimeException("User not found");
                                });

                RoutineEntity routineEntity = routineRepository.findById(routineId)
                                .orElseThrow(() -> {
                                        log.error("Rutina no encontrada: id={}", routineId);
                                        return new RuntimeException("Routine not found");
                                });

                if (!routineEntity.getUser().getId().equals(user.getId())) {
                        log.error("Usuario no autorizado para modificar rutina: rutina={}, usuario={}",
                                        routineId, user.getId());
                        throw new RuntimeException("Unauthorized");
                }

                ExerciseEntity exerciseEntity = exercisePersistencePort.findEntityById(request.getExerciseId())
                                .orElseThrow(() -> {
                                        log.error("Ejercicio no encontrado: {}", request.getExerciseId());
                                        return new RuntimeException("Exercise not found");
                                });

                RoutineExerciseEntity routineExercise = routineConverter.addExerciseToRoutine(
                                routineEntity, request, exerciseEntity);

                routineEntity = routineRepository.save(routineEntity);

                RoutineExerciseEntity savedExercise = routineEntity.getExercises().stream()
                                .filter(e -> e.getExercise().getId().equals(request.getExerciseId())
                                                && e.getPosition().equals(routineExercise.getPosition()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Exercise not found after save"));

                log.info("Ejercicio agregado exitosamente: rutina={}, ejercicio={}, posición={}",
                                routineId, request.getExerciseId(), savedExercise.getPosition());

                return mapToResponse(routineConverter.convertRoutineExercise(savedExercise), exerciseEntity);
        }

        @Override
        @Transactional
        public RoutineExerciseResponse updateExerciseInRoutine(Long routineId, Long exerciseId,
                        AddExerciseToRoutineRequest request, String userEmail) {
                log.info("Actualizando ejercicio en rutina: rutina={}, ejercicio={}, usuario={}",
                                routineId, exerciseId, userEmail);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                RoutineModel routine = routinePersistencePort.findByIdAndUserId(routineId, user.getId())
                                .orElseThrow(() -> new RuntimeException("Routine not found"));

                RoutineExerciseModel existingExercise = routine.getExercises().stream()
                                .filter(e -> e.getId().equals(exerciseId))
                                .findFirst()
                                .orElseThrow(() -> {
                                        log.error("Ejercicio no encontrado en rutina: ejercicio={}, rutina={}",
                                                        exerciseId, routineId);
                                        return new RuntimeException("Exercise not found in routine");
                                });

                if (request.getSessionNumber() != null) {
                        existingExercise.setSessionNumber(request.getSessionNumber());
                }
                if (request.getSessionOrder() != null) {
                        existingExercise.setSessionOrder(request.getSessionOrder());
                }
                if (request.getDayOfWeek() != null) {
                        try {
                                existingExercise.setDayOfWeek(DayOfWeek.valueOf(request.getDayOfWeek()));
                        } catch (IllegalArgumentException e) {
                                log.warn("Día de la semana inválido: {}", request.getDayOfWeek());
                        }
                }
                if (request.getRestAfterExercise() != null) {
                        existingExercise.setRestAfterExercise(request.getRestAfterExercise());
                }

                // Actualizar en base de datos
                RoutineModel updatedRoutine = routinePersistencePort.update(routine);

                log.info("Ejercicio actualizado exitosamente: rutina={}, ejercicio={}", routineId, exerciseId);

                // Obtener ejercicio actualizado
                RoutineExerciseModel updatedExercise = updatedRoutine.getExercises().stream()
                                .filter(e -> e.getId().equals(exerciseId))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Exercise not found after update"));

                ExerciseEntity exerciseEntity = exercisePersistencePort.findEntityById(updatedExercise.getExerciseId())
                                .orElseThrow(() -> new RuntimeException("Exercise entity not found"));

                return mapToResponse(updatedExercise, exerciseEntity);
        }

        @Override
        @Transactional
        public void removeExerciseFromRoutine(Long routineId, Long exerciseId, String userEmail) {
                log.info("Eliminando ejercicio de rutina: rutina={}, ejercicio={}, usuario={}",
                                routineId, exerciseId, userEmail);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Verificar que la rutina pertenece al usuario
                routinePersistencePort.findByIdAndUserId(routineId, user.getId())
                                .orElseThrow(() -> new RuntimeException("Routine not found"));

                // Eliminar ejercicio
                routineExercisePersistencePort.deleteByIdAndRoutineId(exerciseId, routineId);

                log.info("Ejercicio eliminado exitosamente: rutina={}, ejercicio={}", routineId, exerciseId);
        }

        @Override
        @Transactional(readOnly = true)
        public List<RoutineExerciseResponse> getExercisesBySession(Long routineId, Integer sessionNumber,
                        String userEmail) {
                log.debug("Obteniendo ejercicios por sesión: rutina={}, sesión={}, usuario={}",
                                routineId, sessionNumber, userEmail);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Verificar rutina
                routinePersistencePort.findByIdAndUserId(routineId, user.getId())
                                .orElseThrow(() -> new RuntimeException("Routine not found"));

                List<RoutineExerciseModel> exercises = routineExercisePersistencePort
                                .findByRoutineIdAndSessionNumber(routineId, sessionNumber);

                return exercises.stream()
                                .map(exercise -> {
                                        ExerciseEntity exerciseEntity = exercisePersistencePort
                                                        .findEntityById(exercise.getExerciseId())
                                                        .orElse(null);
                                        return mapToResponse(exercise, exerciseEntity);
                                })
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<RoutineExerciseResponse> getExercisesByDay(Long routineId, String dayOfWeek, String userEmail) {
                log.debug("Obteniendo ejercicios por día: rutina={}, día={}, usuario={}",
                                routineId, dayOfWeek, userEmail);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Verificar rutina
                routinePersistencePort.findByIdAndUserId(routineId, user.getId())
                                .orElseThrow(() -> new RuntimeException("Routine not found"));

                List<RoutineExerciseModel> exercises = routineExercisePersistencePort
                                .findByRoutineIdAndDayOfWeek(routineId, dayOfWeek);

                return exercises.stream()
                                .map(exercise -> {
                                        ExerciseEntity exerciseEntity = exercisePersistencePort
                                                        .findEntityById(exercise.getExerciseId())
                                                        .orElse(null);
                                        return mapToResponse(exercise, exerciseEntity);
                                })
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public void reorderExercises(Long routineId, List<Long> exerciseIds, String userEmail) {
                log.info("Reordenando ejercicios: rutina={}, cantidad={}, usuario={}",
                                routineId, exerciseIds.size(), userEmail);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                RoutineModel routine = routinePersistencePort.findByIdAndUserId(routineId, user.getId())
                                .orElseThrow(() -> new RuntimeException("Routine not found"));

                // Verificar que todos los IDs pertenecen a la rutina
                Set<Long> routineExerciseIds = routine.getExercises().stream()
                                .map(RoutineExerciseModel::getId)
                                .collect(Collectors.toSet());

                if (!routineExerciseIds.containsAll(exerciseIds)) {
                        log.error("Algunos ejercicios no pertenecen a la rutina");
                        throw new IllegalArgumentException("Some exercises do not belong to the routine");
                }

                // Reordenar ejercicios
                Map<Long, RoutineExerciseModel> exerciseMap = routine.getExercises().stream()
                                .collect(Collectors.toMap(RoutineExerciseModel::getId, Function.identity()));

                List<RoutineExerciseModel> reorderedExercises = new ArrayList<>();
                for (int i = 0; i < exerciseIds.size(); i++) {
                        RoutineExerciseModel exercise = exerciseMap.get(exerciseIds.get(i));
                        exercise.setPosition(i + 1);
                        reorderedExercises.add(exercise);
                }

                // Agregar los ejercicios que no estaban en la lista de reordenamiento
                for (RoutineExerciseModel exercise : routine.getExercises()) {
                        if (!exerciseIds.contains(exercise.getId())) {
                                reorderedExercises.add(exercise);
                        }
                }

                // Actualizar rutina
                routine.setExercises(reorderedExercises);
                routinePersistencePort.update(routine);

                log.info("Ejercicios reordenados exitosamente: rutina={}, ejercicios={}",
                                routineId, exerciseIds.size());
        }

        @Override
        @Transactional(readOnly = true)
        public List<RoutineExerciseResponse> getRoutineExercises(Long routineId, String userEmail) {
                log.info("Obteniendo todos los ejercicios de la rutina: routineId={}, usuario={}", routineId,
                                userEmail);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> {
                                        log.error("Usuario no encontrado: {}", userEmail);
                                        return new RuntimeException("User not found");
                                });

                RoutineModel routine = routinePersistencePort.findByIdAndUserId(routineId, user.getId())
                                .orElseThrow(() -> {
                                        log.error("Rutina no encontrada o no pertenece al usuario: routineId={}, userId={}",
                                                        routineId, user.getId());
                                        return new RuntimeException("Routine not found");
                                });

                List<RoutineExerciseModel> exercises = routineExercisePersistencePort.findByRoutineId(routineId);
                log.debug("Se encontraron {} ejercicios para la rutina {}", exercises.size(), routineId);

                List<RoutineExerciseResponse> response = exercises.stream()
                                .map(exercise -> {
                                        ExerciseEntity exerciseEntity = exercisePersistencePort
                                                        .findEntityById(exercise.getExerciseId())
                                                        .orElse(null);
                                        return mapToResponse(exercise, exerciseEntity);
                                })
                                .collect(Collectors.toList());

                log.info("Devolviendo {} ejercicios para la rutina {}", response.size(), routineId);
                return response;
        }

        private RoutineExerciseResponse mapToResponse(RoutineExerciseModel model, ExerciseEntity exerciseEntity) {
                if (model == null)
                        return null;

                // Calcular sets y otros valores derivados
                int setsCount = model.getSets() != null ? model.getSets().size() : 0;

                return RoutineExerciseResponse.builder()
                                .id(model.getId())
                                .exerciseId(model.getExerciseId())
                                .exerciseName(exerciseEntity != null ? exerciseEntity.getName() : null)
                                .position(model.getPosition())
                                .sessionNumber(model.getSessionNumber())
                                .dayOfWeek(model.getDayOfWeek())
                                .sessionOrder(model.getSessionOrder())
                                .restAfterExercise(model.getRestAfterExercise())
                                .sets(setsCount)
                                .targetParameters(mapToParameterResponses(model.getTargetParameters()))
                                .setsTemplate(mapToSetTemplateResponses(model.getSets()))
                                .build();
        }

        private List<RoutineExerciseParameterResponse> mapToParameterResponses(
                        List<RoutineExerciseParameterModel> parameters) {
                if (parameters == null)
                        return new ArrayList<>();

                return parameters.stream()
                                .map(param -> RoutineExerciseParameterResponse.builder()
                                                .id(param.getId())
                                                .parameterId(param.getParameterId())
                                                .numericValue(param.getNumericValue())
                                                .integerValue(param.getIntegerValue())
                                                .durationValue(param.getDurationValue())
                                                .stringValue(param.getStringValue())
                                                .minValue(param.getMinValue())
                                                .maxValue(param.getMaxValue())
                                                .defaultValue(param.getDefaultValue())
                                                .build())
                                .collect(Collectors.toList());
        }

        private List<RoutineSetTemplateResponse> mapToSetTemplateResponses(
                        List<RoutineSetTemplateModel> sets) {
                if (sets == null)
                        return new ArrayList<>();

                return sets.stream()
                                .map(set -> RoutineSetTemplateResponse.builder()
                                                .id(set.getId())
                                                .position(set.getPosition())
                                                .subSetNumber(set.getSubSetNumber())
                                                .groupId(set.getGroupId())
                                                .setType(set.getSetType())
                                                .restAfterSet(set.getRestAfterSet())
                                                .parameters(mapToSetParameterResponses(set.getParameters()))
                                                .build())
                                .collect(Collectors.toList());
        }

        private List<RoutineSetParameterResponse> mapToSetParameterResponses(
                        List<RoutineSetParameterModel> parameters) {
                if (parameters == null)
                        return new ArrayList<>();

                return parameters.stream()
                                .map(param -> RoutineSetParameterResponse.builder()
                                                .id(param.getId())
                                                .parameterId(param.getParameterId())
                                                .numericValue(param.getNumericValue())
                                                .durationValue(param.getDurationValue())
                                                .integerValue(param.getIntegerValue())
                                                .build())
                                .collect(Collectors.toList());
        }
}