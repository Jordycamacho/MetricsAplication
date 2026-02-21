package com.fitapp.appfit.ui.workout

import com.fitapp.appfit.response.routine.response.RoutineExerciseResponse
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.response.routine.response.RoutineResponse
import com.fitapp.appfit.response.routine.response.RoutineSetTemplateResponse
import com.fitapp.appfit.response.workout.WorkoutDay

class WorkoutDayAdapter(
    private val onSetValueChanged: (RoutineExerciseResponse, RoutineSetTemplateResponse, String, Double) -> Unit
) : RecyclerView.Adapter<WorkoutDayAdapter.DayViewHolder>() {

    private var days: List<WorkoutDay> = emptyList()
    private val expandedDays = mutableSetOf<Int>()

    fun submitRoutine(routine: RoutineResponse) {
        val grouped = routine.exercises
            .orEmpty()
            .groupBy { it.dayOfWeek ?: "SIN_DIA" }
            .map { (day, exercises) ->
                WorkoutDay(
                    dayOfWeek = day,
                    exercises = exercises.sortedBy { it.position }
                )
            }
            .sortedBy { it.dayOfWeek }

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

        private val exerciseAdapter = WorkoutExerciseAdapter(onSetValueChanged)

        init {
            recyclerExercises.layoutManager = LinearLayoutManager(itemView.context)
            recyclerExercises.adapter = exerciseAdapter

            itemView.setOnClickListener {
                val pos = adapterPosition
                if (expandedDays.contains(pos)) {
                    expandedDays.remove(pos)
                    container.visibility = View.GONE
                    ivExpand.rotation = 0f
                } else {
                    expandedDays.add(pos)
                    container.visibility = View.VISIBLE
                    ivExpand.rotation = 180f
                }
            }
        }


        fun bind(day: WorkoutDay, position: Int) {
            tvDay.text = day.dayOfWeek
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