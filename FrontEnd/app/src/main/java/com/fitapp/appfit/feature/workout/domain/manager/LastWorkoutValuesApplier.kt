package com.fitapp.appfit.feature.workout.domain.manager

import android.util.Log
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineResponse
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineExerciseResponse
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse
import com.fitapp.appfit.feature.routine.model.setparameter.response.RoutineSetParameterResponse
import com.fitapp.appfit.feature.workout.model.response.LastExerciseValuesResponse

class LastWorkoutValuesApplier {

    companion object {
        private const val TAG = "LastWorkoutValuesApplier"
    }

    fun applyValuesToRoutine(
        routine: RoutineResponse,
        lastValues: Map<Long, LastExerciseValuesResponse>
    ): RoutineResponse {
        if (lastValues.isEmpty()) {
            Log.d(TAG, "NO_LAST_VALUES_TO_APPLY | routineId=${routine.id}")
            return routine
        }

        Log.i(TAG, "APPLYING_LAST_VALUES | routineId=${routine.id} | exercisesWithHistory=${lastValues.size}")

        val updatedExercises = routine.exercises?.map { exercise ->
            applyValuesToExercise(exercise, lastValues[exercise.exerciseId])
        }

        return routine.copy(exercises = updatedExercises)
    }

    private fun applyValuesToExercise(
        exercise: RoutineExerciseResponse,
        lastExerciseValues: LastExerciseValuesResponse?
    ): RoutineExerciseResponse {
        if (lastExerciseValues == null) return exercise

        val lastSetByPosition = lastExerciseValues.sets.associateBy { it.position }

        val updatedSets = exercise.setsTemplate?.map { set ->
            val lastSet = lastSetByPosition[set.position]
            if (lastSet != null) applyValuesToSet(set, lastSet) else set
        }

        return exercise.copy(setsTemplate = updatedSets)
    }

    private fun applyValuesToSet(
        set: RoutineSetTemplateResponse,
        lastSet: LastExerciseValuesResponse.LastSetValue
    ): RoutineSetTemplateResponse {
        val lastParamById = lastSet.parameters.associateBy { it.parameterId }

        val updatedParams = set.parameters?.map { param ->
            val lastParam = lastParamById[param.parameterId]
            if (lastParam != null) applyValuesToParameter(param, lastParam) else param
        }

        return set.copy(parameters = updatedParams)
    }

    private fun applyValuesToParameter(
        param: RoutineSetParameterResponse,
        lastParam: LastExerciseValuesResponse.ParameterValue
    ): RoutineSetParameterResponse {
        Log.d(
            TAG,
            "APPLYING_PARAM_VALUE | parameterId=${param.parameterId} | " +
                    "intValue=${lastParam.integerValue} | numValue=${lastParam.numericValue}"
        )

        return RoutineSetParameterResponse(
            id = param.id,
            setTemplateId = param.setTemplateId,
            parameterId = param.parameterId,
            parameterName = param.parameterName,
            parameterType = param.parameterType,
            unit = param.unit,
            repetitions = lastParam.integerValue ?: param.repetitions,
            numericValue = lastParam.numericValue ?: param.numericValue,
            integerValue = lastParam.integerValue ?: param.integerValue,
            durationValue = lastParam.durationValue ?: param.durationValue
        )
    }

    private fun RoutineResponse.copy(exercises: List<RoutineExerciseResponse>?) = RoutineResponse(
        id = this.id,
        name = this.name,
        description = this.description,
        sportId = this.sportId,
        sportName = this.sportName,
        trainingDays = this.trainingDays,
        goal = this.goal,
        sessionsPerWeek = this.sessionsPerWeek,
        isActive = this.isActive,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        lastUsedAt = this.lastUsedAt,
        exercises = exercises
    )

    private fun RoutineExerciseResponse.copy(setsTemplate: List<RoutineSetTemplateResponse>?) = RoutineExerciseResponse(
        id = this.id,
        exerciseId = this.exerciseId,
        routineId = this.routineId,
        exerciseName = this.exerciseName,
        position = this.position,
        sessionNumber = this.sessionNumber,
        dayOfWeek = this.dayOfWeek,
        sessionOrder = this.sessionOrder,
        restAfterExercise = this.restAfterExercise,
        sets = this.sets,
        targetParameters = this.targetParameters,
        setsTemplate = setsTemplate,
        circuitGroupId = this.circuitGroupId,
        circuitRoundCount = this.circuitRoundCount,
        superSetGroupId = this.superSetGroupId,
        amrapDurationSeconds = this.amrapDurationSeconds,
        emomIntervalSeconds = this.emomIntervalSeconds,
        emomTotalRounds = this.emomTotalRounds,
        tabataWorkSeconds = this.tabataWorkSeconds,
        tabataRestSeconds = this.tabataRestSeconds,
        tabataRounds = this.tabataRounds,
        notes = this.notes
    )

    private fun RoutineSetTemplateResponse.copy(parameters: List<RoutineSetParameterResponse>?) = RoutineSetTemplateResponse(
        id = this.id,
        position = this.position,
        subSetNumber = this.subSetNumber,
        groupId = this.groupId,
        setType = this.setType,
        restAfterSet = this.restAfterSet,
        parameters = parameters
    )
}