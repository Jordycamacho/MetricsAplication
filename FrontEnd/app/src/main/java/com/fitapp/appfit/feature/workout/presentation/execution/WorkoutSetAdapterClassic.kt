package com.fitapp.appfit.feature.workout.presentation.execution

import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
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
import com.fitapp.appfit.feature.workout.util.WorkoutParameterHelper
import com.fitapp.appfit.feature.workout.util.WorkoutRepeatButtonHelper
import com.fitapp.appfit.feature.workout.util.WorkoutSoundManager

class WorkoutSetAdapterClassic(
    private val onShowNumericInput: (
        RoutineSetParameterResponse,
        Double,
        (Double) -> Unit
    ) -> Unit,
    private val stateManager: SetParameterStateManager,
    private val onValueChanged: (RoutineSetTemplateResponse, String, Double) -> Unit,
    private val onSetCompletedToggled: (RoutineSetTemplateResponse, Boolean) -> Unit,
    private val onSequenceComplete: () -> Unit = {},
    private val completionState: WorkoutCompletionState
) : RecyclerView.Adapter<WorkoutSetAdapterClassic.SetViewHolder>() {

    private var sets: List<RoutineSetTemplateResponse> = emptyList()
    private var currentRoutineExerciseId: Long = 0L
    private var currentExerciseId: Long = 0L
    private var activeSequenceIndex = -1

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

        private val decRepsHelper = WorkoutRepeatButtonHelper()
        private val incRepsHelper = WorkoutRepeatButtonHelper()
        private val decParamHelper = WorkoutRepeatButtonHelper()
        private val incParamHelper = WorkoutRepeatButtonHelper()
        private val decDurationHelper = WorkoutRepeatButtonHelper()
        private val incDurationHelper = WorkoutRepeatButtonHelper()

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
            clearButtonHelpers()
        }

        private fun clearButtonHelpers() {
            decRepsHelper.clear()
            incRepsHelper.clear()
            decParamHelper.clear()
            incParamHelper.clear()
            decDurationHelper.clear()
            incDurationHelper.clear()
            btnDecReps.setOnTouchListener(null)
            btnIncReps.setOnTouchListener(null)
            btnDecParam.setOnTouchListener(null)
            btnIncParam.setOnTouchListener(null)
            btnDecDurationExtra.setOnTouchListener(null)
            btnIncDurationExtra.setOnTouchListener(null)
        }

        fun bind(set: RoutineSetTemplateResponse, position: Int) {
            stopAllTimers()
            durationDisplayView = null
            currentSetId = set.id
            currentSet = set
            myIndex = position

            stateManager.initializeSet(set.id, currentRoutineExerciseId, currentExerciseId, set)

            bindCheckbox(set)
            bindTypeDecoration(set)
            bindControls(set, position)
            bindRestTimer(set)
        }

        // ═══════════════════════════════════════════════════════════════════════
        // CORE: Identificar parámetros por NOMBRE, no por tipo de dato
        // ═══════════════════════════════════════════════════════════════════════

        /**
         * Obtiene el parámetro "Repeticiones" (identificado por nombre "Repeticiones")
         */
        private fun getRepsParameter(): RoutineSetParameterResponse? =
            WorkoutParameterHelper.findRepsParameter(currentSet?.parameters)

        private fun getNumericParameter(): RoutineSetParameterResponse? =
            WorkoutParameterHelper.findNumericParameter(currentSet?.parameters)

        /**
         * Obtiene el parámetro de duración
         */
        private fun getDurationParameter(): RoutineSetParameterResponse? {
            return currentSet?.parameters?.firstOrNull { param ->
                param.parameterType?.uppercase() == "DURATION" && param.durationValue != null
            }
        }

        // ═══════════════════════════════════════════════════════════════════════
        // VALORES
        // ═══════════════════════════════════════════════════════════════════════

        private fun getRepsValue(): Int? {
            val repsParam = getRepsParameter() ?: return null
            val values = stateManager.getParameterValues(currentSetId, repsParam.parameterId)

            return values?.integerValue
                ?: values?.repetitions
                ?: repsParam.integerValue
                ?: repsParam.repetitions
        }

        private fun getNumericValue(): Double? {
            val numericParam = getNumericParameter() ?: return null
            return WorkoutParameterHelper.readNumericValue(currentSetId, numericParam, stateManager)
        }

        private fun getDurationValue(): Long {
            val durationParam = getDurationParameter() ?: return 0L
            return stateManager.getParameterValues(currentSetId, durationParam.parameterId)?.durationValue
                ?: durationParam.durationValue ?: 0L
        }

        private fun getNumericUnit(): String {
            val param = getNumericParameter() ?: return "—"
            Log.d("PARAM_UNIT_DEBUG", "paramName=${param.parameterName}, serverUnit=${param.unit}, paramType=${param.parameterType}")
            return WorkoutParameterHelper.displayUnit(param)
        }

        // ═══════════════════════════════════════════════════════════════════════
        // BINDCONTROLS: Determina qué se muestra
        // ═══════════════════════════════════════════════════════════════════════

        private fun bindControls(set: RoutineSetTemplateResponse, position: Int) {
            val hasReps     = getRepsParameter() != null
            val hasDuration = getDurationValue() > 0L
            val hasNumeric  = getNumericParameter() != null

            Log.d("BINDCONTROLS", "set=${set.id} | hasReps=$hasReps | hasNumeric=$hasNumeric | hasDuration=$hasDuration")

            tvParamSummary.text = buildSummary(hasReps, hasDuration, hasNumeric)

            layoutReps.visibility = View.GONE
            layoutParam.visibility = View.GONE
            viewDivider.visibility = View.GONE
            layoutDurationExtra.visibility = View.GONE
            resetTextDefaults()

            when {
                // CASO 1: Reps + Peso (+ Duración opcional)
                hasReps && hasNumeric && !hasDuration -> {
                    setupRepsColumn(set)
                    setupNumericColumn(set)
                    viewDivider.visibility = View.VISIBLE
                    Log.d("BINDCONTROLS", "→ Mostrando REPS + NUMERIC")
                }

                // CASO 2: Solo Reps
                hasReps && !hasNumeric && !hasDuration -> {
                    setupRepsColumn(set)
                    setColumnWeight(layoutReps, 2f)
                    Log.d("BINDCONTROLS", "→ Mostrando REPS solo")
                }

                // CASO 3: Duración + Peso
                !hasReps && hasDuration && hasNumeric -> {
                    setupDurationInColumn(set, position, isLeft = true)
                    setupNumericColumn(set)
                    viewDivider.visibility = View.VISIBLE
                    Log.d("BINDCONTROLS", "→ Mostrando DURATION + NUMERIC")
                }

                // CASO 4: Solo Duración
                !hasReps && hasDuration && !hasNumeric -> {
                    setupDurationInColumn(set, position, isLeft = true)
                    setColumnWeight(layoutReps, 2f)
                    Log.d("BINDCONTROLS", "→ Mostrando DURATION solo")
                }

                // CASO 5: Reps + Duración (sin Peso)
                hasReps && hasDuration && !hasNumeric -> {
                    setupRepsColumn(set)
                    setupDurationInColumn(set, position, isLeft = false)
                    viewDivider.visibility = View.VISIBLE
                    Log.d("BINDCONTROLS", "→ Mostrando REPS + DURATION")
                }

                // CASO 6: Reps + Peso + Duración (3 parámetros)
                hasReps && hasNumeric && hasDuration -> {
                    setupRepsColumn(set)
                    setupNumericColumn(set)
                    viewDivider.visibility = View.VISIBLE
                    setupDurationExtra(set, position)
                    Log.d("BINDCONTROLS", "→ Mostrando REPS + NUMERIC + DURATION extra")
                }

                // CASO 7: Solo Peso
                !hasReps && !hasDuration && hasNumeric -> {
                    setupNumericColumn(set)
                    setColumnWeight(layoutParam, 2f)
                    Log.d("BINDCONTROLS", "→ Mostrando NUMERIC solo")
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════════════
        // SETUP COLUMNS
        // ═══════════════════════════════════════════════════════════════════════

        private fun setupRepsColumn(set: RoutineSetTemplateResponse) {
            layoutReps.visibility = View.VISIBLE
            tvRepsLabel.text = when (set.setType?.uppercase()) {
                "ISOMETRIC", "REST_PAUSE" -> "SERIES"
                "DROP_SET" -> "REPS ↓"
                else -> "REPS"
            }
            val reps = getRepsValue() ?: 0
            tvRepsValue.text = reps.toString()

            val repsParam = getRepsParameter() ?: return

            decRepsHelper.attach(btnDecReps, onStep = {
                val cur = getRepsValue() ?: 0
                if (cur > 0) {
                    val n = cur - 1
                    stateManager.updateIntegerValue(currentSetId, repsParam.parameterId, n)
                    tvRepsValue.text = n.toString()
                    onValueChanged(set, "reps", n.toDouble())
                }
            })
            incRepsHelper.attach(btnIncReps, onStep = {
                val n = (getRepsValue() ?: 0) + 1
                stateManager.updateIntegerValue(currentSetId, repsParam.parameterId, n)
                tvRepsValue.text = n.toString()
                onValueChanged(set, "reps", n.toDouble())
            })

            attachManualInputLongPress(tvRepsValue, repsParam) {
                showManualNumericInput(set, repsParam)
            }
        }

        private fun setupNumericColumn(set: RoutineSetTemplateResponse) {
            layoutParam.visibility = View.VISIBLE
            val numericParam = getNumericParameter() ?: return
            val value = getNumericValue() ?: 0.0
            val unit = getNumericUnit()

            tvParamUnit.text = unit
            tvParamValue.text = WorkoutParameterHelper.formatNumericValue(value, numericParam)

            decParamHelper.attach(btnDecParam, onStep = {
                val cur = getNumericValue() ?: 0.0
                if (cur > 0.0) {
                    val n = WorkoutParameterHelper.adjustNumericByButton(cur, -1)
                    applyNumericValue(set, numericParam, n)
                }
            })
            incParamHelper.attach(btnIncParam, onStep = {
                val cur = getNumericValue() ?: 0.0
                val n = WorkoutParameterHelper.adjustNumericByButton(cur, 1)
                applyNumericValue(set, numericParam, n)
            })

            attachManualInputLongPress(tvParamValue, numericParam) {
                showManualNumericInput(set, numericParam)
            }
        }

        private fun applyNumericValue(
            set: RoutineSetTemplateResponse,
            numericParam: RoutineSetParameterResponse,
            value: Double
        ) {
            updateNumericInStateManager(numericParam, value)
            tvParamValue.text = WorkoutParameterHelper.formatNumericValue(value, numericParam)
            onValueChanged(set, "param", value)
        }

        private fun showManualNumericInput(
            set: RoutineSetTemplateResponse,
            param: RoutineSetParameterResponse
        ) {
            val current = if (WorkoutParameterHelper.isRepsParameter(param)) {
                (getRepsValue() ?: 0).toDouble()
            } else {
                getNumericValue() ?: 0.0
            }
            onShowNumericInput(param, current) { newValue ->
                applyParameterValue(set, param, newValue)
            }
        }

        private fun applyParameterValue(
            set: RoutineSetTemplateResponse,
            param: RoutineSetParameterResponse,
            value: Double
        ) {
            if (WorkoutParameterHelper.isRepsParameter(param)) {
                val intVal = value.toInt()
                stateManager.updateIntegerValue(currentSetId, param.parameterId, intVal)
                tvRepsValue.text = intVal.toString()
                onValueChanged(set, "reps", value)
            } else {
                applyNumericValue(set, param, value)
            }
        }

        private fun attachManualInputLongPress(
            valueView: TextView,
            param: RoutineSetParameterResponse,
            onShow: () -> Unit
        ) {
            if (!WorkoutParameterHelper.supportsManualInput(param)) return

            valueView.isClickable = true
            valueView.isLongClickable = true
            valueView.setOnTouchListener { v, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN ->
                        v.parent?.requestDisallowInterceptTouchEvent(true)
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                        v.parent?.requestDisallowInterceptTouchEvent(false)
                }
                false
            }
            valueView.setOnLongClickListener {
                WorkoutHaptics.exerciseStart(itemView.context)
                onShow()
                true
            }
        }

        private fun updateNumericInStateManager(param: RoutineSetParameterResponse, value: Double) {
            if (WorkoutParameterHelper.isIntegerInput(param)) {
                stateManager.updateIntegerValue(currentSetId, param.parameterId, value.toInt())
            } else {
                stateManager.updateNumericValue(currentSetId, param.parameterId, value)
            }
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

            val durationParam = getDurationParameter()
            val unitText = durationParam?.unit?.take(3)?.uppercase() ?: "SEG"
            tvLabel.text = unitText

            tvValue.text = formatDuration(getDurationValue())
            tvValue.setTextColor(ContextCompat.getColor(itemView.context, R.color.gold_primary))
            tvValue.textSize = 32f
            tvValue.isClickable = true
            tvValue.isFocusable = true
            durationDisplayView = tvValue

            tvValue.setOnClickListener { toggleDuration(set, position) }
            decDurationHelper.attach(btnDec, onStep = { adjustDuration(set, -5L) })
            incDurationHelper.attach(btnInc, onStep = { adjustDuration(set, 5L) })

            autoStartIfSequence(set, position)
        }

        private fun setupDurationExtra(set: RoutineSetTemplateResponse, position: Int) {
            layoutDurationExtra.visibility = View.VISIBLE

            val durationParam = getDurationParameter()
            val unitText = durationParam?.unit?.take(3)?.uppercase() ?: "SEG"

            tvDurationTimer.text = formatDuration(getDurationValue())
            durationDisplayView = tvDurationTimer

            tvDurationTimer.setOnClickListener { toggleDuration(set, position) }
            decDurationHelper.attach(btnDecDurationExtra, onStep = { adjustDuration(set, -5L) })
            incDurationHelper.attach(btnIncDurationExtra, onStep = { adjustDuration(set, 5L) })

            autoStartIfSequence(set, position)
        }

        // ═══════════════════════════════════════════════════════════════════════
        // HELPERS
        // ═══════════════════════════════════════════════════════════════════════

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
            val durationParam = getDurationParameter() ?: return
            val newVal = (getDurationValue() + delta).coerceAtLeast(5L)
            stateManager.updateDurationValue(currentSetId, durationParam.parameterId, newVal)
            durationDisplayView?.text = formatDuration(newVal)
            onValueChanged(set, "duration", newVal.toDouble())
        }

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
            tvRepsValue.isLongClickable = false
            tvRepsValue.isFocusable = false
            tvRepsValue.setOnClickListener(null)
            tvRepsValue.setOnLongClickListener(null)
            tvParamValue.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_primary_dark))
            tvParamValue.textSize = 38f
            tvParamValue.isClickable = false
            tvParamValue.isLongClickable = false
            tvParamValue.isFocusable = false
            tvParamValue.setOnClickListener(null)
            tvParamValue.setOnLongClickListener(null)
        }

        private fun setColumnWeight(layout: LinearLayout, weight: Float) {
            val lp = layout.layoutParams as? LinearLayout.LayoutParams ?: return
            lp.weight = weight
            layout.layoutParams = lp
        }

        private fun buildSummary(
            hasReps: Boolean,
            hasDuration: Boolean,
            hasNumeric: Boolean
        ): String {
            val unit = getNumericUnit()
            return listOfNotNull(
                if (hasReps) "REPS" else null,
                if (hasDuration) "TIEMPO" else null,
                if (hasNumeric) unit else null
            ).joinToString(" · ")
        }

        private fun formatDuration(seconds: Long): String {
            val m = seconds / 60
            val s = seconds % 60
            return if (m > 0) "${m}m ${s.toString().padStart(2, '0')}s" else "${s}s"
        }
    }
}