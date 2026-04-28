package com.fitapp.appfit.feature.workout.presentation.execution

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        private val exerciseAdapter =
            WorkoutExerciseAdapter(onSetValueChanged, onSetCompletedToggled, completionState)

        init {
            recyclerExercises.layoutManager = LinearLayoutManager(itemView.context)
            recyclerExercises.adapter = exerciseAdapter
            recyclerExercises.isNestedScrollingEnabled = false

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
        }

        fun bind(day: WorkoutDay, position: Int) {
            tvDay.text = DAY_NAMES_ES[day.dayOfWeek] ?: day.dayOfWeek
            val count = day.exercises.size
            tvExerciseCount.text = "$count ejercicio${if (count != 1) "s" else ""}"
            exerciseAdapter.submitExercises(day.exercises)

            if (expandedDays.contains(position)) {
                container.visibility = View.VISIBLE
                ivExpand.rotation = 180f
            } else {
                container.visibility = View.GONE
                ivExpand.rotation = 0f
            }
        }
    }
}