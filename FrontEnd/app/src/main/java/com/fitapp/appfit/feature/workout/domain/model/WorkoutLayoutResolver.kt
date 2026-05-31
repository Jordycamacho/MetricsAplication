package com.fitapp.appfit.feature.workout.domain.model

import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineResponse

private val CARDIO_SPORTS = setOf(
    "running", "correr", "carrera",
    "ciclismo", "cycling", "bici",
    "natación", "natacion", "swimming",
    "triatlón", "triatlon", "triathlon"
)

/**
 * Resuelve qué perfil de UI usar para una rutina.
 * v1: siempre devuelve SETS; la heurística queda lista para v2.
 */
object WorkoutLayoutResolver {

    fun resolve(routine: RoutineResponse): ExecutionProfile {
        val exercises = routine.exercises.orEmpty()
        if (exercises.isEmpty()) return ExecutionProfile.SETS

        val sport = routine.sportName?.lowercase()?.trim().orEmpty()

        return when {
            exercises.any {
                (it.tabataWorkSeconds ?: 0) > 0 || (it.emomIntervalSeconds ?: 0) > 0 ||
                    (it.amrapDurationSeconds ?: 0) > 0
            } -> ExecutionProfile.INTERVALS

            exercises.any { !it.circuitGroupId.isNullOrBlank() } -> ExecutionProfile.CIRCUIT

            sport in CARDIO_SPORTS || hasDominantDistance(exercises) -> ExecutionProfile.SEGMENTS

            else -> ExecutionProfile.SETS
        }
    }

    private fun hasDominantDistance(
        exercises: List<com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineExerciseResponse>
    ): Boolean {
        var distanceSets = 0
        var totalSets = 0
        exercises.forEach { ex ->
            ex.setsTemplate?.forEach { set ->
                totalSets++
                val hasDistance = set.parameters?.any {
                    it.parameterType?.uppercase() == "DISTANCE"
                } == true
                if (hasDistance) distanceSets++
            }
        }
        return totalSets > 0 && distanceSets.toDouble() / totalSets >= 0.5
    }
}
