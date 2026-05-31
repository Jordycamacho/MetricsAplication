package com.fitapp.appfit.feature.workout.presentation.execution

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineResponse
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineExerciseResponse
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse
import com.fitapp.appfit.feature.routine.model.setparameter.response.RoutineSetParameterResponse
import com.fitapp.appfit.feature.workout.domain.model.WorkoutCompletionState
import com.fitapp.appfit.feature.workout.domain.model.WorkoutDay
import com.fitapp.appfit.feature.workout.domain.model.WorkoutGroupType
import com.fitapp.appfit.feature.workout.presentation.execution.manager.SetParameterStateManager
import com.fitapp.appfit.feature.workout.util.WorkoutPreferences

class WorkoutDayAdapter(
    private val onShowNumericInput: (RoutineSetParameterResponse, Double, (Double) -> Unit) -> Unit,
    private val stateManager: SetParameterStateManager,
    private val onSetValueChanged: (RoutineExerciseResponse, RoutineSetTemplateResponse, String, Double) -> Unit,
    private val onSetCompletedToggled: (RoutineExerciseResponse, RoutineSetTemplateResponse, Boolean) -> Unit,
    private val completionState: WorkoutCompletionState,
    private val executionConfig: WorkoutExecutionConfig
) : RecyclerView.Adapter<WorkoutDayAdapter.DayViewHolder>() {

    data class WorkoutFocusTarget(val dayIndex: Int, val exerciseIndex: Int)

    private var allDays: List<WorkoutDay> = emptyList()
    private var days: List<WorkoutDay> = emptyList()
    private val expandedDays = mutableSetOf<Int>()
    private var isProcessingCheckbox = false
    private var usesDayGrouping = true
    private var pendingFocus: WorkoutFocusTarget? = null

    var filterMode: WorkoutPreferences.WorkoutFilterMode = WorkoutPreferences.WorkoutFilterMode.ALL
        private set
    var filterSessionNumber: Int = 1
        private set
    var filterDayOfWeek: String? = null
        private set

    private enum class ExerciseExpandCommand { NONE, EXPAND_ALL, COLLAPSE_ALL }
    private var exerciseExpandCommand = ExerciseExpandCommand.NONE

    var onFilterSubtitleChanged: ((String?) -> Unit)? = null

    companion object {
        private val DAY_ORDER = mapOf(
            "MONDAY" to 1, "TUESDAY" to 2, "WEDNESDAY" to 3,
            "THURSDAY" to 4, "FRIDAY" to 5, "SATURDAY" to 6, "SUNDAY" to 7
        )
        private val DAY_NAMES_ES = mapOf(
            "MONDAY" to "Lunes", "TUESDAY" to "Martes", "WEDNESDAY" to "Miércoles",
            "THURSDAY" to "Jueves", "FRIDAY" to "Viernes",
            "SATURDAY" to "Sábado", "SUNDAY" to "Domingo",
            "SIN_DIA" to "Sin día"
        )
    }

    fun expandAll() {
        for (i in days.indices) expandedDays.add(i)
        exerciseExpandCommand = ExerciseExpandCommand.EXPAND_ALL
        notifyDataSetChanged()
    }

    fun collapseAll() {
        expandedDays.clear()
        exerciseExpandCommand = ExerciseExpandCommand.COLLAPSE_ALL
        notifyDataSetChanged()
    }

    /** Días disponibles en la rutina: clave (MONDAY…) → título (Lunes…) */
    fun availableDays(): List<Pair<String, String>> =
        allDays.mapNotNull { day ->
            day.dayOfWeek?.let { key -> key to day.displayTitle }
        }

    fun submitRoutine(routine: RoutineResponse) {
        val exercises = routine.exercises.orEmpty()
        usesDayGrouping = exercises.any { it.dayOfWeek != null }

        allDays = if (usesDayGrouping) {
            exercises
                .groupBy { it.dayOfWeek ?: "SIN_DIA" }
                .map { (day, list) ->
                    WorkoutDay(
                        groupKey = day,
                        displayTitle = DAY_NAMES_ES[day] ?: day,
                        groupType = WorkoutGroupType.DAY,
                        dayOfWeek = day,
                        exercises = list.sortedBy { it.sessionOrder ?: it.position }
                    )
                }
                .sortedBy { DAY_ORDER[it.groupKey] ?: 99 }
        } else {
            exercises
                .groupBy { it.sessionNumber ?: 0 }
                .map { (session, list) ->
                    val title = if (session == 0) "Sin sesión" else "Sesión $session"
                    WorkoutDay(
                        groupKey = "SESSION_$session",
                        displayTitle = title,
                        groupType = WorkoutGroupType.SESSION,
                        sessionNumber = session.takeIf { it > 0 },
                        exercises = list.sortedBy { it.sessionOrder ?: it.position }
                    )
                }
                .sortedBy { it.sessionNumber ?: 0 }
        }

        applyFilter()
    }

    fun setFilter(
        mode: WorkoutPreferences.WorkoutFilterMode,
        sessionNumber: Int = filterSessionNumber,
        dayOfWeek: String? = filterDayOfWeek
    ) {
        filterMode = mode
        filterSessionNumber = sessionNumber
        filterDayOfWeek = dayOfWeek
        applyFilter()
    }

    fun suggestSessionNumber(sessionsPerWeek: Int?): Int {
        if (sessionsPerWeek == null || sessionsPerWeek <= 0) return 1
        val week = java.time.LocalDate.now().get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear())
        return ((week - 1) % sessionsPerWeek) + 1
    }

    fun availableSessionNumbers(): List<Int> =
        allDays.mapNotNull { it.sessionNumber }.distinct().sorted()

    fun getProgress(completionState: WorkoutCompletionState): Pair<Int, Int> {
        var total = 0
        var completed = 0
        days.forEach { day ->
            day.exercises.forEach { ex ->
                ex.setsTemplate?.forEach { set ->
                    total++
                    if (completionState.isSetCompleted(set.id)) completed++
                }
            }
        }
        return completed to total
    }

    fun findNextIncomplete(completionState: WorkoutCompletionState): WorkoutFocusTarget? {
        days.forEachIndexed { dayIdx, day ->
            day.exercises.forEachIndexed { exIdx, exercise ->
                val hasIncomplete = exercise.setsTemplate?.any { !completionState.isSetCompleted(it.id) } == true
                if (hasIncomplete) return WorkoutFocusTarget(dayIdx, exIdx)
            }
        }
        return null
    }

    fun focusNextIncomplete(completionState: WorkoutCompletionState): Boolean {
        val target = findNextIncomplete(completionState) ?: return false
        focusTarget(target)
        return true
    }

    fun focusTarget(target: WorkoutFocusTarget) {
        pendingFocus = target
        expandedDays.add(target.dayIndex)
        notifyItemChanged(target.dayIndex)
    }

    fun consumePendingFocus(): WorkoutFocusTarget? {
        val target = pendingFocus
        pendingFocus = null
        return target
    }

    private fun applyFilter() {
        days = when (filterMode) {
            WorkoutPreferences.WorkoutFilterMode.ALL -> allDays
            WorkoutPreferences.WorkoutFilterMode.TODAY -> {
                if (!usesDayGrouping) allDays
                else {
                    val today = java.time.LocalDate.now().dayOfWeek.name
                    allDays.filter { it.dayOfWeek == today }
                }
            }
            WorkoutPreferences.WorkoutFilterMode.SESSION -> {
                if (usesDayGrouping) allDays
                else allDays.filter { (it.sessionNumber ?: 0) == filterSessionNumber }
            }
            WorkoutPreferences.WorkoutFilterMode.DAY -> {
                if (!usesDayGrouping || filterDayOfWeek.isNullOrBlank()) allDays
                else allDays.filter { it.dayOfWeek == filterDayOfWeek }
            }
        }
        expandedDays.clear()
        exerciseExpandCommand = ExerciseExpandCommand.NONE
        updateFilterSubtitle()
        notifyDataSetChanged()
    }

    private fun updateFilterSubtitle() {
        val subtitle = when (filterMode) {
            WorkoutPreferences.WorkoutFilterMode.ALL -> null
            WorkoutPreferences.WorkoutFilterMode.TODAY -> {
                val day = days.firstOrNull()
                if (day != null) "${day.displayTitle} · ${day.exercises.size} ejercicios"
                else "Hoy · sin ejercicios"
            }
            WorkoutPreferences.WorkoutFilterMode.SESSION -> {
                val day = days.firstOrNull()
                if (day != null) "${day.displayTitle} · ${day.exercises.size} ejercicios"
                else "Sesión $filterSessionNumber · sin ejercicios"
            }
            WorkoutPreferences.WorkoutFilterMode.DAY -> {
                val day = days.firstOrNull()
                if (day != null) "${day.displayTitle} · ${day.exercises.size} ejercicios"
                else filterDayOfWeek?.let { DAY_NAMES_ES[it] ?: it }?.let { "$it · sin ejercicios" }
                    ?: "Día · sin ejercicios"
            }
        }
        onFilterSubtitleChanged?.invoke(subtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) =
        holder.bind(days[position], position)

    override fun getItemCount() = days.size

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvDay: TextView = itemView.findViewById(R.id.tv_day_title)
        private val ivExpand: ImageView = itemView.findViewById(R.id.iv_expand_day)
        private val recyclerExercises: RecyclerView = itemView.findViewById(R.id.recycler_exercises)
        private val container: View = itemView.findViewById(R.id.layout_day_container)
        private val tvExerciseCount: TextView = itemView.findViewById(R.id.tv_exercise_count)
        private val tvDayProgress: TextView = itemView.findViewById(R.id.tv_day_progress)
        private val checkboxDayCompleted: CheckBox = itemView.findViewById(R.id.checkbox_day_completed)

        private var exerciseAdapter: WorkoutExerciseAdapter? = null
        private var currentDayIndex = -1
        private var currentDayKey = ""

        init {
            recyclerExercises.layoutManager = LinearLayoutManager(itemView.context)
            recyclerExercises.isNestedScrollingEnabled = false

            itemView.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                if (expandedDays.contains(pos)) {
                    expandedDays.remove(pos)
                    container.visibility = View.GONE
                    ivExpand.animate().rotation(0f).setDuration(200).start()
                } else {
                    expandedDays.add(pos)
                    container.visibility = View.VISIBLE
                    ivExpand.animate().rotation(180f).setDuration(200).start()
                }
            }
        }

        fun bind(day: WorkoutDay, position: Int) {
            currentDayIndex = position
            currentDayKey = day.groupKey

            tvDay.text = day.displayTitle
            val count = day.exercises.size
            tvExerciseCount.text = "$count ejercicio${if (count != 1) "s" else ""}"

            if (exerciseAdapter == null) {
                exerciseAdapter = WorkoutExerciseAdapter(
                    onShowNumericInput = onShowNumericInput,
                    stateManager = stateManager,
                    onSetValueChanged = onSetValueChanged,
                    onSetCompletedToggled = onSetCompletedToggled,
                    completionState = completionState,
                    executionConfig = executionConfig
                )
                recyclerExercises.adapter = exerciseAdapter
            }
            exerciseAdapter?.submitExercises(day.exercises)

            when (exerciseExpandCommand) {
                ExerciseExpandCommand.EXPAND_ALL -> exerciseAdapter?.expandAllExercises()
                ExerciseExpandCommand.COLLAPSE_ALL -> exerciseAdapter?.collapseAllExercises()
                ExerciseExpandCommand.NONE -> {}
            }

            val focus = pendingFocus
            if (focus != null && focus.dayIndex == position) {
                exerciseAdapter?.focusExercise(focus.exerciseIndex, executionConfig.expandActiveOnly)
            }

            val dayKeyForCompletion = day.dayOfWeek ?: day.groupKey
            checkboxDayCompleted.setOnCheckedChangeListener(null)
            checkboxDayCompleted.isChecked = completionState.isDayCompleted(dayKeyForCompletion)
            checkboxDayCompleted.setOnCheckedChangeListener { _, isChecked ->
                if (isProcessingCheckbox) return@setOnCheckedChangeListener
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnCheckedChangeListener
                if (currentDayIndex != pos) return@setOnCheckedChangeListener

                isProcessingCheckbox = true
                try {
                    completionState.setDayCompleted(dayKeyForCompletion, isChecked)
                    updateDayProgress(day, dayKeyForCompletion)
                    exerciseAdapter?.notifyDataSetChanged()
                } finally {
                    isProcessingCheckbox = false
                }
            }

            updateDayProgress(day, dayKeyForCompletion)

            if (expandedDays.contains(position)) {
                container.visibility = View.VISIBLE
                ivExpand.rotation = 180f
            } else {
                container.visibility = View.GONE
                ivExpand.rotation = 0f
            }
        }

        private fun updateDayProgress(day: WorkoutDay, dayKey: String) {
            val completedCount = completionState.getCompletedExercisesCount(dayKey)
            val totalCount = day.exercises.size
            if (completedCount > 0) {
                tvDayProgress.text = " · $completedCount/$totalCount completados"
                tvDayProgress.visibility = View.VISIBLE
            } else {
                tvDayProgress.visibility = View.GONE
            }
        }
    }
}
