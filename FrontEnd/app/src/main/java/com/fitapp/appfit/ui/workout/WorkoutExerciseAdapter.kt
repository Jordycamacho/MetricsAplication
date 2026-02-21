package com.fitapp.appfit.ui.workout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.response.routine.response.RoutineExerciseResponse
import com.fitapp.appfit.response.routine.response.RoutineResponse
import com.fitapp.appfit.response.routine.response.RoutineSetTemplateResponse
import com.google.android.material.card.MaterialCardView

class WorkoutExerciseAdapter(
    private val onSetValueChanged: (RoutineExerciseResponse, RoutineSetTemplateResponse, String, Double) -> Unit
) : RecyclerView.Adapter<WorkoutExerciseAdapter.ExerciseViewHolder>() {

    private var exercises: List<RoutineExerciseResponse> = emptyList()
    private var expandedPositions = mutableSetOf<Int>()

    fun submitExercises(exercises: List<RoutineExerciseResponse>) {
        this.exercises = exercises.sortedBy { it.position }
        expandedPositions.clear()
        notifyDataSetChanged()
    }

    fun submitRoutine(routine: RoutineResponse) {
        exercises = routine.exercises
            ?.sortedWith(compareBy<RoutineExerciseResponse> {
                it.dayOfWeek ?: ""
            }.thenBy {
                it.position
            }) ?: emptyList()
        expandedPositions.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workout_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(exercises[position], position)
    }

    override fun getItemCount() = exercises.size

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardExercise: MaterialCardView = itemView.findViewById(R.id.card_exercise)
        private val tvExerciseName: TextView = itemView.findViewById(R.id.tv_exercise_name)
        private val tvExerciseRest: TextView = itemView.findViewById(R.id.tv_exercise_rest)
        private val recyclerSets: RecyclerView = itemView.findViewById(R.id.recycler_sets)
        private val ivExpand: View = itemView.findViewById(R.id.iv_expand)
        private val layoutSets: View = itemView.findViewById(R.id.layout_sets_container)

        private lateinit var setAdapter: WorkoutSetAdapter
        private var currentExercise: RoutineExerciseResponse? = null

        init {
            setAdapter = WorkoutSetAdapter { set, valueType, newValue ->
                currentExercise?.let { exercise ->
                    onSetValueChanged(exercise, set, valueType, newValue)
                }
            }
            recyclerSets.layoutManager = LinearLayoutManager(itemView.context)
            recyclerSets.adapter = setAdapter

            cardExercise.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    if (expandedPositions.contains(pos)) {
                        expandedPositions.remove(pos)
                        layoutSets.visibility = View.GONE
                        ivExpand.rotation = 0f
                    } else {
                        expandedPositions.add(pos)
                        layoutSets.visibility = View.VISIBLE
                        ivExpand.rotation = 180f
                    }
                }
            }
        }

        fun bind(exercise: RoutineExerciseResponse, position: Int) {
            currentExercise = exercise
            tvExerciseName.text = exercise.exerciseName ?: "Ejercicio ${exercise.position}"
            exercise.restAfterExercise?.let {
                tvExerciseRest.text = "Descanso: ${it}s"
                tvExerciseRest.visibility = View.VISIBLE
            } ?: run { tvExerciseRest.visibility = View.GONE }

            setAdapter.submitList(exercise.setsTemplate ?: emptyList())

            if (expandedPositions.contains(position)) {
                layoutSets.visibility = View.VISIBLE
                ivExpand.rotation = 180f
            } else {
                layoutSets.visibility = View.GONE
                ivExpand.rotation = 0f
            }
        }
    }
}