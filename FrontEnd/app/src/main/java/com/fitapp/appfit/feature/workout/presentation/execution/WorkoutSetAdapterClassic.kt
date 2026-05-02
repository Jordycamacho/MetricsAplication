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
import android.util.Log
import com.fitapp.appfit.core.util.RestTimer
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse
import com.fitapp.appfit.feature.routine.model.setparameter.response.RoutineSetParameterResponse
import com.fitapp.appfit.feature.workout.domain.model.WorkoutCompletionState
import com.fitapp.appfit.feature.workout.presentation.execution.manager.SetParameterStateManager
import com.fitapp.appfit.feature.workout.util.WorkoutHaptics
import com.fitapp.appfit.feature.workout.util.WorkoutSoundManager

class WorkoutSetAdapterClassic(
    private val stateManager: SetParameterStateManager,
    private val onValueChanged: (RoutineSetTemplateResponse, String, Double) -> Unit,
    private val onSetCompletedToggled: (RoutineSetTemplateResponse, Boolean) -> Unit,
    private val onSequenceComplete: () -> Unit = {},
    private val completionState: WorkoutCompletionState
) : RecyclerView.Adapter<WorkoutSetAdapterClassic.SetViewHolder>() {

    private var sets: List<RoutineSetTemplateResponse> = emptyList()
    private var currentRoutineExerciseId: Long = 0L
    private var currentExerciseId: Long = 0L           // ← NUEVO: ID real del ejercicio
    private var activeSequenceIndex = -1

    // ── Submit ────────────────────────────────────────────────────────────────
    // AHORA recibe también el exerciseId real
    fun submitList(newSets: List<RoutineSetTemplateResponse>, routineExerciseId: Long, exerciseId: Long) {
        this.currentRoutineExerciseId = routineExerciseId
        this.currentExerciseId = exerciseId
        sets = newSets
        activeSequenceIndex = -1
        notifyDataSetChanged()
    }

    fun updateRoutineExerciseId(routineExerciseId: Long) {
        this.currentRoutineExerciseId = routineExerciseId
    }

    fun isSequenceMode() = sets.isNotEmpty() && sets.all { set ->
        set.parameters?.any { it.parameterType?.uppercase() == "DURATION" && it.durationValue != null } == true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SetViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_workout_set_classic, parent, false)
        )

    override fun onBindViewHolder(h: SetViewHolder, pos: Int) = h.bind(sets[pos], pos)
    override fun getItemCount() = sets.size
    override fun onViewRecycled(h: SetViewHolder) { super.onViewRecycled(h); h.stopAllTimers() }
    override fun onViewDetachedFromWindow(h: SetViewHolder) { super.onViewDetachedFromWindow(h); h.stopAllTimers() }

    inner class SetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val checkboxCompleted: CheckBox = itemView.findViewById(R.id.checkbox_set_completed)
        private val viewTypeStripe: View = itemView.findViewById(R.id.view_type_stripe)
        private val tvSetBadge: TextView = itemView.findViewById(R.id.tv_set_badge)
        private val tvParamSummary: TextView = itemView.findViewById(R.id.tv_param_summary)
        private val layoutReps: LinearLayout = itemView.findViewById(R.id.layout_reps_container)
        private val tvRepsLabel: TextView = itemView.findViewById(R.id.tv_reps_label)
        private val tvRepsValue: TextView = itemView.findViewById(R.id.tv_reps_value)
        private val btnDecReps: ImageButton = itemView.findViewById(R.id.btn_decrease_reps)
        private val btnIncReps: ImageButton = itemView.findViewById(R.id.btn_increase_reps)
        private val viewDivider: View = itemView.findViewById(R.id.view_divider)
        private val layoutParam: LinearLayout = itemView.findViewById(R.id.layout_param_container)
        private val tvParamUnit: TextView = itemView.findViewById(R.id.tv_weight_unit)
        private val tvParamValue: TextView = itemView.findViewById(R.id.tv_weight_value)
        private val btnDecParam: ImageButton = itemView.findViewById(R.id.btn_decrease_weight)
        private val btnIncParam: ImageButton = itemView.findViewById(R.id.btn_increase_weight)
        private val layoutDurationExtra: View = itemView.findViewById(R.id.layout_duration_extra)
        private val tvDurationTimer: TextView = itemView.findViewById(R.id.tv_duration_timer)
        private val btnDecDurationExtra: ImageButton = itemView.findViewById(R.id.btn_decrease_duration_extra)
        private val btnIncDurationExtra: ImageButton = itemView.findViewById(R.id.btn_increase_duration_extra)
        private val layoutRestContainer: View = itemView.findViewById(R.id.layout_rest_container)
        private val tvRestTimer: TextView = itemView.findViewById(R.id.tv_rest_timer)
        private val tvRestHint: TextView = itemView.findViewById(R.id.tv_rest_hint)

        private var currentSetId: Long = -1L
        private var currentSet: RoutineSetTemplateResponse? = null
        private var myIndex = -1
        private var restSeconds = 0
        private var restTimerActive = false
        private var durationTimerActive = false
        private var durationDisplayView: TextView? = null

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
                    val dur = getDurationValue()
                    durationDisplayView?.text = formatDuration(dur)
                    WorkoutHaptics.setComplete(itemView.context)
                    Thread { WorkoutSoundManager.playSetComplete(itemView.context) }.start()
                    currentSet?.let { onValueChanged(it, "completed", 1.0) }
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
            restTimerActive = false
            durationTimerActive = false
            restTimer.stop()
            durationTimer.stop()
        }

        fun bind(set: RoutineSetTemplateResponse, position: Int) {
            stopAllTimers()
            durationDisplayView = null
            currentSetId = set.id
            currentSet = set
            myIndex = position

            // AHORA usamos currentExerciseId (el real) en lugar de una variable inexistente
            stateManager.initializeSet(set.id, currentRoutineExerciseId, currentExerciseId, set)

            bindCheckbox(set)
            bindTypeDecoration(set)
            bindControls(set, position)
            bindRestTimer(set)
        }

        // ... el resto de la clase (bindCheckbox, bindTypeDecoration, bindControls, etc.)
        // se mantiene EXACTAMENTE IGUAL que en tu código original.
        // Solo cambió la línea de initializeSet arriba.

        // (Aquí va todo el código que sigue, sin modificaciones)
        // Para no alargar, copia el resto de tu clase original desde aquí hasta el final.
        // Yo lo incluyo por completitud, pero puedes dejarlo igual.

        private fun bindCheckbox(set: RoutineSetTemplateResponse) {
            val isCompleted = completionState.isSetCompleted(set.id)
            checkboxCompleted.isChecked = isCompleted
            checkboxCompleted.setOnCheckedChangeListener(null)
            checkboxCompleted.setOnCheckedChangeListener { _, checked ->
                completionState.markSetCompleted(set.id, currentRoutineExerciseId, checked)
                onSetCompletedToggled(set, checked)
                updateCompletionVisuals(checked)
            }
            updateCompletionVisuals(isCompleted)
        }

        private fun updateCompletionVisuals(completed: Boolean) {
            itemView.findViewById<View>(R.id.card_set_root)?.alpha = if (completed) 0.5f else 1.0f
        }

        private fun bindTypeDecoration(set: RoutineSetTemplateResponse) {
            val (label, colorRes) = setTypeMeta(set.setType ?: "NORMAL")
            val color = ContextCompat.getColor(itemView.context, colorRes)
            viewTypeStripe.setBackgroundColor(color)
            tvSetBadge.text = label
            tvSetBadge.backgroundTintList = ContextCompat.getColorStateList(itemView.context, colorRes)
            tvSetBadge.setTextColor(
                if (colorRes == R.color.set_type_normal)
                    ContextCompat.getColor(itemView.context, R.color.background_dark)
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
            val hasReps     = getRepsValue() != null
            val hasDuration = getDurationValue() > 0L
            val hasParam    = getParamValue() != null

            tvParamSummary.text = buildSummary(hasReps, hasDuration, hasParam, set)

            layoutReps.visibility = View.GONE
            layoutParam.visibility = View.GONE
            viewDivider.visibility = View.GONE
            layoutDurationExtra.visibility = View.GONE
            resetTextDefaults()

            when {
                hasReps && hasParam && !hasDuration -> {
                    setupRepsColumn(set)
                    setupParamColumn(set)
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
                    setupRepsColumn(set)
                    setupParamColumn(set)
                    viewDivider.visibility = View.VISIBLE
                    setupDurationExtra(set, position)
                }
            }
        }

        private fun getRepsValue(): Int? {
            val params = stateManager.getParameterValues(currentSetId, getFirstParamId(isReps = true))
                ?: return currentSet?.parameters?.firstOrNull { it.repetitions != null }?.repetitions
            return params.repetitions
        }

        private fun getDurationValue(): Long {
            val durationParamId = currentSet?.parameters
                ?.firstOrNull { it.parameterType?.uppercase() == "DURATION" }
                ?.parameterId ?: return 0L
            return stateManager.getParameterValues(currentSetId, durationParamId)?.durationValue
                ?: currentSet?.parameters?.firstOrNull { it.parameterType?.uppercase() == "DURATION" }?.durationValue
                ?: 0L
        }

        private fun getParamValue(): Double? {
            val numericParam = currentSet?.parameters?.firstOrNull { p ->
                p.parameterType?.uppercase() in listOf("NUMBER", "INTEGER", "DISTANCE", "PERCENTAGE")
            } ?: return null
            val values = stateManager.getParameterValues(currentSetId, numericParam.parameterId)
            return values?.numericValue ?: values?.integerValue?.toDouble()
            ?: numericParam.numericValue ?: numericParam.integerValue?.toDouble()
        }

        private fun getParamUnit(): String {
            val p = currentSet?.parameters?.firstOrNull { it.parameterType?.uppercase() in
                    listOf("NUMBER", "INTEGER", "DISTANCE", "PERCENTAGE") }
                ?: return ""

            Log.d("PARAM_UNIT_DEBUG", "paramName=${p.parameterName}, serverUnit=${p.unit}, paramType=${p.parameterType}")

            return p.unit ?: when (p.parameterType?.uppercase()) {
                "DISTANCE"   -> "M"
                "PERCENTAGE" -> "%"
                "INTEGER"    -> "REP"
                else         -> "KG"
            }
        }

        private fun getParamType(): String {
            return currentSet?.parameters?.firstOrNull { it.parameterType?.uppercase() in
                    listOf("NUMBER", "INTEGER", "DISTANCE", "PERCENTAGE") }
                ?.parameterType?.lowercase() ?: "number"
        }

        private fun getFirstParamId(isReps: Boolean): Long {
            return currentSet?.parameters?.firstOrNull {
                if (isReps) it.repetitions != null
                else it.parameterType?.uppercase() in listOf("NUMBER", "INTEGER", "DISTANCE", "PERCENTAGE")
            }?.parameterId ?: -1L
        }

        private fun setupRepsColumn(set: RoutineSetTemplateResponse) {
            layoutReps.visibility = View.VISIBLE
            tvRepsLabel.text = when (set.setType?.uppercase()) {
                "ISOMETRIC", "REST_PAUSE" -> "SERIES"
                "DROP_SET" -> "REPS ↓"
                else -> "REPS"
            }
            val reps = getRepsValue() ?: 0
            tvRepsValue.text = reps.toString()

            btnDecReps.setOnClickListener {
                val cur = getRepsValue() ?: 0
                if (cur > 0) {
                    val n = cur - 1
                    stateManager.updateReps(currentSetId, n)
                    tvRepsValue.text = n.toString()
                    onValueChanged(set, "reps", n.toDouble())
                }
            }
            btnIncReps.setOnClickListener {
                val n = (getRepsValue() ?: 0) + 1
                stateManager.updateReps(currentSetId, n)
                tvRepsValue.text = n.toString()
                onValueChanged(set, "reps", n.toDouble())
            }
        }

        private fun setupParamColumn(set: RoutineSetTemplateResponse) {
            layoutParam.visibility = View.VISIBLE
            val value = getParamValue() ?: 0.0
            val unit  = getParamUnit()
            val type  = getParamType()
            val paramId = currentSet?.parameters?.firstOrNull { it.parameterType?.uppercase() in
                    listOf("NUMBER", "INTEGER", "DISTANCE", "PERCENTAGE") }?.parameterId ?: return

            tvParamUnit.text = unit
            tvParamValue.text = formatValue(value, type)

            val step = stepFor(unit, type)
            btnDecParam.setOnClickListener {
                val cur = getParamValue() ?: 0.0
                if (cur >= step) {
                    val n = cur - step
                    updateParamInStateManager(paramId, n, type)
                    tvParamValue.text = formatValue(n, type)
                    onValueChanged(set, "param", n)
                }
            }
            btnIncParam.setOnClickListener {
                val n = (getParamValue() ?: 0.0) + step
                updateParamInStateManager(paramId, n, type)
                tvParamValue.text = formatValue(n, type)
                onValueChanged(set, "param", n)
            }
        }

        private fun updateParamInStateManager(paramId: Long, value: Double, type: String) {
            if (type == "integer") stateManager.updateIntegerValue(currentSetId, paramId, value.toInt())
            else stateManager.updateNumericValue(currentSetId, paramId, value)
        }

        private fun setupDurationInColumn(
            set: RoutineSetTemplateResponse,
            position: Int,
            isLeft: Boolean
        ) {
            val layout  = if (isLeft) layoutReps  else layoutParam
            val tvLabel = if (isLeft) tvRepsLabel  else tvParamUnit
            val tvValue = if (isLeft) tvRepsValue  else tvParamValue
            val btnDec  = if (isLeft) btnDecReps   else btnDecParam
            val btnInc  = if (isLeft) btnIncReps   else btnIncParam

            layout.visibility = View.VISIBLE

            val durationParam = set.parameters?.firstOrNull { it.parameterType?.uppercase() == "DURATION" }
            val unitText = durationParam?.unit?.take(3)?.uppercase() ?: "SEG"
            tvLabel.text = unitText

            tvValue.text = formatDuration(getDurationValue())
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

            val durationParam = set.parameters?.firstOrNull { it.parameterType?.uppercase() == "DURATION" }
            val unitText = durationParam?.unit?.take(3)?.uppercase() ?: "SEG"

            tvDurationTimer.text = formatDuration(getDurationValue())
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
                durationTimer.start(getDurationValue().toInt())
            }
        }

        private fun toggleDuration(set: RoutineSetTemplateResponse, position: Int) {
            if (durationTimerActive) {
                durationTimerActive = false
                durationTimer.stop()
                durationDisplayView?.text = formatDuration(getDurationValue())
            } else {
                WorkoutHaptics.exerciseStart(itemView.context)
                durationTimerActive = true
                durationTimer.start(getDurationValue().toInt())
            }
        }

        private fun adjustDuration(set: RoutineSetTemplateResponse, delta: Long) {
            if (durationTimerActive) return
            val durationParam = set.parameters?.firstOrNull { it.parameterType?.uppercase() == "DURATION" } ?: return
            val newVal = (getDurationValue() + delta).coerceAtLeast(5L)
            stateManager.updateDurationValue(currentSetId, durationParam.parameterId, newVal)
            durationDisplayView?.text = formatDuration(newVal)
            onValueChanged(set, "duration", newVal.toDouble())
        }

        private fun bindRestTimer(set: RoutineSetTemplateResponse) {
            restSeconds = set.restAfterSet ?: 0
            if (restSeconds <= 0) {
                layoutRestContainer.visibility = View.GONE
                return
            }
            layoutRestContainer.visibility = View.VISIBLE
            updateRestLabel()
            layoutRestContainer.setOnClickListener {
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
            tvRestTimer.text = "${restSeconds}s descanso"
            tvRestHint.text = "TAP"
        }

        private fun onSetRestFinished(index: Int) {
            val next = index + 1
            if (next < sets.size) {
                activeSequenceIndex = next
                notifyItemChanged(next)
            } else {
                activeSequenceIndex = -1
                onSequenceComplete()
            }
        }

        private fun resetTextDefaults() {
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
        }

        private fun setColumnWeight(layout: LinearLayout, weight: Float) {
            val lp = layout.layoutParams as? LinearLayout.LayoutParams ?: return
            lp.weight = weight
            layout.layoutParams = lp
        }

        private fun buildSummary(
            hasReps: Boolean,
            hasDuration: Boolean,
            hasParam: Boolean,
            set: RoutineSetTemplateResponse
        ): String {
            val unit = getParamUnit()
            return listOfNotNull(
                if (hasReps) "REPS" else null,
                if (hasDuration) "TIEMPO" else null,
                if (hasParam) unit else null
            ).joinToString(" · ")
        }

        private fun inferUnit(type: String?, name: String?): String = when (type?.uppercase()) {
            "DISTANCE"   -> "M"
            "PERCENTAGE" -> "%"
            "INTEGER"    -> "REP"
            else         -> "KG"
        }

        private fun stepFor(unit: String, type: String): Double = when {
            type == "percentage" -> 5.0
            type == "distance"   -> 1.0
            unit.uppercase() == "KG" -> 2.5
            unit.uppercase() == "LB" -> 5.0
            else -> 1.0
        }

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