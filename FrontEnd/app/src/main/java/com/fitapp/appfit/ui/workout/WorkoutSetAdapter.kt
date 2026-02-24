package com.fitapp.appfit.ui.workout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.response.routine.response.RoutineSetTemplateResponse
import com.fitapp.appfit.timer.RestTimer

class WorkoutSetAdapter(
    private val onValueChanged: (RoutineSetTemplateResponse, String, Double) -> Unit
) : RecyclerView.Adapter<WorkoutSetAdapter.SetViewHolder>() {

    private var sets: List<RoutineSetTemplateResponse> = emptyList()
    private val currentReps = mutableMapOf<Long, Int>()
    private val currentParam = mutableMapOf<Long, Double>()
    private val paramLabel = mutableMapOf<Long, String>()

    fun submitList(newSets: List<RoutineSetTemplateResponse>) {
        sets = newSets

        currentReps.clear()
        currentParam.clear()
        paramLabel.clear()

        sets.forEach { set ->
            android.util.Log.d("SET_DEBUG", "Set id=${set.id} pos=${set.position}")
            val repsValue = set.parameters
                ?.firstOrNull { it.repetitions != null }
                ?.repetitions
                ?: set.parameters
                    ?.firstOrNull { it.parameterName?.lowercase()?.contains("rep") == true }
                    ?.let { it.integerValue ?: it.numericValue?.toInt() }

            currentReps[set.id] = repsValue ?: 10

            // PARÁMETRO SECUNDARIO
            val paramEntry = set.parameters?.firstOrNull { param ->
                android.util.Log.d("SET_DEBUG", "  param: name=${param.parameterName} " +
                        "repetitions=${param.repetitions} integerValue=${param.integerValue} " +
                        "numericValue=${param.numericValue} unit=${param.unit}")
                param.repetitions == null &&
                        param.parameterName?.lowercase()?.contains("rep") != true
            }
            currentParam[set.id] = paramEntry?.numericValue
                ?: paramEntry?.integerValue?.toDouble()
                        ?: 0.0
            paramLabel[set.id] = paramEntry?.unit
                ?: paramEntry?.parameterName
                        ?: "KG"
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_set, parent, false)
        return SetViewHolder(view)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        holder.bind(sets[position])
    }

    override fun onViewRecycled(holder: SetViewHolder) {
        super.onViewRecycled(holder)
        holder.stopTimer()
    }

    override fun onViewDetachedFromWindow(holder: SetViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.stopTimer()
    }

    override fun getItemCount() = sets.size

    inner class SetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSetTitle: TextView = itemView.findViewById(R.id.tv_set_title)
        private val tvRepsValue: TextView = itemView.findViewById(R.id.tv_reps_value)
        private val tvWeightValue: TextView = itemView.findViewById(R.id.tv_weight_value)
        private val tvWeightUnit: TextView = itemView.findViewById(R.id.tv_weight_unit)
        private val btnDecreaseReps: ImageButton = itemView.findViewById(R.id.btn_decrease_reps)
        private val btnIncreaseReps: ImageButton = itemView.findViewById(R.id.btn_increase_reps)
        private val btnDecreaseWeight: ImageButton = itemView.findViewById(R.id.btn_decrease_weight)
        private val btnIncreaseWeight: ImageButton = itemView.findViewById(R.id.btn_increase_weight)
        private val tvRestTimer: TextView = itemView.findViewById(R.id.tv_rest_timer)

        private var currentSetId: Long = -1L
        private var restSeconds = 0
        private var timerActive = false

        private val restTimer = RestTimer(
            onTick = { seconds ->
                if (timerActive && itemView.isAttachedToWindow) {
                    tvRestTimer.text = "⏱ ${seconds}s"
                }
            },
            onFinish = {
                if (timerActive && itemView.isAttachedToWindow) {
                    timerActive = false
                    tvRestTimer.text = "Listo — tocá para repetir"
                }
            }
        )

        fun stopTimer() {
            timerActive = false
            restTimer.stop()
        }

        fun bind(set: RoutineSetTemplateResponse) {
            stopTimer()
            currentSetId = set.id

            tvSetTitle.text = "Serie ${set.position}  ·  ${set.setType ?: "NORMAL"}"

            val reps = currentReps[set.id] ?: 10
            val weight = currentParam[set.id] ?: 0.0
            val unit = paramLabel[set.id] ?: "KG"

            tvRepsValue.text = reps.toString()
            tvWeightValue.text = formatValue(weight)
            tvWeightUnit.text = unit

            restSeconds = set.restAfterSet ?: 0
            updateTimerLabel()

            tvRestTimer.isClickable = restSeconds > 0
            tvRestTimer.setOnClickListener {
                if (restSeconds <= 0) return@setOnClickListener
                if (timerActive) {
                    stopTimer()
                    updateTimerLabel()
                } else {
                    timerActive = true
                    restTimer.start(restSeconds)
                }
            }

            val step = if (unit.uppercase() == "KG") 2.5 else 1.0

            btnDecreaseReps.setOnClickListener {
                val cur = currentReps[set.id] ?: 0
                if (cur > 0) {
                    val new = cur - 1
                    currentReps[set.id] = new
                    tvRepsValue.text = new.toString()
                    onValueChanged(set, "reps", new.toDouble())
                }
            }

            btnIncreaseReps.setOnClickListener {
                val new = (currentReps[set.id] ?: 0) + 1
                currentReps[set.id] = new
                tvRepsValue.text = new.toString()
                onValueChanged(set, "reps", new.toDouble())
            }

            btnDecreaseWeight.setOnClickListener {
                val cur = currentParam[set.id] ?: 0.0
                if (cur >= step) {
                    val new = cur - step
                    currentParam[set.id] = new
                    tvWeightValue.text = formatValue(new)
                    onValueChanged(set, "weight", new)
                }
            }

            btnIncreaseWeight.setOnClickListener {
                val new = (currentParam[set.id] ?: 0.0) + step
                currentParam[set.id] = new
                tvWeightValue.text = formatValue(new)
                onValueChanged(set, "weight", new)
            }
        }

        private fun updateTimerLabel() {
            tvRestTimer.text = if (restSeconds > 0) "▶  ${restSeconds}s descanso" else "Sin descanso"
        }

        private fun formatValue(v: Double): String =
            if (v % 1.0 == 0.0) v.toInt().toString() else "%.1f".format(v)
    }
}