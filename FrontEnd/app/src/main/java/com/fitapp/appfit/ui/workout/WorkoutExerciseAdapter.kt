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
import com.fitapp.appfit.timer.RestTimer
import com.google.android.material.card.MaterialCardView

class WorkoutExerciseAdapter(
    private val onSetValueChanged: (RoutineExerciseResponse, RoutineSetTemplateResponse, String, Double) -> Unit
) : RecyclerView.Adapter<WorkoutExerciseAdapter.ExerciseViewHolder>() {

    private var exercises: List<RoutineExerciseResponse> = emptyList()
    private val expandedPositions = mutableSetOf<Int>()

    fun submitExercises(exercises: List<RoutineExerciseResponse>) {
        this.exercises = exercises.sortedBy { it.position }
        expandedPositions.clear()
        notifyDataSetChanged()
    }

    fun submitRoutine(routine: RoutineResponse) {
        exercises = routine.exercises
            ?.sortedWith(compareBy<RoutineExerciseResponse> { it.dayOfWeek ?: "" }
                .thenBy { it.position })
            ?: emptyList()
        expandedPositions.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(exercises[position], position)
    }

    override fun getItemCount() = exercises.size

    override fun onViewRecycled(holder: ExerciseViewHolder) {
        super.onViewRecycled(holder)
        holder.stopTimer()
    }

    override fun onViewDetachedFromWindow(holder: ExerciseViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.stopTimer()
    }

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val cardExercise: MaterialCardView = itemView.findViewById(R.id.card_exercise)
        private val tvExerciseName: TextView       = itemView.findViewById(R.id.tv_exercise_name)
        private val ivExpand: View                 = itemView.findViewById(R.id.iv_expand)
        private val layoutSets: View               = itemView.findViewById(R.id.layout_sets_container)
        private val recyclerSets: RecyclerView     = itemView.findViewById(R.id.recycler_sets)
        // Al final del contenedor de sets
        private val tvExerciseRest: TextView       = itemView.findViewById(R.id.tv_exercise_rest)

        private val setAdapter = WorkoutSetAdapter { set, valueType, newValue ->
            currentExercise?.let { exercise ->
                onSetValueChanged(exercise, set, valueType, newValue)
            }
        }

        private var currentExercise: RoutineExerciseResponse? = null
        private var restSeconds = 0
        private var restTimerActive = false

        private val restTimer = RestTimer(
            onTick = { seconds ->
                if (restTimerActive && itemView.isAttachedToWindow)
                    tvExerciseRest.text = "⏸  ${seconds}s"
            },
            onFinish = {
                if (restTimerActive && itemView.isAttachedToWindow) {
                    restTimerActive = false
                    updateRestLabel()
                }
            }
        )

        fun stopTimer() {
            restTimerActive = false
            restTimer.stop()
        }

        init {
            recyclerSets.layoutManager = LinearLayoutManager(itemView.context)
            recyclerSets.adapter = setAdapter
            recyclerSets.isNestedScrollingEnabled = false

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
            stopTimer()
            currentExercise = exercise
            tvExerciseName.text = exercise.exerciseName ?: "Ejercicio ${exercise.position}"

            // Timer de descanso del ejercicio — aparece al final del bloque de sets
            restSeconds = exercise.restAfterExercise ?: 0
            if (restSeconds > 0) {
                tvExerciseRest.visibility = View.VISIBLE
                updateRestLabel()
                tvExerciseRest.setOnClickListener {
                    if (restTimerActive) {
                        restTimerActive = false
                        restTimer.stop()
                        updateRestLabel()
                    } else {
                        restTimerActive = true
                        restTimer.start(restSeconds)
                    }
                }
            } else {
                tvExerciseRest.visibility = View.GONE
            }

            setAdapter.submitList(exercise.setsTemplate ?: emptyList())

            if (expandedPositions.contains(position)) {
                layoutSets.visibility = View.VISIBLE
                ivExpand.rotation = 180f
            } else {
                layoutSets.visibility = View.GONE
                ivExpand.rotation = 0f
            }
        }

        private fun updateRestLabel() {
            tvExerciseRest.text = "▶  ${restSeconds}s descanso entre ejercicios"
        }
    }
}