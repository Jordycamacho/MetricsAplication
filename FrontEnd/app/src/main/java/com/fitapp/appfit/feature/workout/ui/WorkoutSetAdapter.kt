package com.fitapp.appfit.feature.workout.ui

import android.graphics.Color
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse
import com.fitapp.appfit.feature.routine.model.setparameter.response.RoutineSetParameterResponse
import com.fitapp.appfit.core.util.RestTimer
import com.fitapp.appfit.feature.workout.util.WorkoutHaptics
import com.fitapp.appfit.feature.workout.util.WorkoutSoundManager
import com.google.android.material.button.MaterialButton

class WorkoutSetAdapter(
    private val onValueChanged: (RoutineSetTemplateResponse, String, Double) -> Unit,
    private val onSetCompletedToggled: (RoutineSetTemplateResponse, Boolean) -> Unit,
    private val onSequenceComplete: () -> Unit = {}
) : RecyclerView.Adapter<WorkoutSetAdapter.SetViewHolder>() {

    private var sets: List<RoutineSetTemplateResponse> = emptyList()
    private val currentReps     = mutableMapOf<Long, Int>()
    private val currentParam    = mutableMapOf<Long, Double>()
    private val currentDuration = mutableMapOf<Long, Long>()
    private val paramLabel      = mutableMapOf<Long, String>()
    private val paramType       = mutableMapOf<Long, String>()
    private var activeSequenceIndex = -1

    companion object {
        private const val TAG = "WorkoutSetAdapter"
    }

    fun submitList(newSets: List<RoutineSetTemplateResponse>) {
        sets = newSets
        currentReps.clear(); currentParam.clear()
        currentDuration.clear(); paramLabel.clear(); paramType.clear()
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
            p.parameterType?.uppercase() in listOf("NUMBER", "INTEGER", "DISTANCE", "PERCENTAGE")
                    && (p.numericValue != null || p.integerValue != null)
        }
        if (numericParam != null) {
            currentParam[setId] = numericParam.numericValue ?: numericParam.integerValue?.toDouble() ?: 0.0
            paramLabel[setId]   = numericParam.unit ?: inferUnit(numericParam.parameterType, numericParam.parameterName)
            paramType[setId]    = numericParam.parameterType?.lowercase() ?: "number"
        } else {
            currentParam[setId] = 0.0
            paramLabel[setId]   = "KG"
            paramType[setId]    = "number"
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
        SetViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_set, parent, false))

    override fun onBindViewHolder(h: SetViewHolder, pos: Int) = h.bind(sets[pos], pos)
    override fun getItemCount() = sets.size
    override fun onViewRecycled(h: SetViewHolder) { super.onViewRecycled(h); h.stopAllTimers() }
    override fun onViewDetachedFromWindow(h: SetViewHolder) { super.onViewDetachedFromWindow(h); h.stopAllTimers() }


    inner class SetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val viewTypeStripe: View      = itemView.findViewById(R.id.view_type_stripe)
        private val tvSetBadge: TextView      = itemView.findViewById(R.id.tv_set_badge)
        private val tvParamSummary: TextView  = itemView.findViewById(R.id.tv_param_summary)

        private val layoutReps: LinearLayout  = itemView.findViewById(R.id.layout_reps_container)
        private val tvRepsLabel: TextView     = itemView.findViewById(R.id.tv_reps_label)
        private val pickerReps: RecyclerView  = itemView.findViewById(R.id.picker_reps)

        private val viewDivider: View         = itemView.findViewById(R.id.view_divider)

        private val layoutParam: LinearLayout = itemView.findViewById(R.id.layout_param_container)
        private val tvParamUnit: TextView     = itemView.findViewById(R.id.tv_weight_unit)
        private val pickerParam: RecyclerView = itemView.findViewById(R.id.picker_param)

        private val layoutDurationExtra: View           = itemView.findViewById(R.id.layout_duration_extra)
        private val pickerDurationMinutes: RecyclerView = itemView.findViewById(R.id.picker_duration_minutes)
        private val pickerDurationSeconds: RecyclerView = itemView.findViewById(R.id.picker_duration_seconds)

        private val layoutRestContainer: View = itemView.findViewById(R.id.layout_rest_container)
        private val tvRestTimer: TextView     = itemView.findViewById(R.id.tv_rest_timer)
        private val tvRestHint: TextView      = itemView.findViewById(R.id.tv_rest_hint)

        // ⭐ Referencias a los frames del contador
        private val frameDurationCounter: View = itemView.findViewById(R.id.frame_duration_counter)
        private val tvDurationCounter: TextView = itemView.findViewById(R.id.tv_duration_counter)
        private val frameDurationCounterMinutes: View = itemView.findViewById(R.id.frame_duration_counter_minutes)
        private val tvDurationCounterMinutes: TextView = itemView.findViewById(R.id.tv_duration_counter_minutes)
        private val frameDurationCounterSeconds: View = itemView.findViewById(R.id.frame_duration_counter_seconds)
        private val tvDurationCounterSeconds: TextView = itemView.findViewById(R.id.tv_duration_counter_seconds)
        private val frameRepsPicker: View = itemView.findViewById(R.id.frame_reps_picker)
        private val frameParamPicker: View = itemView.findViewById(R.id.frame_param_picker)

        private var currentSetId: Long = -1L
        private var myIndex = -1
        private var restSeconds = 0
        private var restTimerActive = false

        private val snapReps  = LinearSnapHelper()
        private val snapParam = LinearSnapHelper()
        private val snapDurationMin = LinearSnapHelper()
        private val snapDurationSec = LinearSnapHelper()

        private var countDownTimer: CountDownTimer? = null
        private var durationCountdownTimer: CountDownTimer? = null
        private var durationCountdownActive = false
        private var durationDisplay: TextView? = null

        private var isScrolling = false

        fun stopAllTimers() {
            restTimerActive = false
            countDownTimer?.cancel()
            countDownTimer = null
            durationCountdownTimer?.cancel()
            durationCountdownTimer = null
            durationCountdownActive = false
        }

        fun bind(set: RoutineSetTemplateResponse, position: Int) {
            stopAllTimers()
            currentSetId = set.id
            myIndex = position
            isScrolling = false
            bindTypeDecoration(set)
            bindControls(set, position)
            bindRestTimer(set)
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
            "WARM_UP"         -> "Calentamiento" to R.color.set_type_warmup
            "DROP_SET"        -> "Drop Set"       to R.color.set_type_drop
            "SUPER_SET"       -> "Super Set"      to R.color.set_type_super
            "GIANT_SET"       -> "Giant Set"      to R.color.set_type_giant
            "PYRAMID"         -> "Pirámide"       to R.color.set_type_pyramid
            "REVERSE_PYRAMID" -> "Pir. Inversa"   to R.color.set_type_pyramid
            "CLUSTER"         -> "Cluster"        to R.color.set_type_cluster
            "REST_PAUSE"      -> "Rest-Pause"     to R.color.set_type_rest_pause
            "ECCENTRIC"       -> "Excéntrico"     to R.color.set_type_eccentric
            "ISOMETRIC"       -> "Isométrico"     to R.color.set_type_isometric
            else              -> "Normal"         to R.color.set_type_normal
        }

        private fun bindControls(set: RoutineSetTemplateResponse, position: Int) {
            val hasReps     = currentReps.containsKey(set.id)
            val hasDuration = currentDuration.containsKey(set.id)
            val hasParam    = set.parameters?.any {
                it.parameterType?.uppercase() in listOf("NUMBER", "INTEGER", "DISTANCE", "PERCENTAGE")
            } == true

            tvParamSummary.text = buildSummary(hasReps, hasDuration, hasParam, set.id)

            layoutReps.visibility          = View.GONE
            layoutParam.visibility         = View.GONE
            viewDivider.visibility         = View.GONE
            layoutDurationExtra.visibility = View.GONE

            setColumnWeight(layoutReps, 1f)
            setColumnWeight(layoutParam, 1f)

            pickerReps.visibility  = View.VISIBLE
            pickerParam.visibility = View.VISIBLE

            when {
                hasReps && hasParam && !hasDuration -> {
                    setupRepsPicker(set)
                    setupParamPicker(set)
                    viewDivider.visibility = View.VISIBLE
                }
                hasReps && !hasParam && !hasDuration -> {
                    setupRepsPicker(set)
                    setColumnWeight(layoutReps, 2f)
                }
                !hasReps && hasDuration && hasParam -> {
                    setupDurationInline(set, position, isLeft = true)
                    setupParamPicker(set)
                    viewDivider.visibility = View.VISIBLE
                }
                !hasReps && hasDuration && !hasParam -> {
                    setupDurationInline(set, position, isLeft = true)
                    setColumnWeight(layoutReps, 2f)
                }
                hasReps && hasDuration && !hasParam -> {
                    setupRepsPicker(set)
                    setupDurationInline(set, position, isLeft = false)
                    viewDivider.visibility = View.VISIBLE
                }
                hasReps && hasParam && hasDuration -> {
                    setupRepsPicker(set)
                    setupParamPicker(set)
                    viewDivider.visibility = View.VISIBLE
                    setupDurationExtra(set, position)
                }
            }
        }

        private fun setupRepsPicker(set: RoutineSetTemplateResponse) {
            layoutReps.visibility  = View.VISIBLE
            pickerReps.visibility  = View.VISIBLE
            tvRepsLabel.text = when (set.setType?.uppercase()) {
                "ISOMETRIC", "REST_PAUSE" -> "SERIES"
                "DROP_SET" -> "REPS ↓"
                else -> "REPS"
            }

            val initialValue = currentReps[set.id] ?: 0
            val values = (0..99).toList()

            val startPos = initialValue.coerceIn(0, values.lastIndex)
            val pickerAdapter = PickerAdapter(
                values      = values.map { it.toString() },
                initialPos  = startPos,
                colorGold   = ContextCompat.getColor(itemView.context, R.color.gold_primary),
                colorDim    = ContextCompat.getColor(itemView.context, R.color.text_secondary_dark)
            )

            pickerReps.adapter = pickerAdapter
            val llm = LinearLayoutManager(itemView.context)
            pickerReps.layoutManager = llm

            fixPickerScroll(pickerReps)

            snapReps.attachToRecyclerView(null)
            snapReps.attachToRecyclerView(pickerReps)

            pickerReps.post {
                val offset = (pickerReps.height / 2) - (20 * itemView.context.resources.displayMetrics.density).toInt()
                llm.scrollToPositionWithOffset(startPos, offset)
            }

            pickerReps.clearOnScrollListeners()
            pickerReps.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val snapView = snapReps.findSnapView(llm) ?: return
                        val pos = llm.getPosition(snapView).coerceIn(0, values.lastIndex)
                        val newVal = values[pos]
                        if (currentReps[set.id] != newVal) {
                            currentReps[set.id] = newVal
                            pickerAdapter.setSelected(pos)
                            onValueChanged(set, "reps", newVal.toDouble())
                        }
                    }
                }
            })

            // ⭐ Ocultar botón PLAY en reps
            itemView.findViewById<View>(R.id.layout_duration_play)?.visibility = View.GONE
        }

        private fun fixPickerScroll(picker: RecyclerView) {
            picker.apply {
                isNestedScrollingEnabled = false

                setOnTouchListener { v, event ->
                    v.parent.requestDisallowInterceptTouchEvent(true)

                    if (event.action == android.view.MotionEvent.ACTION_UP ||
                        event.action == android.view.MotionEvent.ACTION_CANCEL) {
                        v.parent.requestDisallowInterceptTouchEvent(false)
                    }
                    false
                }

                overScrollMode = View.OVER_SCROLL_NEVER
            }
        }

        private fun setupParamPicker(set: RoutineSetTemplateResponse) {
            layoutParam.visibility = View.VISIBLE
            pickerParam.visibility = View.VISIBLE
            val unit  = paramLabel[set.id] ?: "KG"
            val type  = paramType[set.id]  ?: "number"
            tvParamUnit.text = unit

            val step   = stepFor(unit, type)
            val values = generateParamValues(type, step)
            val initialValue = currentParam[set.id] ?: 0.0
            val startPos = values.indexOfFirst { it >= initialValue }.coerceAtLeast(0)

            val pickerAdapter = PickerAdapter(
                values      = values.map { formatValue(it, type) },
                initialPos  = startPos,
                colorGold   = ContextCompat.getColor(itemView.context, R.color.gold_primary),
                colorDim    = ContextCompat.getColor(itemView.context, R.color.text_secondary_dark)
            )

            pickerParam.adapter = pickerAdapter
            val llm = LinearLayoutManager(itemView.context)
            pickerParam.layoutManager = llm

            fixPickerScroll(pickerParam)

            snapParam.attachToRecyclerView(null)
            snapParam.attachToRecyclerView(pickerParam)

            pickerParam.post {
                val offset = (pickerParam.height / 2) - (20 * itemView.context.resources.displayMetrics.density).toInt()
                llm.scrollToPositionWithOffset(startPos, offset)
            }

            pickerParam.clearOnScrollListeners()
            pickerParam.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val snapView = snapParam.findSnapView(llm) ?: return
                        val pos = llm.getPosition(snapView).coerceIn(0, values.lastIndex)
                        val newVal = values[pos]
                        if (currentParam[set.id] != newVal) {
                            currentParam[set.id] = newVal
                            pickerAdapter.setSelected(pos)
                            onValueChanged(set, "param", newVal)
                        }
                    }
                }
            })

            // ⭐ Ocultar botón PLAY en param
            itemView.findViewById<View>(R.id.layout_duration_play)?.visibility = View.GONE
        }

        private fun setupDurationInline(
            set: RoutineSetTemplateResponse,
            position: Int,
            isLeft: Boolean
        ) {
            Log.d(TAG, "setupDurationInline: isLeft=$isLeft, setId=${set.id}")

            val layout  = if (isLeft) layoutReps  else layoutParam
            val tvLabel = if (isLeft) tvRepsLabel  else tvParamUnit
            val picker  = if (isLeft) pickerReps   else pickerParam
            val snap    = if (isLeft) snapReps     else snapParam
            val btnStart = itemView.findViewById<View>(R.id.layout_duration_play)

            layout.visibility = View.VISIBLE
            tvLabel.text      = "TIEMPO (SEG)"
            picker.visibility = View.VISIBLE

            val totalSeconds = currentDuration[set.id] ?: 0L
            Log.d(TAG, "setupDurationInline: totalSeconds=$totalSeconds")

            val values = (5..600 step 5).toList()
            val startPos = values.indexOfFirst { it >= totalSeconds.toInt() }.coerceAtLeast(0)

            val pickerAdapter = PickerAdapter(
                values      = values.map { formatDuration(it.toLong()) },
                initialPos  = startPos,
                colorGold   = ContextCompat.getColor(itemView.context, R.color.gold_primary),
                colorDim    = ContextCompat.getColor(itemView.context, R.color.text_secondary_dark)
            )

            picker.adapter = pickerAdapter
            val llm = LinearLayoutManager(itemView.context)
            picker.layoutManager = llm
            fixPickerScroll(picker)

            snap.attachToRecyclerView(null)
            snap.attachToRecyclerView(picker)

            picker.post {
                val offset = (picker.height / 2) - (20 * itemView.context.resources.displayMetrics.density).toInt()
                llm.scrollToPositionWithOffset(startPos, offset)
            }

            picker.clearOnScrollListeners()
            picker.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                    Log.d(TAG, "setupDurationInline: onScrollStateChanged newState=$newState")
                    when (newState) {
                        RecyclerView.SCROLL_STATE_DRAGGING -> {
                            Log.d(TAG, "setupDurationInline: SCROLL_STATE_DRAGGING")
                            isScrolling = true
                        }
                        RecyclerView.SCROLL_STATE_SETTLING -> {
                            Log.d(TAG, "setupDurationInline: SCROLL_STATE_SETTLING")
                            isScrolling = true
                        }
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            Log.d(TAG, "setupDurationInline: SCROLL_STATE_IDLE")
                            isScrolling = false
                            val snapView = snap.findSnapView(llm) ?: return
                            val pos = llm.getPosition(snapView).coerceIn(0, values.lastIndex)
                            val newVal = values[pos].toLong()
                            Log.d(TAG, "setupDurationInline: newVal=$newVal, currentDuration=${currentDuration[set.id]}")
                            if (currentDuration[set.id] != newVal) {
                                currentDuration[set.id] = newVal
                                pickerAdapter.setSelected(pos)
                                onValueChanged(set, "duration", newVal.toDouble())
                                Log.d(TAG, "setupDurationInline: Updated duration to $newVal")
                            }
                        }
                    }
                }
            })

            // ⭐ Botón PLAY para iniciar countdown (SIN INTERFERENCIA CON SCROLL)
            btnStart.setOnClickListener {
                Log.d(TAG, "setupDurationInline: PLAY button clicked")
                if (isSequenceMode() && !durationCountdownActive) {
                    Log.d(TAG, "setupDurationInline: Starting countdown")
                    val selectedDuration = currentDuration[set.id] ?: 0L
                    startDurationCountdown(selectedDuration, set.id, tvLabel)
                }
            }
        }

        private fun startDurationCountdown(durationSeconds: Long, setId: Long, displayView: TextView) {
            Log.d(TAG, "startDurationCountdown: Starting with $durationSeconds seconds")

            if (durationCountdownActive) {
                Log.d(TAG, "startDurationCountdown: Already active, returning")
                return
            }

            durationCountdownTimer?.cancel()
            durationCountdownTimer = null
            durationCountdownActive = true
            durationDisplay = displayView

            // ⭐ Ocultar picker, mostrar contador
            when (displayView.id) {
                R.id.tv_reps_label -> {
                    frameRepsPicker.visibility = View.GONE
                    frameDurationCounter.visibility = View.VISIBLE
                }
                R.id.tv_weight_unit -> {
                    frameParamPicker.visibility = View.GONE
                }
            }

            durationCountdownTimer = object : CountDownTimer(durationSeconds * 1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val secondsLeft = (millisUntilFinished / 1000).toInt()
                    Log.d(TAG, "onTick: $secondsLeft seconds left")

                    if (!durationCountdownActive || !itemView.isAttachedToWindow) {
                        Log.d(TAG, "onTick: Cancelling (not active or not attached)")
                        cancel()
                        return
                    }

                    val formattedTime = formatDuration(secondsLeft.toLong())
                    when (displayView.id) {
                        R.id.tv_reps_label -> tvDurationCounter.text = formattedTime
                        R.id.tv_weight_unit -> {
                            tvDurationCounterMinutes.text = formattedTime
                            tvDurationCounterSeconds.text = formattedTime
                        }
                    }
                }

                override fun onFinish() {
                    Log.d(TAG, "onFinish: Countdown finished")
                    if (durationCountdownActive && itemView.isAttachedToWindow) {
                        durationCountdownActive = false
                        durationCountdownTimer = null

                        // ⭐ Mostrar picker nuevamente, ocultar contador
                        when (displayView.id) {
                            R.id.tv_reps_label -> {
                                frameRepsPicker.visibility = View.VISIBLE
                                frameDurationCounter.visibility = View.GONE
                            }
                            R.id.tv_weight_unit -> {
                                frameParamPicker.visibility = View.VISIBLE
                                itemView.findViewById<View>(R.id.frame_minutes_picker).visibility = View.VISIBLE
                                itemView.findViewById<View>(R.id.frame_seconds_picker).visibility = View.VISIBLE
                                frameDurationCounterMinutes.visibility = View.GONE
                                frameDurationCounterSeconds.visibility = View.GONE
                            }
                        }

                        Log.d(TAG, "onFinish: Playing haptic and sound")
                        WorkoutHaptics.setComplete(itemView.context)
                        Thread { WorkoutSoundManager.playSetComplete(itemView.context) }.start()

                        // ⭐ NUEVO: Secuencia automática
                        if (isSequenceMode()) {
                            if (restSeconds > 0) {
                                // Hay descanso → iniciar descanso automáticamente
                                Log.d(TAG, "onFinish: Starting rest timer automatically")
                                restTimerActive = true
                                tvRestTimer.text = "${restSeconds}s descanso"
                                tvRestHint.text = "STOP"
                                countDownTimer?.cancel()
                                countDownTimer = object : CountDownTimer(restSeconds * 1000L, 1000) {
                                    override fun onTick(millisUntilFinished: Long) {
                                        if (restTimerActive && itemView.isAttachedToWindow) {
                                            val secondsLeft = (millisUntilFinished / 1000).toInt()
                                            tvRestTimer.text = "${secondsLeft}s descanso"
                                            tvRestHint.text = "STOP"
                                        }
                                    }

                                    override fun onFinish() {
                                        if (restTimerActive && itemView.isAttachedToWindow) {
                                            restTimerActive = false
                                            countDownTimer = null
                                            WorkoutHaptics.restFinished(itemView.context)
                                            Thread { WorkoutSoundManager.playRestFinished(itemView.context) }.start()
                                            updateRestLabel()
                                            // Avanzar al siguiente set
                                            Log.d(TAG, "onFinish: Moving to next set after rest")
                                            onSetRestFinished(myIndex)
                                        }
                                    }
                                }.start()
                            } else {
                                // Sin descanso → ir directo al siguiente
                                Log.d(TAG, "onFinish: No rest, moving to next set")
                                onSetRestFinished(myIndex)
                            }
                        }
                    }
                }
            }.start()

            Log.d(TAG, "startDurationCountdown: Timer started")
        }

        private fun setupDurationExtra(set: RoutineSetTemplateResponse, position: Int) {
            Log.d(TAG, "setupDurationExtra: setId=${set.id}")
            layoutDurationExtra.visibility = View.VISIBLE

            val totalSeconds = currentDuration[set.id] ?: 0L
            val initialMinutes = (totalSeconds / 60).toInt()
            val initialSeconds = (totalSeconds % 60).toInt()

            val minuteValues = (0..59).toList()
            val minuteStartPos = initialMinutes.coerceIn(0, minuteValues.lastIndex)
            val minuteAdapter = PickerAdapter(
                values      = minuteValues.map { it.toString() },
                initialPos  = minuteStartPos,
                colorGold   = ContextCompat.getColor(itemView.context, R.color.gold_primary),
                colorDim    = ContextCompat.getColor(itemView.context, R.color.text_secondary_dark)
            )

            pickerDurationMinutes.adapter = minuteAdapter
            val llmMin = LinearLayoutManager(itemView.context)
            pickerDurationMinutes.layoutManager = llmMin
            fixPickerScroll(pickerDurationMinutes)

            snapDurationMin.attachToRecyclerView(null)
            snapDurationMin.attachToRecyclerView(pickerDurationMinutes)

            pickerDurationMinutes.post {
                val offset = (pickerDurationMinutes.height / 2) - (20 * itemView.context.resources.displayMetrics.density).toInt()
                llmMin.scrollToPositionWithOffset(minuteStartPos, offset)
            }

            val secondValues = (0..59).toList()
            val secondStartPos = initialSeconds.coerceIn(0, secondValues.lastIndex)
            val secondAdapter = PickerAdapter(
                values      = secondValues.map { it.toString().padStart(2, '0') },
                initialPos  = secondStartPos,
                colorGold   = ContextCompat.getColor(itemView.context, R.color.gold_primary),
                colorDim    = ContextCompat.getColor(itemView.context, R.color.text_secondary_dark)
            )

            pickerDurationSeconds.adapter = secondAdapter
            val llmSec = LinearLayoutManager(itemView.context)
            pickerDurationSeconds.layoutManager = llmSec
            fixPickerScroll(pickerDurationSeconds)

            snapDurationSec.attachToRecyclerView(null)
            snapDurationSec.attachToRecyclerView(pickerDurationSeconds)

            pickerDurationSeconds.post {
                val offset = (pickerDurationSeconds.height / 2) - (20 * itemView.context.resources.displayMetrics.density).toInt()
                llmSec.scrollToPositionWithOffset(secondStartPos, offset)
            }

            pickerDurationMinutes.clearOnScrollListeners()
            pickerDurationMinutes.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                    Log.d(TAG, "setupDurationExtra MINUTES: onScrollStateChanged newState=$newState")
                    when (newState) {
                        RecyclerView.SCROLL_STATE_DRAGGING -> isScrolling = true
                        RecyclerView.SCROLL_STATE_SETTLING -> isScrolling = true
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            isScrolling = false
                            val snapView = snapDurationMin.findSnapView(llmMin) ?: return
                            val pos = llmMin.getPosition(snapView).coerceIn(0, minuteValues.lastIndex)
                            val minutes = minuteValues[pos]

                            val secSnapView = snapDurationSec.findSnapView(llmSec)
                            val secPos = if (secSnapView != null) llmSec.getPosition(secSnapView).coerceIn(0, secondValues.lastIndex) else initialSeconds
                            val seconds = secondValues[secPos]

                            val newTotal = (minutes * 60L + seconds).coerceAtLeast(5L)
                            if (currentDuration[set.id] != newTotal) {
                                currentDuration[set.id] = newTotal
                                minuteAdapter.setSelected(pos)
                                onValueChanged(set, "duration", newTotal.toDouble())
                                Log.d(TAG, "setupDurationExtra MINUTES: Updated to $newTotal")
                            }
                        }
                    }
                }
            })

            // ⭐ Botón PLAY para MINUTOS
            val btnStartMin = itemView.findViewById<View>(R.id.layout_duration_play_minutes)
            btnStartMin.setOnClickListener {
                Log.d(TAG, "setupDurationExtra MINUTES: PLAY button clicked")
                if (isSequenceMode() && !durationCountdownActive) {
                    val selectedDuration = currentDuration[set.id] ?: 0L
                    Log.d(TAG, "setupDurationExtra MINUTES: Starting countdown with $selectedDuration")
                    // Ocultar pickers
                    itemView.findViewById<View>(R.id.frame_minutes_picker).visibility = View.GONE
                    itemView.findViewById<View>(R.id.frame_seconds_picker).visibility = View.GONE
                    // Mostrar contadores
                    frameDurationCounterMinutes.visibility = View.VISIBLE
                    frameDurationCounterSeconds.visibility = View.VISIBLE
                    startDurationCountdown(selectedDuration, set.id, tvRepsLabel)
                }
            }

            pickerDurationSeconds.clearOnScrollListeners()
            pickerDurationSeconds.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                    Log.d(TAG, "setupDurationExtra SECONDS: onScrollStateChanged newState=$newState")
                    when (newState) {
                        RecyclerView.SCROLL_STATE_DRAGGING -> isScrolling = true
                        RecyclerView.SCROLL_STATE_SETTLING -> isScrolling = true
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            isScrolling = false
                            val snapView = snapDurationSec.findSnapView(llmSec) ?: return
                            val pos = llmSec.getPosition(snapView).coerceIn(0, secondValues.lastIndex)
                            val seconds = secondValues[pos]

                            val minSnapView = snapDurationMin.findSnapView(llmMin)
                            val minPos = if (minSnapView != null) llmMin.getPosition(minSnapView).coerceIn(0, minuteValues.lastIndex) else initialMinutes
                            val minutes = minuteValues[minPos]

                            val newTotal = (minutes * 60L + seconds).coerceAtLeast(5L)
                            if (currentDuration[set.id] != newTotal) {
                                currentDuration[set.id] = newTotal
                                secondAdapter.setSelected(pos)
                                onValueChanged(set, "duration", newTotal.toDouble())
                                Log.d(TAG, "setupDurationExtra SECONDS: Updated to $newTotal")
                            }
                        }
                    }
                }
            })

            // ⭐ Botón PLAY para SEGUNDOS
            val btnStartSec = itemView.findViewById<View>(R.id.layout_duration_play_seconds)
            btnStartSec.setOnClickListener {
                Log.d(TAG, "setupDurationExtra SECONDS: PLAY button clicked")
                if (isSequenceMode() && !durationCountdownActive) {
                    val selectedDuration = currentDuration[set.id] ?: 0L
                    Log.d(TAG, "setupDurationExtra SECONDS: Starting countdown with $selectedDuration")
                    // Ocultar pickers
                    itemView.findViewById<View>(R.id.frame_minutes_picker).visibility = View.GONE
                    itemView.findViewById<View>(R.id.frame_seconds_picker).visibility = View.GONE
                    // Mostrar contadores
                    frameDurationCounterMinutes.visibility = View.VISIBLE
                    frameDurationCounterSeconds.visibility = View.VISIBLE
                    startDurationCountdown(selectedDuration, set.id, tvParamUnit)
                }
            }
        }

        private fun bindRestTimer(set: RoutineSetTemplateResponse) {
            restSeconds = set.restAfterSet ?: 0
            if (restSeconds <= 0) {
                layoutRestContainer.visibility = View.GONE
                return
            }
            layoutRestContainer.visibility = View.VISIBLE
            updateRestLabel()
            countDownTimer?.cancel()
            restTimerActive = false

            layoutRestContainer.setOnClickListener {
                if (restTimerActive) {
                    countDownTimer?.cancel()
                    countDownTimer = null
                    restTimerActive = false
                    updateRestLabel()
                } else {
                    restTimerActive = true

                    countDownTimer = object : CountDownTimer(restSeconds * 1000L, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            if (!restTimerActive || !itemView.isAttachedToWindow) {
                                cancel()
                                return
                            }
                            val secondsLeft = (millisUntilFinished / 1000).toInt()
                            tvRestTimer.text = "${secondsLeft}s descanso"
                            tvRestHint.text = "STOP"
                        }

                        override fun onFinish() {
                            if (restTimerActive && itemView.isAttachedToWindow) {
                                restTimerActive = false
                                countDownTimer = null
                                WorkoutHaptics.restFinished(itemView.context)
                                Thread { WorkoutSoundManager.playRestFinished(itemView.context) }.start()
                                updateRestLabel()
                                if (isSequenceMode()) onSetRestFinished(myIndex)
                            }
                        }
                    }.start()
                }
            }
        }

        private fun updateRestLabel() {
            tvRestTimer.text = "${restSeconds}s descanso"
            tvRestHint.text  = "TAP"
        }

        private fun setColumnWeight(layout: LinearLayout, weight: Float) {
            val lp = layout.layoutParams as? LinearLayout.LayoutParams ?: return
            lp.weight = weight; layout.layoutParams = lp
        }

        private fun buildSummary(
            hasReps: Boolean, hasDuration: Boolean, hasParam: Boolean, setId: Long
        ): String {
            val unit = paramLabel[setId] ?: "KG"
            return listOfNotNull(
                if (hasReps) "REPS" else null,
                if (hasDuration) "TIEMPO" else null,
                if (hasParam) unit else null
            ).joinToString(" · ")
        }

        private fun generateParamValues(type: String, step: Double): List<Double> {
            return when (type) {
                "percentage" -> (0..100 step 5).map { it.toDouble() }
                "integer"    -> (0..200).map { it.toDouble() }
                "distance"   -> (0..1000 step 1).map { it.toDouble() }
                else -> {
                    val list = mutableListOf<Double>()
                    var v = 0.0
                    while (v <= 300.0) {
                        list.add(v)
                        v = Math.round((v + step) * 100) / 100.0
                    }
                    list
                }
            }
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
            else -> if (v % 1.0 == 0.0) v.toInt().toString() else "%.1f".format(v)
        }

        private fun formatDuration(seconds: Long): String {
            val m = seconds / 60; val s = seconds % 60
            return when {
                m > 0 -> String.format("%d:%02d", m, s)
                else -> String.format("%ds", s)
            }
        }
    }

    inner class PickerAdapter(
        private val values: List<String>,
        initialPos: Int = 0,
        private val colorGold: Int,
        private val colorDim: Int
    ) : RecyclerView.Adapter<PickerAdapter.PVH>() {

        private var selectedPos: Int = initialPos.coerceIn(0, values.lastIndex.coerceAtLeast(0))

        fun setSelected(pos: Int) {
            val old = selectedPos
            selectedPos = pos
            notifyItemChanged(old)
            notifyItemChanged(pos)
        }

        inner class PVH(v: View) : RecyclerView.ViewHolder(v) {
            val tv: TextView = v.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PVH {
            val tv = TextView(parent.context).apply {
                id = android.R.id.text1
                layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    40.dpToPxStatic(parent.context)
                )
                gravity  = android.view.Gravity.CENTER
                textSize = 22f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            return PVH(tv)
        }

        override fun onBindViewHolder(h: PVH, pos: Int) {
            h.tv.text = values[pos]
            val isSelected = pos == selectedPos
            h.tv.setTextColor(if (isSelected) colorGold else colorDim)
            h.tv.textSize = if (isSelected) 26f else 20f
            h.tv.alpha = when {
                isSelected -> 1f
                Math.abs(pos - selectedPos) == 1 -> 0.55f
                else -> 0.20f
            }
        }

        override fun getItemCount() = values.size

        private fun Int.dpToPxStatic(context: android.content.Context): Int =
            (this * context.resources.displayMetrics.density).toInt()
    }
}

private infix fun IntRange.step(step: Int): List<Int> {
    val list = mutableListOf<Int>()
    var i = this.first
    while (i <= this.last) { list.add(i); i += step }
    return list
}