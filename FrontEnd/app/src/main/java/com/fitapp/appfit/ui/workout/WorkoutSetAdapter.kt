package com.fitapp.appfit.ui.workout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.response.routine.response.RoutineSetTemplateResponse
import com.google.android.material.card.MaterialCardView

class WorkoutSetAdapter(
    private val onValueChanged: (RoutineSetTemplateResponse, String, Double) -> Unit
) : RecyclerView.Adapter<WorkoutSetAdapter.SetViewHolder>() {

    private var sets: List<RoutineSetTemplateResponse> = emptyList()
    private val currentReps = mutableMapOf<Long, Int>()
    private val currentWeight = mutableMapOf<Long, Double>()

    fun submitList(newSets: List<RoutineSetTemplateResponse>) {
        sets = newSets
        // Inicializar con valores del servidor
        sets.forEach { set ->
            set.parameters?.forEach { param ->
                when {
                    param.parameterName?.contains("rep", ignoreCase = true) == true -> {
                        currentReps[set.id] = param.integerValue ?: 0
                    }
                    param.parameterName?.contains("peso", ignoreCase = true) == true ||
                            param.parameterName?.contains("weight", ignoreCase = true) == true -> {
                        currentWeight[set.id] = param.numericValue ?: 0.0
                    }
                }
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workout_set, parent, false)
        return SetViewHolder(view)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        holder.bind(sets[position])
    }

    override fun getItemCount() = sets.size

    inner class SetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSetTitle: TextView = itemView.findViewById(R.id.tv_set_title)
        private val tvRepsValue: TextView = itemView.findViewById(R.id.tv_reps_value)
        private val tvWeightValue: TextView = itemView.findViewById(R.id.tv_weight_value)
        private val btnDecreaseReps: ImageButton = itemView.findViewById(R.id.btn_decrease_reps)
        private val btnIncreaseReps: ImageButton = itemView.findViewById(R.id.btn_increase_reps)
        private val btnDecreaseWeight: ImageButton = itemView.findViewById(R.id.btn_decrease_weight)
        private val btnIncreaseWeight: ImageButton = itemView.findViewById(R.id.btn_increase_weight)
        private val tvTimer: TextView = itemView.findViewById(R.id.tv_timer)

        fun bind(set: RoutineSetTemplateResponse) {
            val setType = set.setType ?: "NORMAL"
            tvSetTitle.text = "Set ${set.position} - $setType"

            val reps = currentReps[set.id] ?: 0
            val weight = currentWeight[set.id] ?: 0.0
            tvRepsValue.text = reps.toString()
            tvWeightValue.text = weight.toString()

            // Botones de reps
            btnDecreaseReps.setOnClickListener {
                val newReps = (currentReps[set.id] ?: 0) - 1
                if (newReps >= 0) {
                    currentReps[set.id] = newReps
                    tvRepsValue.text = newReps.toString()
                    onValueChanged(set, "reps", newReps.toDouble())
                }
            }

            btnIncreaseReps.setOnClickListener {
                val newReps = (currentReps[set.id] ?: 0) + 1
                currentReps[set.id] = newReps
                tvRepsValue.text = newReps.toString()
                onValueChanged(set, "reps", newReps.toDouble())
            }

            // Botones de peso
            btnDecreaseWeight.setOnClickListener {
                val newWeight = (currentWeight[set.id] ?: 0.0) - 2.5
                if (newWeight >= 0) {
                    currentWeight[set.id] = newWeight
                    tvWeightValue.text = newWeight.toString()
                    onValueChanged(set, "weight", newWeight)
                }
            }

            btnIncreaseWeight.setOnClickListener {
                val newWeight = (currentWeight[set.id] ?: 0.0) + 2.5
                currentWeight[set.id] = newWeight
                tvWeightValue.text = newWeight.toString()
                onValueChanged(set, "weight", newWeight)
            }
        }

        fun updateTimer(secondsLeft: Long) {
            tvTimer.text = "⏱️ $secondsLeft"
            tvTimer.visibility = View.VISIBLE
        }

        fun hideTimer() {
            tvTimer.visibility = View.GONE
        }
    }
}