package com.fitapp.appfit.feature.workout.presentation.execution

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineResponse
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineExerciseResponse
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse
import com.fitapp.appfit.feature.workout.domain.model.WorkoutCompletionState
import com.fitapp.appfit.feature.workout.domain.model.WorkoutDay

class WorkoutDayAdapter(
    private val onSetValueChanged: (RoutineExerciseResponse, RoutineSetTemplateResponse, String, Double) -> Unit,
    private val onSetCompletedToggled: (RoutineExerciseResponse, RoutineSetTemplateResponse, Boolean) -> Unit,
    private val completionState: WorkoutCompletionState
) : RecyclerView.Adapter<WorkoutDayAdapter.DayViewHolder>() {

    private var days: List<WorkoutDay> = emptyList()
    private val expandedDays = mutableSetOf<Int>()

    companion object {
        private val DAY_ORDER = mapOf(
            "MONDAY" to 1, "TUESDAY" to 2, "WEDNESDAY" to 3,
            "THURSDAY" to 4, "FRIDAY" to 5, "SATURDAY" to 6, "SUNDAY" to 7
        )
        private val DAY_NAMES_ES = mapOf(
            "MONDAY"    to "Lunes",
            "TUESDAY"   to "Martes",
            "WEDNESDAY" to "Miércoles",
            "THURSDAY"  to "Jueves",
            "FRIDAY"    to "Viernes",
            "SATURDAY"  to "Sábado",
            "SUNDAY"    to "Domingo",
            "SIN_DIA"   to "Sin día"
        )
    }

    fun expandAll() {
        for (i in days.indices) expandedDays.add(i)
        notifyDataSetChanged()
    }

    fun collapseAll() {
        expandedDays.clear()
        notifyDataSetChanged()
    }

    fun submitRoutine(routine: RoutineResponse) {
        val grouped = routine.exercises
            .orEmpty()
            .groupBy { it.dayOfWeek ?: "SIN_DIA" }
            .map { (day, exercises) ->
                WorkoutDay(
                    dayOfWeek = day,
                    exercises = exercises.sortedBy { it.sessionOrder ?: it.position }
                )
            }
            .sortedBy { DAY_ORDER[it.dayOfWeek] ?: 99 }

        days = grouped
        expandedDays.clear()
        notifyDataSetChanged()
    }

    fun findDayIndexForExercise(exerciseId: Long): Int {
        return days.indexOfFirst { day ->
            day.exercises.any { it.exerciseId == exerciseId }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(days[position], position)
    }

    override fun getItemCount() = days.size

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDay: TextView = itemView.findViewById(R.id.tv_day_title)
        private val ivExpand: ImageView = itemView.findViewById(R.id.iv_expand_day)
        private val recyclerExercises: RecyclerView = itemView.findViewById(R.id.recycler_exercises)
        private val container: View = itemView.findViewById(R.id.layout_day_container)
        private val tvExerciseCount: TextView = itemView.findViewById(R.id.tv_exercise_count)
        private val tvDayProgress: TextView = itemView.findViewById(R.id.tv_day_progress)
        private val checkboxDayCompleted: CheckBox = itemView.findViewById(R.id.checkbox_day_completed)

        private lateinit var exerciseAdapter: WorkoutExerciseAdapter

        init {
            recyclerExercises.layoutManager = LinearLayoutManager(itemView.context)
            recyclerExercises.isNestedScrollingEnabled = false

            // ✅ CREAR ADAPTER UNA SOLA VEZ
            exerciseAdapter = WorkoutExerciseAdapter(onSetValueChanged, onSetCompletedToggled, completionState)
            recyclerExercises.adapter = exerciseAdapter

            itemView.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                if (expandedDays.contains(pos)) {
                    expandedDays.remove(pos)
                    container.visibility = View.GONE
                    ivExpand.animate().rotation(0f).setDuration(200).start()
                } else {
                    expandedDays.add(pos)
                    container.visibility = View.VISIBLE
                    ivExpand.animate().rotation(180f).setDuration(200).start()
                }
            }

            checkboxDayCompleted.setOnCheckedChangeListener { _, isChecked ->
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val day = days[pos]
                    day.exercises.forEach { exercise ->
                        exercise.setsTemplate?.forEach { set ->
                            completionState.markSetCompleted(set.id, exercise.exerciseId, isChecked)
                            onSetCompletedToggled(exercise, set, isChecked)
                        }
                    }
                    exerciseAdapter.refreshCheckboxes()
                    updateDayProgress(day)
                }
            }
        }

        fun bind(day: WorkoutDay, position: Int) {
            tvDay.text = DAY_NAMES_ES[day.dayOfWeek] ?: day.dayOfWeek
            tvExerciseCount.text = "${day.exercises.size} ejercicio${if (day.exercises.size != 1) "s" else ""}"
            updateDayProgress(day)

            // ✅ Actualizar datos SIN perder expansión de ejercicios
            exerciseAdapter.updateExercises(day.exercises)

            checkboxDayCompleted.setOnCheckedChangeListener(null)
            checkboxDayCompleted.isChecked = completionState.isDayCompleted(day.dayOfWeek)
            checkboxDayCompleted.setOnCheckedChangeListener { _, isChecked ->
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val currentDay = days[pos]
                    currentDay.exercises.forEach { exercise ->
                        exercise.setsTemplate?.forEach { set ->
                            completionState.markSetCompleted(set.id, exercise.exerciseId, isChecked)
                            onSetCompletedToggled(exercise, set, isChecked)
                        }
                    }
                    exerciseAdapter.refreshCheckboxes()
                    updateDayProgress(currentDay)
                }
            }

            if (expandedDays.contains(position)) {
                container.visibility = View.VISIBLE
                ivExpand.rotation = 180f
            } else {
                container.visibility = View.GONE
                ivExpand.rotation = 0f
            }
        }

        private fun updateDayProgress(day: WorkoutDay) {
            val completedCount = completionState.getCompletedExercisesCount(day.dayOfWeek)
            val totalCount = day.exercises.size
            if (completedCount > 0) {
                tvDayProgress.text = " · $completedCount/$totalCount completados"
                tvDayProgress.visibility = View.VISIBLE
            } else {
                tvDayProgress.visibility = View.GONE
            }
        }
    }
}