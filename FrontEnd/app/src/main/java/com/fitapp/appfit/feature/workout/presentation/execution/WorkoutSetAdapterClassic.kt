package com.fitapp.appfit.feature.workout.presentation.execution

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.core.util.RestTimer
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse
import com.fitapp.appfit.feature.routine.model.setparameter.response.RoutineSetParameterResponse
import com.fitapp.appfit.feature.workout.domain.model.WorkoutCompletionState
import com.fitapp.appfit.feature.workout.util.WorkoutHaptics
import com.fitapp.appfit.feature.workout.util.WorkoutSoundManager

class WorkoutSetAdapterClassic(
    private val onValueChanged: (RoutineSetTemplateResponse, String, Double) -> Unit,
    private val onSetCompletedToggled: (RoutineSetTemplateResponse, Boolean) -> Unit,
    private val onSequenceComplete: () -> Unit = {},
    private val completionState: WorkoutCompletionState
) : RecyclerView.Adapter<WorkoutSetAdapterClassic.SetViewHolder>() {

    private var sets: List<RoutineSetTemplateResponse> = emptyList()
    private val currentReps     = mutableMapOf<Long, Int>()
    private val currentParam    = mutableMapOf<Long, Double>()
    private val currentDuration = mutableMapOf<Long, Long>()
    private val paramLabel      = mutableMapOf<Long, String>()
    private val paramType       = mutableMapOf<Long, String>()
    private var activeSequenceIndex = -1

    fun submitList(newSets: List<RoutineSetTemplateResponse>) {
        sets = newSets
        currentReps.clear(); currentParam.clear(); currentDuration.clear()
        paramLabel.clear(); paramType.clear()
        activeSequenceIndex = -1
        sets.forEach { initSetState(it.id, it.parameters ?: emptyList()) }
        notifyDataSetChanged()
    }

    fun isSequenceMode() = sets.isNotEmpty() && sets.all { currentDuration.containsKey(it.id) }

    private fun initSetState(setId: Long, params: List<RoutineSetParameterResponse>) {
        params.firstOrNull { it.repetitions != null }
            ?.let { currentReps[setId] = it.repetitions!! }
        params.firstOrNull { it.parameterType?.uppercase() == "DURATION" && it.durationValue != null }
            ?.let { currentDuration[setId] = it.durationValue!! }
        val numericParam = params.firstOrNull { p ->
            p.parameterType?.uppercase() in listOf("NUMBER","INTEGER","DISTANCE","PERCENTAGE")
                    && (p.numericValue != null || p.integerValue != null)
        }
        if (numericParam != null) {
            currentParam[setId] = numericParam.numericValue ?: numericParam.integerValue?.toDouble() ?: 0.0
            paramLabel[setId] = numericParam.unit ?: inferUnit(numericParam.parameterType, numericParam.parameterName)
            paramType[setId]  = numericParam.parameterType?.lowercase() ?: "number"
        } else {
            currentParam[setId] = 0.0
            paramLabel[setId] = "KG"
            paramType[setId]  = "number"
        }
    }

    private fun inferUnit(type: String?, name: String?): String = when (type?.uppercase()) {
        "DISTANCE" -> "M"; "PERCENTAGE" -> "%"; "INTEGER" -> name?.take(3)?.uppercase() ?: "REP"
        else -> name?.take(3)?.uppercase() ?: "KG"
    }

    private fun onSetRestFinished(index: Int) {
        val next = index + 1
        if (next < sets.size) { activeSequenceIndex = next; notifyItemChanged(next) }
        else { activeSequenceIndex = -1; onSequenceComplete() }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SetViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_workout_set_classic, parent, false))

    override fun onBindViewHolder(h: SetViewHolder, pos: Int) = h.bind(sets[pos], pos)
    override fun getItemCount() = sets.size
    override fun onViewRecycled(h: SetViewHolder) { super.onViewRecycled(h); h.stopAllTimers() }
    override fun onViewDetachedFromWindow(h: SetViewHolder) { super.onViewDetachedFromWindow(h); h.stopAllTimers() }

    // ─────────────────────────────────────────────────────────────────────────

    inner class SetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkboxCompleted: CheckBox = itemView.findViewById(R.id.checkbox_set_completed)
        // Header
        private val viewTypeStripe: View = itemView.findViewById(R.id.view_type_stripe)
        private val tvSetBadge: TextView = itemView.findViewById(R.id.tv_set_badge)
        private val tvParamSummary: TextView = itemView.findViewById(R.id.tv_param_summary)
        // Columna izquierda
        private val layoutReps: LinearLayout = itemView.findViewById(R.id.layout_reps_container)
        private val tvRepsLabel: TextView = itemView.findViewById(R.id.tv_reps_label)
        private val tvRepsValue: TextView = itemView.findViewById(R.id.tv_reps_value)
        private val btnDecReps: ImageButton = itemView.findViewById(R.id.btn_decrease_reps)
        private val btnIncReps: ImageButton = itemView.findViewById(R.id.btn_increase_reps)
        private val viewDivider: View = itemView.findViewById(R.id.view_divider)
        // Columna derecha
        private val layoutParam: LinearLayout = itemView.findViewById(R.id.layout_param_container)
        private val tvParamUnit: TextView = itemView.findViewById(R.id.tv_weight_unit)
        private val tvParamValue: TextView = itemView.findViewById(R.id.tv_weight_value)
        private val btnDecParam: ImageButton = itemView.findViewById(R.id.btn_decrease_weight)
        private val btnIncParam: ImageButton = itemView.findViewById(R.id.btn_increase_weight)
        // Bloque extra de duración
        private val layoutDurationExtra: View = itemView.findViewById(R.id.layout_duration_extra)
        private val tvDurationTimer: TextView = itemView.findViewById(R.id.tv_duration_timer)
        private val btnDecDurationExtra: ImageButton = itemView.findViewById(R.id.btn_decrease_duration_extra)
        private val btnIncDurationExtra: ImageButton = itemView.findViewById(R.id.btn_increase_duration_extra)
        // Descanso
        private val layoutRestContainer: View = itemView.findViewById(R.id.layout_rest_container)
        private val tvRestTimer: TextView = itemView.findViewById(R.id.tv_rest_timer)
        private val tvRestHint: TextView = itemView.findViewById(R.id.tv_rest_hint)
        private var durationDisplayView: TextView? = null

        private var currentSetId: Long = -1L
        private var myIndex = -1
        private var restSeconds = 0
        private var restTimerActive = false
        private var durationTimerActive = false

        private val restTimer = RestTimer(
            onTick = { s ->
                if (restTimerActive && itemView.isAttachedToWindow) {
                    tvRestTimer.text = "${s}s descanso"
                    tvRestHint.text = "STOP"
                }
            },
            onFinish = {
                if (restTimerActive && itemView.isAttachedToWindow) {
                    restTimerActive = false
                    WorkoutHaptics.restFinished(itemView.context)
                    Thread { WorkoutSoundManager.playRestFinished(itemView.context) }.start()
                    updateRestLabel()
                    if (isSequenceMode()) onSetRestFinished(myIndex)
                }
            }
        )

        private val durationTimer = RestTimer(
            onTick = { s ->
                if (durationTimerActive && itemView.isAttachedToWindow) {
                    durationDisplayView?.text = formatDuration(s.toLong())
                }
            },
            onFinish = {
                if (durationTimerActive && itemView.isAttachedToWindow) {
                    durationTimerActive = false
                    durationDisplayView?.text = formatDuration(currentDuration[currentSetId] ?: 0L)
                    WorkoutHaptics.setComplete(itemView.context)
                    Thread { WorkoutSoundManager.playSetComplete(itemView.context) }.start()
                    onValueChanged(sets[myIndex], "completed", 1.0)
                    if (isSequenceMode() && restSeconds > 0) {
                        restTimerActive = true
                        tvRestTimer.text = "${restSeconds}s descanso"
                        tvRestHint.text = "STOP"
                        restTimer.start(restSeconds)
                    } else if (isSequenceMode()) {
                        onSetRestFinished(myIndex)
                    }
                }
            }
        )

        fun stopAllTimers() {
            restTimerActive = false; durationTimerActive = false
            restTimer.stop(); durationTimer.stop()
        }

        fun bind(set: RoutineSetTemplateResponse, position: Int) {
            stopAllTimers()
            durationDisplayView = null
            currentSetId = set.id
            myIndex = position

            bindCheckbox(set)
            bindTypeDecoration(set)
            bindControls(set, position)
            bindRestTimer(set)
        }

        private fun bindCheckbox(set: RoutineSetTemplateResponse) {
            val isCompleted = completionState.isSetCompleted(set.id)
            checkboxCompleted.isChecked = isCompleted

            checkboxCompleted.setOnCheckedChangeListener(null)
            checkboxCompleted.setOnCheckedChangeListener { _, checked ->
                completionState.markSetCompleted(set.id, 0L, checked)
                onSetCompletedToggled(set, checked)
                updateCompletionVisuals(checked)
            }

            updateCompletionVisuals(isCompleted)
        }

        private fun updateCompletionVisuals(completed: Boolean) {
            val alpha = if (completed) 0.5f else 1.0f
            val cardRoot = itemView.findViewById<View>(R.id.card_set_root)
            cardRoot?.alpha = alpha
        }

        private fun bindTypeDecoration(set: RoutineSetTemplateResponse) {
            val (label, colorRes) = setTypeMeta(set.setType ?: "NORMAL")
            val color = ContextCompat.getColor(itemView.context, colorRes)
            viewTypeStripe.setBackgroundColor(color)
            tvSetBadge.text = label
            tvSetBadge.backgroundTintList = ContextCompat.getColorStateList(itemView.context, colorRes)
            tvSetBadge.setTextColor(
                if (colorRes == R.color.set_type_normal) ContextCompat.getColor(itemView.context, R.color.background_dark)
                else Color.WHITE
            )
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

        private fun bindControls(set: RoutineSetTemplateResponse, position: Int) {
            val hasReps     = currentReps.containsKey(set.id)
            val hasDuration = currentDuration.containsKey(set.id)
            val hasParam    = set.parameters?.any {
                it.parameterType?.uppercase() in listOf("NUMBER","INTEGER","DISTANCE","PERCENTAGE")
            } == true

            tvParamSummary.text = buildSummary(hasReps, hasDuration, hasParam, set.id)

            layoutReps.visibility = View.GONE
            layoutParam.visibility = View.GONE
            viewDivider.visibility = View.GONE
            layoutDurationExtra.visibility = View.GONE
            setColumnWeight(layoutReps, 1f)
            setColumnWeight(layoutParam, 1f)

            tvRepsValue.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_primary_dark))
            tvRepsValue.textSize = 38f
            tvRepsValue.isClickable = false
            tvRepsValue.isFocusable = false
            tvRepsValue.setOnClickListener(null)
            tvParamValue.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_primary_dark))
            tvParamValue.textSize = 38f
            tvParamValue.isClickable = false
            tvParamValue.isFocusable = false
            tvParamValue.setOnClickListener(null)

            when {
                hasReps && hasParam && !hasDuration -> {
                    setupRepsColumn(set); setupParamColumn(set)
                    viewDivider.visibility = View.VISIBLE
                }
                hasReps && !hasParam && !hasDuration -> {
                    setupRepsColumn(set)
                    setColumnWeight(layoutReps, 2f)
                }
                !hasReps && hasDuration && hasParam -> {
                    setupDurationInColumn(set, position, isLeft = true)
                    setupParamColumn(set)
                    viewDivider.visibility = View.VISIBLE
                }
                !hasReps && hasDuration && !hasParam -> {
                    setupDurationInColumn(set, position, isLeft = true)
                    setColumnWeight(layoutReps, 2f)
                }
                hasReps && hasDuration && !hasParam -> {
                    setupRepsColumn(set)
                    setupDurationInColumn(set, position, isLeft = false)
                    viewDivider.visibility = View.VISIBLE
                }
                hasReps && hasParam && hasDuration -> {
                    setupRepsColumn(set); setupParamColumn(set)
                    viewDivider.visibility = View.VISIBLE
                    setupDurationExtra(set, position)
                }
            }
        }

        private fun setColumnWeight(layout: LinearLayout, weight: Float) {
            val lp = layout.layoutParams as? LinearLayout.LayoutParams ?: return
            lp.weight = weight
            layout.layoutParams = lp
        }

        private fun buildSummary(hasReps: Boolean, hasDuration: Boolean, hasParam: Boolean, setId: Long): String {
            val unit = paramLabel[setId] ?: "KG"
            return listOfNotNull(
                if (hasReps) "REPS" else null,
                if (hasDuration) "TIEMPO" else null,
                if (hasParam) unit else null
            ).joinToString(" · ")
        }

        private fun setupRepsColumn(set: RoutineSetTemplateResponse) {
            layoutReps.visibility = View.VISIBLE
            tvRepsLabel.text = when (set.setType?.uppercase()) {
                "ISOMETRIC", "REST_PAUSE" -> "SERIES"
                "DROP_SET" -> "REPS ↓"
                else -> "REPS"
            }
            tvRepsValue.text = currentReps[set.id].toString()
            btnDecReps.setOnClickListener {
                val cur = currentReps[set.id] ?: 0
                if (cur > 0) { val n = cur-1; currentReps[set.id]=n; tvRepsValue.text=n.toString(); onValueChanged(set,"reps",n.toDouble()) }
            }
            btnIncReps.setOnClickListener {
                val n=(currentReps[set.id]?:0)+1; currentReps[set.id]=n; tvRepsValue.text=n.toString(); onValueChanged(set,"reps",n.toDouble())
            }
        }

        private fun setupParamColumn(set: RoutineSetTemplateResponse) {
            layoutParam.visibility = View.VISIBLE
            val value = currentParam[set.id] ?: 0.0
            val unit  = paramLabel[set.id] ?: "KG"
            val type  = paramType[set.id] ?: "number"
            tvParamUnit.text = unit
            tvParamValue.text = formatValue(value, type)
            val step = stepFor(unit, type)
            btnDecParam.setOnClickListener {
                val cur = currentParam[set.id] ?: 0.0
                if (cur >= step) { val n=cur-step; currentParam[set.id]=n; tvParamValue.text=formatValue(n,type); onValueChanged(set,"param",n) }
            }
            btnIncParam.setOnClickListener {
                val n=(currentParam[set.id]?:0.0)+step; currentParam[set.id]=n; tvParamValue.text=formatValue(n,type); onValueChanged(set,"param",n)
            }
        }

        private fun setupDurationInColumn(set: RoutineSetTemplateResponse, position: Int, isLeft: Boolean) {
            val layout  = if (isLeft) layoutReps  else layoutParam
            val tvLabel = if (isLeft) tvRepsLabel  else tvParamUnit
            val tvValue = if (isLeft) tvRepsValue  else tvParamValue
            val btnDec  = if (isLeft) btnDecReps   else btnDecParam
            val btnInc  = if (isLeft) btnIncReps   else btnIncParam

            layout.visibility = View.VISIBLE
            tvLabel.text = "TIEMPO"
            tvValue.text = formatDuration(currentDuration[set.id] ?: 0L)
            tvValue.setTextColor(ContextCompat.getColor(itemView.context, R.color.gold_primary))
            tvValue.textSize = 32f
            tvValue.isClickable = true
            tvValue.isFocusable = true

            durationDisplayView = tvValue

            tvValue.setOnClickListener { toggleDuration(set, position) }
            btnDec.setOnClickListener { adjustDuration(set, -5L) }
            btnInc.setOnClickListener { adjustDuration(set, +5L) }

            autoStartIfSequence(set, position)
        }

        private fun setupDurationExtra(set: RoutineSetTemplateResponse, position: Int) {
            layoutDurationExtra.visibility = View.VISIBLE
            tvDurationTimer.text = formatDuration(currentDuration[set.id] ?: 0L)

            durationDisplayView = tvDurationTimer

            tvDurationTimer.setOnClickListener { toggleDuration(set, position) }
            btnDecDurationExtra.setOnClickListener { adjustDuration(set, -5L) }
            btnIncDurationExtra.setOnClickListener { adjustDuration(set, +5L) }

            autoStartIfSequence(set, position)
        }

        private fun autoStartIfSequence(set: RoutineSetTemplateResponse, position: Int) {
            if (isSequenceMode() && activeSequenceIndex == position && !durationTimerActive) {
                WorkoutHaptics.exerciseStart(itemView.context)
                durationTimerActive = true
                durationTimer.start((currentDuration[set.id] ?: 0L).toInt())
            }
        }

        private fun toggleDuration(set: RoutineSetTemplateResponse, position: Int) {
            if (durationTimerActive) {
                durationTimerActive = false
                durationTimer.stop()
                durationDisplayView?.text = formatDuration(currentDuration[set.id] ?: 0L)
            } else {
                WorkoutHaptics.exerciseStart(itemView.context)
                durationTimerActive = true
                durationTimer.start((currentDuration[set.id] ?: 0L).toInt())
            }
        }

        private fun adjustDuration(set: RoutineSetTemplateResponse, delta: Long) {
            if (durationTimerActive) return
            val new = ((currentDuration[set.id] ?: 0L) + delta).coerceAtLeast(5L)
            currentDuration[set.id] = new
            durationDisplayView?.text = formatDuration(new)
            onValueChanged(set, "duration", new.toDouble())
        }

        private fun bindRestTimer(set: RoutineSetTemplateResponse) {
            restSeconds = set.restAfterSet ?: 0
            if (restSeconds <= 0) { layoutRestContainer.visibility = View.GONE; return }
            layoutRestContainer.visibility = View.VISIBLE
            updateRestLabel()
            layoutRestContainer.setOnClickListener {
                if (restTimerActive) { restTimerActive=false; restTimer.stop(); updateRestLabel() }
                else { restTimerActive=true; restTimer.start(restSeconds) }
            }
        }

        private fun updateRestLabel() {
            tvRestTimer.text = "${restSeconds}s descanso"
            tvRestHint.text = "TAP"
        }

        private fun stepFor(unit: String, type: String): Double = when {
            type == "percentage" -> 5.0; type == "distance" -> 1.0
            unit.uppercase() == "KG" -> 2.5; unit.uppercase() == "LB" -> 5.0
            else -> 1.0
        }

        private fun formatValue(v: Double, type: String): String = when (type) {
            "percentage" -> "%.0f".format(v); "integer" -> v.toInt().toString()
            else -> if (v % 1.0 == 0.0) v.toInt().toString() else "%.1f".format(v)
        }

        private fun formatDuration(seconds: Long): String {
            val m = seconds / 60; val s = seconds % 60
            return if (m > 0) "${m}m ${s.toString().padStart(2,'0')}s" else "${s}s"
        }
    }
}