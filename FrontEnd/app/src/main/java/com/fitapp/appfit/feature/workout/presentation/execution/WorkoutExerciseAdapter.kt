package com.fitapp.appfit.feature.workout.presentation.execution

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.core.util.RestTimer
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineResponse
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineExerciseResponse
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse
import com.fitapp.appfit.feature.workout.domain.model.WorkoutCompletionState
import com.fitapp.appfit.feature.workout.util.WorkoutHaptics
import com.fitapp.appfit.feature.workout.util.WorkoutPreferences
import com.fitapp.appfit.feature.workout.util.WorkoutSoundManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class WorkoutExerciseAdapter(
    private val onSetValueChanged: (RoutineExerciseResponse, RoutineSetTemplateResponse, String, Double) -> Unit,
    private val onSetCompletedToggled: (RoutineExerciseResponse, RoutineSetTemplateResponse, Boolean) -> Unit,
    private val completionState: WorkoutCompletionState
) : RecyclerView.Adapter<WorkoutExerciseAdapter.ExerciseViewHolder>() {

    private var exercises: List<RoutineExerciseResponse> = emptyList()
    private val expandedPositions = mutableSetOf<Int>()
    private val viewHolders = mutableMapOf<Int, ExerciseViewHolder>()

    fun submitExercises(list: List<RoutineExerciseResponse>) {
        exercises = list.sortedBy { it.position }
        expandedPositions.clear()
        viewHolders.clear()
        notifyDataSetChanged()
    }

    fun updateExercises(list: List<RoutineExerciseResponse>) {
        exercises = list.sortedBy { it.position }
        notifyDataSetChanged()
    }

    fun submitRoutine(routine: RoutineResponse) {
        exercises = routine.exercises
            ?.sortedWith(compareBy<RoutineExerciseResponse> { it.dayOfWeek ?: "" }.thenBy { it.position })
            ?: emptyList()
        expandedPositions.clear()
        viewHolders.clear()
        notifyDataSetChanged()
    }

    /**
     * NUEVO: Refrescar solo los checkboxes sin colapsar expandidos
     */
    fun refreshCheckboxes() {
        viewHolders.forEach { (_, holder) ->
            holder.refreshCheckboxState()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ExerciseViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_workout_exercise, parent, false)
        )

    override fun onBindViewHolder(h: ExerciseViewHolder, pos: Int) {
        h.bind(exercises[pos], pos)
        viewHolders[pos] = h
    }

    override fun getItemCount() = exercises.size
    override fun onViewRecycled(h: ExerciseViewHolder) {
        super.onViewRecycled(h)
        h.stopTimer()
        viewHolders.remove(h.adapterPosition)
    }
    override fun onViewDetachedFromWindow(h: ExerciseViewHolder) {
        super.onViewDetachedFromWindow(h)
        h.stopTimer()
    }

    // ─────────────────────────────────────────────────────────────────────────

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val cardExercise: View = itemView.findViewById(R.id.card_exercise)
        private val tvExerciseName: TextView = itemView.findViewById(R.id.tv_exercise_name)
        private val ivExpand: View = itemView.findViewById(R.id.iv_expand)
        private val layoutSets: View = itemView.findViewById(R.id.layout_sets_container)
        private val recyclerSets: RecyclerView = itemView.findViewById(R.id.recycler_sets)
        private val layoutExerciseRest: LinearLayout = itemView.findViewById(R.id.layout_exercise_rest)
        private val tvExerciseRest: TextView = itemView.findViewById(R.id.tv_exercise_rest)
        private val tvExerciseRestHint: TextView = itemView.findViewById(R.id.tv_exercise_rest_hint)
        private val viewGroupStripe: View = itemView.findViewById(R.id.view_group_stripe)

        // Badges v2
        private val layoutBadges: LinearLayout = itemView.findViewById(R.id.layout_exercise_badges)
        private val tvSpecialModeBadge: TextView = itemView.findViewById(R.id.tv_special_mode_badge)
        private val tvGroupBadge: TextView = itemView.findViewById(R.id.tv_group_badge)
        private val tvNotesBadge: TextView = itemView.findViewById(R.id.tv_notes_badge)
        private val tvSpecialModeInfo: TextView = itemView.findViewById(R.id.tv_special_mode_info)

        // NUEVO: Checkbox y contador de sets
        private val checkboxExerciseCompleted: CheckBox = itemView.findViewById(R.id.checkbox_exercise_completed)
        private val tvSetsProgress: TextView = itemView.findViewById(R.id.tv_sets_progress)

        private var currentExercise: RoutineExerciseResponse? = null
        private var restSeconds = 0
        private var restTimerActive = false

        private val setAdapter: RecyclerView.Adapter<*> by lazy {
            val viewType = WorkoutPreferences.getSetViewType(itemView.context)
            when (viewType) {
                WorkoutPreferences.SetViewType.CLASSIC -> WorkoutSetAdapterClassic(
                    onValueChanged = { set, type, value ->
                        currentExercise?.let { onSetValueChanged(it, set, type, value) }
                    },
                    onSetCompletedToggled = { set, completed ->
                        currentExercise?.let { onSetCompletedToggled(it, set, completed) }
                        updateSetCounter()
                    },
                    onSequenceComplete = {
                        if (restSeconds > 0 && itemView.isAttachedToWindow) {
                            WorkoutHaptics.exerciseComplete(itemView.context)
                            restTimerActive = true
                            tvExerciseRest.text = "${restSeconds}s"
                            tvExerciseRestHint.text = "STOP"
                            restTimer.start(restSeconds)
                        }
                    },
                    completionState = completionState
                )
                WorkoutPreferences.SetViewType.MODERN -> WorkoutSetAdapter(
                    onValueChanged = { set, type, value ->
                        currentExercise?.let { onSetValueChanged(it, set, type, value) }
                    },
                    onSetCompletedToggled = { set, completed ->
                        currentExercise?.let { onSetCompletedToggled(it, set, completed) }
                        updateSetCounter()
                    },
                    onSequenceComplete = {
                        if (restSeconds > 0 && itemView.isAttachedToWindow) {
                            WorkoutHaptics.exerciseComplete(itemView.context)
                            restTimerActive = true
                            tvExerciseRest.text = "${restSeconds}s"
                            tvExerciseRestHint.text = "STOP"
                            restTimer.start(restSeconds)
                        }
                    },
                    completionState = completionState
                )
            }
        }

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
                    Thread { WorkoutSoundManager.playRestFinished(itemView.context) }.start()
                    tvExerciseRest.text = "${restSeconds}s"
                    tvExerciseRestHint.text = "TAP"
                }
            }
        )

        fun stopTimer() { restTimerActive = false; restTimer.stop() }

        init {
            recyclerSets.layoutManager = LinearLayoutManager(itemView.context)
            recyclerSets.adapter = setAdapter as RecyclerView.Adapter<RecyclerView.ViewHolder>
            recyclerSets.isNestedScrollingEnabled = false

            // Tap: expandir/colapsar sets
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

            // Long-press: menú contextual
            cardExercise.setOnLongClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    showExerciseContextMenu(exercises[pos])
                }
                true
            }

            // CORREGIDO: Checkbox del ejercicio propaga a todos los sets
            // SIN notifyDataSetChanged() - solo refrescar checkboxes
            checkboxExerciseCompleted.setOnCheckedChangeListener { _, isChecked ->
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val exercise = exercises[pos]
                    exercise.setsTemplate?.forEach { set ->
                        completionState.markSetCompleted(set.id, exercise.exerciseId, isChecked)
                        onSetCompletedToggled(exercise, set, isChecked)
                    }
                    // Solo refrescar los checkboxes de sets, no todo
                    (setAdapter as? WorkoutSetAdapterClassic)?.refreshCheckboxes()
                    //(setAdapter as? WorkoutSetAdapter)?.refreshCheckboxes()
                    updateSetCounter()
                }
            }
        }

        /**
         * NUEVO: Refrescar solo el checkbox del ejercicio sin afectar expandidos
         */
        fun refreshCheckboxState() {
            currentExercise?.let { exercise ->
                checkboxExerciseCompleted.setOnCheckedChangeListener(null)
                checkboxExerciseCompleted.isChecked = completionState.isExerciseCompleted(exercise.exerciseId)
                checkboxExerciseCompleted.setOnCheckedChangeListener { _, isChecked ->
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        val currentEx = exercises[pos]
                        currentEx.setsTemplate?.forEach { set ->
                            completionState.markSetCompleted(set.id, currentEx.exerciseId, isChecked)
                            onSetCompletedToggled(currentEx, set, isChecked)
                        }
                        (setAdapter as? WorkoutSetAdapterClassic)?.refreshCheckboxes()
                        //(setAdapter as? WorkoutSetAdapter)?.refreshCheckboxes()
                        updateSetCounter()
                    }
                }
            }
        }

        fun bind(exercise: RoutineExerciseResponse, position: Int) {
            stopTimer()
            currentExercise = exercise
            tvExerciseName.text = exercise.exerciseName ?: "Ejercicio ${exercise.position}"

            // Actualizar checkbox
            refreshCheckboxState()

            bindGroupStripe(exercise)
            bindBadges(exercise)
            bindSpecialModeInfo(exercise)
            bindRestTimer(exercise)
            bindSets(exercise)
            bindExpandState(position)
            updateSetCounter()
        }

        // ── Actualizar contador de sets ──────────────────────────────────────

        private fun updateSetCounter() {
            currentExercise?.let { exercise ->
                val completed = completionState.getCompletedSetsCount(exercise.exerciseId)
                val total = completionState.getTotalSetsCount(exercise.exerciseId)
                tvSetsProgress.text = "$completed/$total"
            }
        }

        // ── Franja lateral de color según agrupación ──────────────────────────

        private fun bindGroupStripe(exercise: RoutineExerciseResponse) {
            val color = when {
                !exercise.circuitGroupId.isNullOrBlank() ->
                    ContextCompat.getColor(itemView.context, R.color.set_type_giant)
                !exercise.superSetGroupId.isNullOrBlank() ->
                    ContextCompat.getColor(itemView.context, R.color.set_type_super)
                else ->
                    ContextCompat.getColor(itemView.context, R.color.gold_primary)
            }
            viewGroupStripe.setBackgroundColor(color)
        }

        // ── Badges: modo especial + agrupación + notas ────────────────────────

        private fun bindBadges(exercise: RoutineExerciseResponse) {
            var anyBadge = false

            val specialMode = detectSpecialMode(exercise)
            if (specialMode != null) {
                tvSpecialModeBadge.text = specialMode.label
                tvSpecialModeBadge.backgroundTintList =
                    ContextCompat.getColorStateList(itemView.context, specialMode.colorRes)
                tvSpecialModeBadge.isVisible = true
                anyBadge = true
            } else {
                tvSpecialModeBadge.isVisible = false
            }

            val groupLabel = when {
                !exercise.circuitGroupId.isNullOrBlank() ->
                    "Circuito · ${exercise.circuitRoundCount ?: "?"} rondas"
                !exercise.superSetGroupId.isNullOrBlank() -> "Superset"
                else -> null
            }
            if (groupLabel != null) {
                tvGroupBadge.text = groupLabel
                val groupColorRes = if (!exercise.circuitGroupId.isNullOrBlank())
                    R.color.set_type_giant else R.color.set_type_super
                tvGroupBadge.backgroundTintList =
                    ContextCompat.getColorStateList(itemView.context, groupColorRes)
                tvGroupBadge.isVisible = true
                anyBadge = true
            } else {
                tvGroupBadge.isVisible = false
            }

            val hasNotes = !exercise.notes.isNullOrBlank()
            tvNotesBadge.isVisible = hasNotes
            if (hasNotes) anyBadge = true

            layoutBadges.isVisible = anyBadge
        }

        // ── Info del modo especial debajo de badges ───────────────────────────

        private fun bindSpecialModeInfo(exercise: RoutineExerciseResponse) {
            val info = buildSpecialModeInfo(exercise)
            if (info != null) {
                tvSpecialModeInfo.text = info
                tvSpecialModeInfo.isVisible = true
            } else {
                tvSpecialModeInfo.isVisible = false
            }
        }

        private fun buildSpecialModeInfo(exercise: RoutineExerciseResponse): String? = when {
            (exercise.amrapDurationSeconds ?: 0) > 0 ->
                "⏱ ${formatSeconds(exercise.amrapDurationSeconds!!)} máx repeticiones"

            (exercise.emomIntervalSeconds ?: 0) > 0 ->
                "⏱ ${formatSeconds(exercise.emomIntervalSeconds!!)} · ${exercise.emomTotalRounds ?: "?"} rondas"

            (exercise.tabataWorkSeconds ?: 0) > 0 ->
                "▶ ${formatSeconds(exercise.tabataWorkSeconds!!)}  ·  ⏸ ${formatSeconds(exercise.tabataRestSeconds ?: 0)}  ·  ${exercise.tabataRounds ?: "?"} rondas"

            else -> null
        }

        // ── Descanso del ejercicio ────────────────────────────────────────────

        private fun bindRestTimer(exercise: RoutineExerciseResponse) {
            restSeconds = exercise.restAfterExercise ?: 0
            if (restSeconds > 0) {
                layoutExerciseRest.visibility = View.VISIBLE
                tvExerciseRest.text = "${restSeconds}s"
                tvExerciseRestHint.text = "TAP"
                layoutExerciseRest.setOnClickListener {
                    if (restTimerActive) {
                        restTimerActive = false
                        restTimer.stop()
                        tvExerciseRest.text = "${restSeconds}s"
                        tvExerciseRestHint.text = "TAP"
                    } else {
                        restTimerActive = true
                        restTimer.start(restSeconds)
                    }
                }
            } else {
                layoutExerciseRest.visibility = View.GONE
            }
        }

        // ── Sets ──────────────────────────────────────────────────────────────

        private fun bindSets(exercise: RoutineExerciseResponse) {
            when (setAdapter) {
                is WorkoutSetAdapterClassic ->
                    (setAdapter as WorkoutSetAdapterClassic).submitList(exercise.setsTemplate ?: emptyList())
                is WorkoutSetAdapter ->
                    (setAdapter as WorkoutSetAdapter).submitList(exercise.setsTemplate ?: emptyList())
            }
        }

        private fun bindExpandState(position: Int) {
            if (expandedPositions.contains(position)) {
                layoutSets.visibility = View.VISIBLE
                ivExpand.rotation = 180f
            } else {
                layoutSets.visibility = View.GONE
                ivExpand.rotation = 0f
            }
        }

        // ── Menú contextual (long-press) ──────────────────────────────────────

        private fun showExerciseContextMenu(exercise: RoutineExerciseResponse) {
            val popup = PopupMenu(itemView.context, itemView)

            if (!exercise.notes.isNullOrBlank()) {
                popup.menu.add(0, MENU_VIEW_NOTES, 0, "📝  Ver nota")
            }

            val specialMode = detectSpecialMode(exercise)
            if (specialMode != null) {
                popup.menu.add(0, MENU_VIEW_MODE_INFO, 1, "${specialMode.emoji}  Info ${specialMode.label}")
            }

            if (!exercise.circuitGroupId.isNullOrBlank() || !exercise.superSetGroupId.isNullOrBlank()) {
                popup.menu.add(0, MENU_VIEW_GROUP_INFO, 2, "🔗  Info agrupación")
            }

            popup.menu.add(0, MENU_MARK_ALL_DONE, 3, "✅  Marcar todos los sets completos")
            popup.menu.add(0, MENU_CLEAR_SETS, 4, "↩️  Limpiar sets completados")

            val pos = adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                if (expandedPositions.contains(pos)) {
                    popup.menu.add(0, MENU_COLLAPSE, 5, "▲  Colapsar ejercicio")
                } else {
                    popup.menu.add(0, MENU_EXPAND, 5, "▼  Expandir ejercicio")
                }
            }

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    MENU_VIEW_NOTES -> showNotesDialog(exercise)
                    MENU_VIEW_MODE_INFO -> showModeInfoDialog(exercise, specialMode)
                    MENU_VIEW_GROUP_INFO -> showGroupInfoDialog(exercise)
                    MENU_MARK_ALL_DONE -> markAllSetsCompleted(exercise)
                    MENU_CLEAR_SETS -> clearAllSetsCompleted(exercise)
                    MENU_EXPAND -> {
                        val p = adapterPosition
                        if (p != RecyclerView.NO_POSITION) {
                            expandedPositions.add(p)
                            layoutSets.visibility = View.VISIBLE
                            ivExpand.rotation = 180f
                        }
                    }
                    MENU_COLLAPSE -> {
                        val p = adapterPosition
                        if (p != RecyclerView.NO_POSITION) {
                            expandedPositions.remove(p)
                            layoutSets.visibility = View.GONE
                            ivExpand.rotation = 0f
                        }
                    }
                }
                true
            }

            popup.show()
        }

        private fun showNotesDialog(exercise: RoutineExerciseResponse) {
            MaterialAlertDialogBuilder(itemView.context)
                .setTitle("📝  ${exercise.exerciseName ?: "Ejercicio"}")
                .setMessage(exercise.notes)
                .setPositiveButton("Cerrar", null)
                .show()
        }

        private fun showModeInfoDialog(exercise: RoutineExerciseResponse, mode: SpecialModeInfo?) {
            if (mode == null) return
            val detail = buildSpecialModeDetail(exercise) ?: return
            MaterialAlertDialogBuilder(itemView.context)
                .setTitle("${mode.emoji}  ${mode.label}")
                .setMessage(detail)
                .setPositiveButton("Cerrar", null)
                .show()
        }

        private fun showGroupInfoDialog(exercise: RoutineExerciseResponse) {
            val sb = StringBuilder()
            if (!exercise.circuitGroupId.isNullOrBlank()) {
                sb.appendLine("Tipo: Circuito")
                sb.appendLine("Rondas: ${exercise.circuitRoundCount ?: "No especificado"}")
                sb.appendLine("ID grupo: …${exercise.circuitGroupId.takeLast(6)}")
            } else if (!exercise.superSetGroupId.isNullOrBlank()) {
                sb.appendLine("Tipo: Superset / Giant Set")
                sb.appendLine("ID grupo: …${exercise.superSetGroupId.takeLast(6)}")
            }
            MaterialAlertDialogBuilder(itemView.context)
                .setTitle("🔗  Agrupación")
                .setMessage(sb.toString().trimEnd())
                .setPositiveButton("Cerrar", null)
                .show()
        }

        private fun markAllSetsCompleted(exercise: RoutineExerciseResponse) {
            exercise.setsTemplate?.forEach { set ->
                completionState.markSetCompleted(set.id, exercise.exerciseId, true)
                onSetCompletedToggled(exercise, set, true)
            }
            updateSetCounter()
            refreshCheckboxState()
        }

        private fun clearAllSetsCompleted(exercise: RoutineExerciseResponse) {
            exercise.setsTemplate?.forEach { set ->
                completionState.markSetCompleted(set.id, exercise.exerciseId, false)
                onSetCompletedToggled(exercise, set, false)
            }
            updateSetCounter()
            refreshCheckboxState()
        }

        // ── Helpers de modo especial ──────────────────────────────────────────

        private fun detectSpecialMode(exercise: RoutineExerciseResponse): SpecialModeInfo? = when {
            (exercise.amrapDurationSeconds ?: 0) > 0 ->
                SpecialModeInfo("AMRAP", "⏱", R.color.set_type_super)
            (exercise.emomIntervalSeconds ?: 0) > 0 ->
                SpecialModeInfo("EMOM", "🔁", R.color.set_type_cluster)
            (exercise.tabataWorkSeconds ?: 0) > 0 ->
                SpecialModeInfo("TABATA", "⚡", R.color.set_type_warmup)
            else -> null
        }

        private fun buildSpecialModeDetail(exercise: RoutineExerciseResponse): String? = when {
            (exercise.amrapDurationSeconds ?: 0) > 0 ->
                "Realiza el máximo de repeticiones posibles en:\n\n${formatSeconds(exercise.amrapDurationSeconds!!)}"

            (exercise.emomIntervalSeconds ?: 0) > 0 ->
                "Cada minuto en el minuto:\n\n" +
                        "• Intervalo: ${formatSeconds(exercise.emomIntervalSeconds!!)}\n" +
                        "• Rondas totales: ${exercise.emomTotalRounds ?: "?"}"

            (exercise.tabataWorkSeconds ?: 0) > 0 ->
                "Protocolo Tabata:\n\n" +
                        "• Trabajo: ${formatSeconds(exercise.tabataWorkSeconds!!)}\n" +
                        "• Descanso: ${formatSeconds(exercise.tabataRestSeconds ?: 0)}\n" +
                        "• Rondas: ${exercise.tabataRounds ?: "?"}"

            else -> null
        }

        private fun formatSeconds(seconds: Int): String {
            val m = seconds / 60
            val s = seconds % 60
            return if (m > 0 && s > 0) "${m}m ${s}s"
            else if (m > 0) "${m}m"
            else "${s}s"
        }
    }

    companion object {
        private const val MENU_VIEW_NOTES     = 1
        private const val MENU_VIEW_MODE_INFO = 2
        private const val MENU_VIEW_GROUP_INFO = 3
        private const val MENU_MARK_ALL_DONE  = 4
        private const val MENU_CLEAR_SETS     = 5
        private const val MENU_EXPAND         = 6
        private const val MENU_COLLAPSE       = 7
    }

    private data class SpecialModeInfo(
        val label: String,
        val emoji: String,
        val colorRes: Int
    )
}