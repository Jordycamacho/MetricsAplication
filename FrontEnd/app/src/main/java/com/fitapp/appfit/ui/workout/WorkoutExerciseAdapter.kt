package com.fitapp.appfit.ui.workout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.response.routine.response.RoutineExerciseResponse
import com.fitapp.appfit.response.routine.response.RoutineResponse
import com.fitapp.appfit.response.routine.response.RoutineSetTemplateResponse
import com.fitapp.appfit.timer.RestTimer
import com.fitapp.appfit.utils.WorkoutHaptics

class WorkoutExerciseAdapter(
    private val onSetValueChanged: (RoutineExerciseResponse, RoutineSetTemplateResponse, String, Double) -> Unit
) : RecyclerView.Adapter<WorkoutExerciseAdapter.ExerciseViewHolder>() {

    private var exercises: List<RoutineExerciseResponse> = emptyList()
    private val expandedPositions = mutableSetOf<Int>()

    fun submitExercises(list: List<RoutineExerciseResponse>) {
        exercises = list.sortedBy { it.position }; expandedPositions.clear(); notifyDataSetChanged()
    }

    fun submitRoutine(routine: RoutineResponse) {
        exercises = routine.exercises
            ?.sortedWith(compareBy<RoutineExerciseResponse> { it.dayOfWeek ?: "" }.thenBy { it.position })
            ?: emptyList()
        expandedPositions.clear(); notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ExerciseViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_workout_exercise, parent, false))

    override fun onBindViewHolder(h: ExerciseViewHolder, pos: Int) = h.bind(exercises[pos], pos)
    override fun getItemCount() = exercises.size
    override fun onViewRecycled(h: ExerciseViewHolder) { super.onViewRecycled(h); h.stopTimer() }
    override fun onViewDetachedFromWindow(h: ExerciseViewHolder) { super.onViewDetachedFromWindow(h); h.stopTimer() }

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val cardExercise: View             = itemView.findViewById(R.id.card_exercise)
        private val tvExerciseName: TextView       = itemView.findViewById(R.id.tv_exercise_name)
        private val ivExpand: View                 = itemView.findViewById(R.id.iv_expand)
        private val layoutSets: View               = itemView.findViewById(R.id.layout_sets_container)
        private val recyclerSets: RecyclerView     = itemView.findViewById(R.id.recycler_sets)
        private val layoutExerciseRest: LinearLayout = itemView.findViewById(R.id.layout_exercise_rest)
        private val tvExerciseRest: TextView       = itemView.findViewById(R.id.tv_exercise_rest)
        private val tvExerciseRestHint: TextView   = itemView.findViewById(R.id.tv_exercise_rest_hint)

        private var currentExercise: RoutineExerciseResponse? = null
        private var restSeconds = 0
        private var restTimerActive = false

        private val setAdapter = WorkoutSetAdapter(
            onValueChanged = { set, type, value ->
                currentExercise?.let { onSetValueChanged(it, set, type, value) }
            },
            onSequenceComplete = {
                // Al terminar la secuencia de sets, activar descanso del ejercicio
                if (restSeconds > 0 && itemView.isAttachedToWindow) {
                    WorkoutHaptics.exerciseComplete(itemView.context)
                    restTimerActive = true
                    tvExerciseRest.text = "${restSeconds}s"
                    tvExerciseRestHint.text = "STOP"
                    restTimer.start(restSeconds)
                }
            }
        )

        private val restTimer = RestTimer(
            onTick = { s ->
                if (restTimerActive && itemView.isAttachedToWindow) {
                    tvExerciseRest.text = "${s}s"
                    tvExerciseRestHint.text = "STOP"
                }
            },
            onFinish = {
                if (restTimerActive && itemView.isAttachedToWindow) {
                    restTimerActive = false
                    WorkoutHaptics.restFinished(itemView.context)
                    tvExerciseRest.text = "${restSeconds}s"
                    tvExerciseRestHint.text = "TAP"
                }
            }
        )

        fun stopTimer() { restTimerActive = false; restTimer.stop() }

        init {
            recyclerSets.layoutManager = LinearLayoutManager(itemView.context)
            recyclerSets.adapter = setAdapter
            recyclerSets.isNestedScrollingEnabled = false

            cardExercise.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    if (expandedPositions.contains(pos)) {
                        expandedPositions.remove(pos); layoutSets.visibility = View.GONE; ivExpand.rotation = 0f
                    } else {
                        expandedPositions.add(pos); layoutSets.visibility = View.VISIBLE; ivExpand.rotation = 180f
                    }
                }
            }
        }

        fun bind(exercise: RoutineExerciseResponse, position: Int) {
            stopTimer()
            currentExercise = exercise
            tvExerciseName.text = exercise.exerciseName ?: "Ejercicio ${exercise.position}"

            restSeconds = exercise.restAfterExercise ?: 0
            if (restSeconds > 0) {
                // Siempre visible — no depende de la secuencia
                layoutExerciseRest.visibility = View.VISIBLE
                tvExerciseRest.text = "${restSeconds}s"
                tvExerciseRestHint.text = "TAP"
                layoutExerciseRest.setOnClickListener {
                    if (restTimerActive) {
                        restTimerActive = false; restTimer.stop()
                        tvExerciseRest.text = "${restSeconds}s"; tvExerciseRestHint.text = "TAP"
                    } else {
                        restTimerActive = true; restTimer.start(restSeconds)
                    }
                }
            } else {
                layoutExerciseRest.visibility = View.GONE
            }

            setAdapter.submitList(exercise.setsTemplate ?: emptyList())

            if (expandedPositions.contains(position)) {
                layoutSets.visibility = View.VISIBLE; ivExpand.rotation = 180f
            } else {
                layoutSets.visibility = View.GONE; ivExpand.rotation = 0f
            }
        }
    }
}