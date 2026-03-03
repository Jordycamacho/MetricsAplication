package com.fitapp.appfit.ui.workout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.response.routine.response.RoutineSetTemplateResponse
import com.fitapp.appfit.response.sets.response.RoutineSetParameterResponse
import com.fitapp.appfit.timer.RestTimer

class WorkoutSetAdapter(
    private val onValueChanged: (RoutineSetTemplateResponse, String, Double) -> Unit
) : RecyclerView.Adapter<WorkoutSetAdapter.SetViewHolder>() {

    private var sets: List<RoutineSetTemplateResponse> = emptyList()

    private val currentReps     = mutableMapOf<Long, Int>()
    private val currentParam    = mutableMapOf<Long, Double>()
    private val currentDuration = mutableMapOf<Long, Long>()  // segundos
    private val paramLabel      = mutableMapOf<Long, String>()
    private val paramType       = mutableMapOf<Long, String>()

    fun submitList(newSets: List<RoutineSetTemplateResponse>) {
        sets = newSets
        currentReps.clear()
        currentParam.clear()
        currentDuration.clear()
        paramLabel.clear()
        paramType.clear()
        sets.forEach { set -> initSetState(set.id, set.parameters ?: emptyList()) }
        notifyDataSetChanged()
    }

    private fun initSetState(setId: Long, params: List<RoutineSetParameterResponse>) {
        // Reps
        params.firstOrNull { it.repetitions != null }?.let {
            currentReps[setId] = it.repetitions!!
        }

        // Duración — durationValue llega en segundos
        params.firstOrNull {
            it.parameterType?.uppercase() == "DURATION" && it.durationValue != null
        }?.let {
            currentDuration[setId] = it.durationValue!!  // ya en segundos
        }

        // Parámetro numérico principal
        val numericParam = params.firstOrNull { p ->
            p.parameterType?.uppercase() in listOf("NUMBER", "INTEGER", "DISTANCE", "PERCENTAGE")
                    && (p.numericValue != null || p.integerValue != null)
        }
        if (numericParam != null) {
            currentParam[setId] = numericParam.numericValue
                ?: numericParam.integerValue?.toDouble() ?: 0.0
            paramLabel[setId] = numericParam.unit
                ?: inferUnit(numericParam.parameterType, numericParam.parameterName)
            paramType[setId] = numericParam.parameterType?.lowercase() ?: "number"
        } else {
            currentParam[setId] = 0.0
            paramLabel[setId] = "KG"
            paramType[setId]  = "number"
        }
    }

    private fun inferUnit(type: String?, name: String?): String = when (type?.uppercase()) {
        "DISTANCE"   -> "M"
        "PERCENTAGE" -> "%"
        "INTEGER"    -> name?.take(3)?.uppercase() ?: "REP"
        else         -> name?.take(3)?.uppercase() ?: "KG"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_set, parent, false)
        return SetViewHolder(view)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        holder.bind(sets[position], position + 1)
    }

    override fun onViewRecycled(holder: SetViewHolder) {
        super.onViewRecycled(holder)
        holder.stopAllTimers()
    }

    override fun onViewDetachedFromWindow(holder: SetViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.stopAllTimers()
    }

    override fun getItemCount() = sets.size

    inner class SetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // Header
        private val tvSetBadge: TextView      = itemView.findViewById(R.id.tv_set_badge)
        private val tvSetNumber: TextView     = itemView.findViewById(R.id.tv_set_number)

        // Reps
        private val layoutReps: View          = itemView.findViewById(R.id.layout_reps_container)
        private val tvRepsLabel: TextView     = itemView.findViewById(R.id.tv_reps_label)
        private val tvRepsValue: TextView     = itemView.findViewById(R.id.tv_reps_value)
        private val btnDecReps: ImageButton   = itemView.findViewById(R.id.btn_decrease_reps)
        private val btnIncReps: ImageButton   = itemView.findViewById(R.id.btn_increase_reps)

        // Divider
        private val viewDivider: View         = itemView.findViewById(R.id.view_divider)

        // Param numérico
        private val layoutParam: View         = itemView.findViewById(R.id.layout_param_container)
        private val tvParamUnit: TextView     = itemView.findViewById(R.id.tv_weight_unit)
        private val tvParamValue: TextView    = itemView.findViewById(R.id.tv_weight_value)
        private val btnDecParam: ImageButton  = itemView.findViewById(R.id.btn_decrease_weight)
        private val btnIncParam: ImageButton  = itemView.findViewById(R.id.btn_increase_weight)

        // Contador de duración (tappable, sin botón play)
        private val layoutDuration: View        = itemView.findViewById(R.id.layout_duration_container)
        private val tvDurationTimer: TextView   = itemView.findViewById(R.id.tv_duration_timer)
        private val btnDecDuration: ImageButton = itemView.findViewById(R.id.btn_decrease_duration)
        private val btnIncDuration: ImageButton = itemView.findViewById(R.id.btn_increase_duration)

        // Rest timer del set
        private val tvRestTimer: TextView     = itemView.findViewById(R.id.tv_rest_timer)

        private var currentSetId: Long = -1L
        private var restSeconds = 0
        private var restTimerActive = false
        private var durationTimerActive = false

        private val restTimer = RestTimer(
            onTick = { seconds ->
                if (restTimerActive && itemView.isAttachedToWindow)
                    tvRestTimer.text = "⏸  ${seconds}s"
            },
            onFinish = {
                if (restTimerActive && itemView.isAttachedToWindow) {
                    restTimerActive = false
                    updateRestLabel()
                }
            }
        )

        private val durationTimer = RestTimer(
            onTick = { seconds ->
                if (durationTimerActive && itemView.isAttachedToWindow)
                    tvDurationTimer.text = formatDuration(seconds.toLong())
            },
            onFinish = {
                if (durationTimerActive && itemView.isAttachedToWindow) {
                    durationTimerActive = false
                    // Volver a mostrar el valor objetivo
                    tvDurationTimer.text = formatDuration(currentDuration[currentSetId] ?: 0L)
                }
            }
        )

        fun stopAllTimers() {
            restTimerActive = false
            durationTimerActive = false
            restTimer.stop()
            durationTimer.stop()
        }

        fun bind(set: RoutineSetTemplateResponse, positionInList: Int) {
            stopAllTimers()
            currentSetId = set.id
            bindHeader(set, positionInList)
            bindRepsBlock(set)
            bindParamBlock(set)
            bindDurationBlock(set)
            bindRestTimer(set)
        }

        // ── Header ────────────────────────────────────────────────────────────

        private fun bindHeader(set: RoutineSetTemplateResponse, positionInList: Int) {
            tvSetNumber.text = "Serie $positionInList"
            val (label, colorRes) = setTypeMeta(set.setType ?: "NORMAL")
            tvSetBadge.text = label
            tvSetBadge.backgroundTintList =
                ContextCompat.getColorStateList(itemView.context, colorRes)
        }

        private fun setTypeMeta(type: String): Pair<String, Int> = when (type.uppercase()) {
            "WARM_UP"         -> "Calentamiento"  to R.color.set_type_warmup
            "DROP_SET"        -> "Drop Set"        to R.color.set_type_drop
            "SUPER_SET"       -> "Super Set"       to R.color.set_type_super
            "GIANT_SET"       -> "Giant Set"       to R.color.set_type_giant
            "PYRAMID"         -> "Pirámide"        to R.color.set_type_pyramid
            "REVERSE_PYRAMID" -> "Pir. Inversa"    to R.color.set_type_pyramid
            "CLUSTER"         -> "Cluster"         to R.color.set_type_cluster
            "REST_PAUSE"      -> "Rest-Pause"      to R.color.set_type_rest_pause
            "ECCENTRIC"       -> "Excéntrico"      to R.color.set_type_eccentric
            "ISOMETRIC"       -> "Isométrico"      to R.color.set_type_isometric
            else              -> "Normal"          to R.color.set_type_normal
        }

        // ── Reps ─────────────────────────────────────────────────────────────

        private fun bindRepsBlock(set: RoutineSetTemplateResponse) {
            val hasReps = currentReps.containsKey(set.id)
            layoutReps.visibility = if (hasReps) View.VISIBLE else View.GONE
            viewDivider.visibility = if (hasReps) View.VISIBLE else View.GONE
            if (!hasReps) return

            tvRepsValue.text = currentReps[set.id].toString()
            tvRepsLabel.text = when (set.setType?.uppercase()) {
                "ISOMETRIC", "REST_PAUSE" -> "SERIES"
                "DROP_SET"               -> "REPS ↓"
                else                     -> "REPS"
            }

            btnDecReps.setOnClickListener {
                val cur = currentReps[set.id] ?: 0
                if (cur > 0) {
                    val new = cur - 1
                    currentReps[set.id] = new
                    tvRepsValue.text = new.toString()
                    onValueChanged(set, "reps", new.toDouble())
                }
            }
            btnIncReps.setOnClickListener {
                val new = (currentReps[set.id] ?: 0) + 1
                currentReps[set.id] = new
                tvRepsValue.text = new.toString()
                onValueChanged(set, "reps", new.toDouble())
            }
        }

        // ── Param numérico ────────────────────────────────────────────────────

        private fun bindParamBlock(set: RoutineSetTemplateResponse) {
            // Ocultar si el único parámetro es duración
            val onlyDuration = currentDuration.containsKey(set.id) &&
                    (set.parameters?.none {
                        it.parameterType?.uppercase() in listOf("NUMBER","INTEGER","DISTANCE","PERCENTAGE")
                    } == true)

            layoutParam.visibility = if (onlyDuration) View.GONE else View.VISIBLE
            if (onlyDuration) {
                viewDivider.visibility = View.GONE
                return
            }

            val value = currentParam[set.id] ?: 0.0
            val unit  = paramLabel[set.id] ?: "KG"
            val type  = paramType[set.id] ?: "number"
            tvParamValue.text = formatValue(value, type)
            tvParamUnit.text  = unit

            val step = stepFor(unit, type)
            btnDecParam.setOnClickListener {
                val cur = currentParam[set.id] ?: 0.0
                if (cur >= step) {
                    val new = cur - step
                    currentParam[set.id] = new
                    tvParamValue.text = formatValue(new, type)
                    onValueChanged(set, "param", new)
                }
            }
            btnIncParam.setOnClickListener {
                val new = (currentParam[set.id] ?: 0.0) + step
                currentParam[set.id] = new
                tvParamValue.text = formatValue(new, type)
                onValueChanged(set, "param", new)
            }
        }

        private fun stepFor(unit: String, type: String): Double = when {
            type == "percentage"      -> 5.0
            type == "distance"        -> 1.0
            unit.uppercase() == "KG" -> 2.5
            unit.uppercase() == "LB" -> 5.0
            else                      -> 1.0
        }

        // ── Contador de duración — tap para iniciar/parar ─────────────────────

        private fun bindDurationBlock(set: RoutineSetTemplateResponse) {
            val hasDuration = currentDuration.containsKey(set.id)
            layoutDuration.visibility = if (hasDuration) View.VISIBLE else View.GONE
            if (!hasDuration) return

            val targetSecs = currentDuration[set.id] ?: 0L
            tvDurationTimer.text = formatDuration(targetSecs)

            // Tap en el número → arranca o para
            tvDurationTimer.setOnClickListener {
                if (durationTimerActive) {
                    durationTimerActive = false
                    durationTimer.stop()
                    tvDurationTimer.text = formatDuration(currentDuration[set.id] ?: 0L)
                } else {
                    durationTimerActive = true
                    durationTimer.start((currentDuration[set.id] ?: 0L).toInt())
                }
            }

            // Botones ±5s — solo cuando el timer está parado
            btnDecDuration.setOnClickListener {
                if (durationTimerActive) return@setOnClickListener
                val cur = currentDuration[set.id] ?: 0L
                if (cur > 5L) {
                    currentDuration[set.id] = cur - 5L
                    tvDurationTimer.text = formatDuration(currentDuration[set.id]!!)
                    onValueChanged(set, "duration", currentDuration[set.id]!!.toDouble())
                }
            }
            btnIncDuration.setOnClickListener {
                if (durationTimerActive) return@setOnClickListener
                val cur = currentDuration[set.id] ?: 0L
                currentDuration[set.id] = cur + 5L
                tvDurationTimer.text = formatDuration(currentDuration[set.id]!!)
                onValueChanged(set, "duration", currentDuration[set.id]!!.toDouble())
            }
        }

        // ── Rest timer del set ────────────────────────────────────────────────

        private fun bindRestTimer(set: RoutineSetTemplateResponse) {
            restSeconds = set.restAfterSet ?: 0
            updateRestLabel()
            tvRestTimer.isClickable = restSeconds > 0
            tvRestTimer.setOnClickListener {
                if (restSeconds <= 0) return@setOnClickListener
                if (restTimerActive) {
                    restTimerActive = false
                    restTimer.stop()
                    updateRestLabel()
                } else {
                    restTimerActive = true
                    restTimer.start(restSeconds)
                }
            }
        }

        private fun updateRestLabel() {
            tvRestTimer.text =
                if (restSeconds > 0) "▶  ${restSeconds}s descanso" else "Sin descanso"
        }

        // ── Helpers ───────────────────────────────────────────────────────────

        private fun formatValue(v: Double, type: String): String = when (type) {
            "percentage" -> "%.0f".format(v)
            "integer"    -> v.toInt().toString()
            else         -> if (v % 1.0 == 0.0) v.toInt().toString() else "%.1f".format(v)
        }

        private fun formatDuration(seconds: Long): String {
            val m = seconds / 60
            val s = seconds % 60
            return if (m > 0) "${m}m ${s.toString().padStart(2, '0')}s" else "${s}s"
        }
    }
}