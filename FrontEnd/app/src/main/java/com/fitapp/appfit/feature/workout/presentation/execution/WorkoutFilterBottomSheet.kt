package com.fitapp.appfit.feature.workout.presentation.execution

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import com.fitapp.appfit.R
import com.fitapp.appfit.feature.workout.util.WorkoutPreferences
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class WorkoutFilterBottomSheet : BottomSheetDialogFragment() {

    interface Listener {
        fun onFilterApplied(
            mode: WorkoutPreferences.WorkoutFilterMode,
            sessionNumber: Int,
            dayOfWeek: String? = null
        )
        fun onExpandAll()
        fun onCollapseAll()
    }

    companion object {
        private const val TAG = "WorkoutFilterBottomSheet"
        private const val ARG_USES_DAYS = "uses_days"
        private const val ARG_MODE = "mode"
        private const val ARG_SESSION = "session"
        private const val ARG_DAY = "day"
        private const val ARG_DAY_KEYS = "day_keys"
        private const val ARG_DAY_TITLES = "day_titles"
        private const val ARG_SESSION_NUMBERS = "session_numbers"

        fun show(
            fragment: WorkoutFragment,
            usesDayGrouping: Boolean,
            currentMode: WorkoutPreferences.WorkoutFilterMode,
            currentSession: Int,
            currentDayOfWeek: String?,
            availableDays: List<Pair<String, String>>,
            availableSessions: List<Int>
        ) {
            if (fragment.parentFragmentManager.isStateSaved) return
            WorkoutFilterBottomSheet().apply {
                listener = fragment
                arguments = Bundle().apply {
                    putBoolean(ARG_USES_DAYS, usesDayGrouping)
                    putString(ARG_MODE, currentMode.name)
                    putInt(ARG_SESSION, currentSession)
                    putString(ARG_DAY, currentDayOfWeek)
                    putStringArrayList(ARG_DAY_KEYS, ArrayList(availableDays.map { it.first }))
                    putStringArrayList(ARG_DAY_TITLES, ArrayList(availableDays.map { it.second }))
                    putIntegerArrayList(ARG_SESSION_NUMBERS, ArrayList(availableSessions))
                }
            }.show(fragment.parentFragmentManager, TAG)
        }
    }

    var listener: Listener? = null

    private var selectedDayKey: String? = null
    private var selectedSession: Int = 1
    private var suppressScopeCallback = false
    private var chipGroupScopeRef: ChipGroup? = null

    override fun getTheme(): Int = R.style.DarkBottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_workout_filter, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usesDays = arguments?.getBoolean(ARG_USES_DAYS) ?: true
        val currentMode = WorkoutPreferences.WorkoutFilterMode.valueOf(
            arguments?.getString(ARG_MODE) ?: WorkoutPreferences.WorkoutFilterMode.ALL.name
        )
        val currentSession = arguments?.getInt(ARG_SESSION) ?: 1
        val currentDay = arguments?.getString(ARG_DAY)
        val dayKeys = arguments?.getStringArrayList(ARG_DAY_KEYS).orEmpty()
        val dayTitles = arguments?.getStringArrayList(ARG_DAY_TITLES).orEmpty()
        val sessionNumbers = arguments?.getIntegerArrayList(ARG_SESSION_NUMBERS)
            ?.takeIf { it.isNotEmpty() } ?: arrayListOf(1)

        val chipScopeAll = view.findViewById<Chip>(R.id.chip_scope_all)
        val chipScopeToday = view.findViewById<Chip>(R.id.chip_scope_today)
        val chipGroupScope = view.findViewById<ChipGroup>(R.id.chip_group_scope)
        chipGroupScopeRef = chipGroupScope
        val layoutDay = view.findViewById<View>(R.id.layout_day_picker)
        val chipGroupDays = view.findViewById<ChipGroup>(R.id.chip_group_days)
        val layoutSession = view.findViewById<View>(R.id.layout_session_picker)
        val chipGroupSessions = view.findViewById<ChipGroup>(R.id.chip_group_sessions)
        val tvHint = view.findViewById<TextView>(R.id.tv_filter_hint)

        chipScopeToday.isVisible = usesDays
        layoutDay.isVisible = usesDays && dayKeys.isNotEmpty()
        layoutSession.isVisible = !usesDays && sessionNumbers.isNotEmpty()

        tvHint.text = if (usesDays) {
            "Hoy es el día del calendario. También puedes elegir otro día si te lo saltaste."
        } else {
            "Elige la sesión que quieres hacer hoy."
        }

        selectedDayKey = currentDay
        selectedSession = currentSession

        if (usesDays) {
            populateDayChips(chipGroupDays, dayKeys, dayTitles, currentDay, currentMode)
        } else {
            populateSessionChips(chipGroupSessions, sessionNumbers, currentSession, currentMode)
        }

        suppressScopeCallback = true
        when (currentMode) {
            WorkoutPreferences.WorkoutFilterMode.ALL -> chipScopeAll.isChecked = true
            WorkoutPreferences.WorkoutFilterMode.TODAY -> chipScopeToday.isChecked = true
            WorkoutPreferences.WorkoutFilterMode.DAY -> {
                chipScopeAll.isChecked = false
                chipScopeToday.isChecked = false
            }
            WorkoutPreferences.WorkoutFilterMode.SESSION -> chipScopeAll.isChecked = false
        }
        suppressScopeCallback = false

        chipGroupScope.setOnCheckedStateChangeListener { _, checkedIds ->
            if (suppressScopeCallback || checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            when (checkedIds.first()) {
                chipScopeAll.id -> {
                    selectedDayKey = null
                    clearChipGroup(chipGroupDays)
                    clearChipGroup(chipGroupSessions)
                }
                chipScopeToday.id -> {
                    selectedDayKey = null
                    clearChipGroup(chipGroupDays)
                    clearChipGroup(chipGroupSessions)
                }
            }
        }

        view.findViewById<MaterialButton>(R.id.btn_expand_all).setOnClickListener {
            listener?.onExpandAll()
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.btn_collapse_all).setOnClickListener {
            listener?.onCollapseAll()
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.btn_apply_filter).setOnClickListener {
            val mode = resolveMode(chipScopeAll, chipScopeToday, chipGroupDays, chipGroupSessions, usesDays)
            listener?.onFilterApplied(
                mode,
                selectedSession,
                if (mode == WorkoutPreferences.WorkoutFilterMode.DAY) selectedDayKey else null
            )
            dismiss()
        }
    }

    private fun populateDayChips(
        group: ChipGroup,
        keys: List<String>,
        titles: List<String>,
        currentDay: String?,
        currentMode: WorkoutPreferences.WorkoutFilterMode
    ) {
        group.removeAllViews()
        keys.forEachIndexed { index, key ->
            val title = titles.getOrNull(index) ?: key
            val chip = layoutInflater.inflate(R.layout.item_filter_day_chip, group, false) as Chip
            chip.text = title
            chip.tag = key
            chip.isChecked = currentMode == WorkoutPreferences.WorkoutFilterMode.DAY && key == currentDay
            if (chip.isChecked) selectedDayKey = key
            chip.setOnCheckedChangeListener { button, isChecked ->
                if (!isChecked) return@setOnCheckedChangeListener
                selectedDayKey = button.tag as String
                clearScopeChips()
            }
            group.addView(chip)
        }
    }

    private fun populateSessionChips(
        group: ChipGroup,
        sessions: List<Int>,
        currentSession: Int,
        currentMode: WorkoutPreferences.WorkoutFilterMode
    ) {
        group.removeAllViews()
        sessions.forEach { session ->
            val chip = layoutInflater.inflate(R.layout.item_filter_day_chip, group, false) as Chip
            chip.text = "Sesión $session"
            chip.tag = session
            chip.isChecked = currentMode == WorkoutPreferences.WorkoutFilterMode.SESSION &&
                session == currentSession
            if (chip.isChecked) selectedSession = session
            chip.setOnCheckedChangeListener { button, isChecked ->
                if (!isChecked) return@setOnCheckedChangeListener
                selectedSession = button.tag as Int
                clearScopeChips()
            }
            group.addView(chip)
        }
    }

    private fun clearScopeChips() {
        suppressScopeCallback = true
        chipGroupScopeRef?.clearCheck()
        suppressScopeCallback = false
    }

    private fun clearChipGroup(group: ChipGroup) {
        for (i in 0 until group.childCount) {
            (group.getChildAt(i) as? Chip)?.isChecked = false
        }
    }

    private fun resolveMode(
        chipAll: Chip,
        chipToday: Chip,
        chipDays: ChipGroup,
        chipSessions: ChipGroup,
        usesDays: Boolean
    ): WorkoutPreferences.WorkoutFilterMode {
        if (chipToday.isChecked) return WorkoutPreferences.WorkoutFilterMode.TODAY
        if (usesDays) {
            val checkedDay = findCheckedTag(chipDays) as? String
            if (checkedDay != null) {
                selectedDayKey = checkedDay
                return WorkoutPreferences.WorkoutFilterMode.DAY
            }
        } else {
            val checkedSession = findCheckedTag(chipSessions) as? Int
            if (checkedSession != null) {
                selectedSession = checkedSession
                return WorkoutPreferences.WorkoutFilterMode.SESSION
            }
        }
        if (chipAll.isChecked || (!chipToday.isChecked && findCheckedTag(chipDays) == null &&
                findCheckedTag(chipSessions) == null)
        ) {
            return WorkoutPreferences.WorkoutFilterMode.ALL
        }
        return WorkoutPreferences.WorkoutFilterMode.ALL
    }

    private fun findCheckedTag(group: ChipGroup): Any? {
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as? Chip ?: continue
            if (chip.isChecked) return chip.tag
        }
        return null
    }
}
