package com.fitapp.appfit.feature.workout.presentation.execution

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.view.isVisible
import com.fitapp.appfit.R
import com.fitapp.appfit.feature.workout.util.WorkoutPreferences
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider

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
        private const val ARG_MAX_SESSION = "max_session"
        private const val ARG_MODE = "mode"
        private const val ARG_SESSION = "session"
        private const val ARG_DAY = "day"
        private const val ARG_DAY_KEYS = "day_keys"
        private const val ARG_DAY_TITLES = "day_titles"

        fun show(
            fragment: WorkoutFragment,
            usesDayGrouping: Boolean,
            maxSession: Int,
            currentMode: WorkoutPreferences.WorkoutFilterMode,
            currentSession: Int,
            currentDayOfWeek: String?,
            availableDays: List<Pair<String, String>>
        ) {
            if (fragment.parentFragmentManager.isStateSaved) return
            WorkoutFilterBottomSheet().apply {
                listener = fragment
                arguments = Bundle().apply {
                    putBoolean(ARG_USES_DAYS, usesDayGrouping)
                    putInt(ARG_MAX_SESSION, maxSession.coerceAtLeast(1))
                    putString(ARG_MODE, currentMode.name)
                    putInt(ARG_SESSION, currentSession)
                    putString(ARG_DAY, currentDayOfWeek)
                    putStringArrayList(ARG_DAY_KEYS, ArrayList(availableDays.map { it.first }))
                    putStringArrayList(ARG_DAY_TITLES, ArrayList(availableDays.map { it.second }))
                }
            }.show(fragment.parentFragmentManager, TAG)
        }
    }

    var listener: Listener? = null

    override fun getTheme(): Int = R.style.DarkBottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_workout_filter, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usesDays = arguments?.getBoolean(ARG_USES_DAYS) ?: true
        val maxSession = arguments?.getInt(ARG_MAX_SESSION) ?: 1
        val currentMode = WorkoutPreferences.WorkoutFilterMode.valueOf(
            arguments?.getString(ARG_MODE) ?: WorkoutPreferences.WorkoutFilterMode.ALL.name
        )
        val currentSession = arguments?.getInt(ARG_SESSION) ?: 1
        val currentDay = arguments?.getString(ARG_DAY)
        val dayKeys = arguments?.getStringArrayList(ARG_DAY_KEYS).orEmpty()
        val dayTitles = arguments?.getStringArrayList(ARG_DAY_TITLES).orEmpty()

        val rgFilter = view.findViewById<RadioGroup>(R.id.rg_filter_mode)
        val rbAll = view.findViewById<RadioButton>(R.id.rb_filter_all)
        val rbToday = view.findViewById<RadioButton>(R.id.rb_filter_today)
        val rbPickDay = view.findViewById<RadioButton>(R.id.rb_filter_pick_day)
        val rbSession = view.findViewById<RadioButton>(R.id.rb_filter_session)
        val layoutDay = view.findViewById<View>(R.id.layout_day_picker)
        val rgDayPicker = view.findViewById<RadioGroup>(R.id.rg_day_picker)
        val layoutSession = view.findViewById<View>(R.id.layout_session_picker)
        val sliderSession = view.findViewById<Slider>(R.id.slider_session)
        val tvSession = view.findViewById<TextView>(R.id.tv_session_value)
        val tvHint = view.findViewById<TextView>(R.id.tv_filter_hint)

        rbToday.isVisible = usesDays
        rbPickDay.isVisible = usesDays && dayKeys.isNotEmpty()
        rbSession.isVisible = !usesDays
        tvHint.text = if (usesDays) {
            "Puedes hacer el entrenamiento de cualquier día si te lo saltaste"
        } else {
            "Esta rutina usa sesiones numeradas"
        }

        var selectedDayKey: String? = currentDay
        if (usesDays && dayKeys.isNotEmpty()) {
            dayKeys.forEachIndexed { index, key ->
                val title = dayTitles.getOrNull(index) ?: key
                val count = rgDayPicker.childCount
                val rb = RadioButton(requireContext()).apply {
                    id = View.generateViewId()
                    text = title
                    setTextColor(0xFFFFFFFF.toInt())
                    textSize = 14f
                    buttonTintList = android.content.res.ColorStateList.valueOf(0xFF78703F.toInt())
                    val padV = (8 * resources.displayMetrics.density).toInt()
                    setPadding(0, padV, 0, padV)
                    tag = key
                }
                rgDayPicker.addView(rb)
                if (key == currentDay) {
                    rgDayPicker.check(rb.id)
                    selectedDayKey = key
                }
            }
            if (rgDayPicker.checkedRadioButtonId == -1 && dayKeys.isNotEmpty()) {
                val first = rgDayPicker.getChildAt(0) as RadioButton
                rgDayPicker.check(first.id)
                selectedDayKey = first.tag as String
            }
            rgDayPicker.setOnCheckedChangeListener { _, checkedId ->
                val rb = rgDayPicker.findViewById<RadioButton>(checkedId)
                selectedDayKey = rb?.tag as? String
            }
        }

        sliderSession.valueTo = maxSession.toFloat()
        sliderSession.value = currentSession.coerceIn(1, maxSession).toFloat()
        tvSession.text = "Sesión ${sliderSession.value.toInt()}"

        when (currentMode) {
            WorkoutPreferences.WorkoutFilterMode.ALL -> rgFilter.check(rbAll.id)
            WorkoutPreferences.WorkoutFilterMode.TODAY -> rgFilter.check(rbToday.id)
            WorkoutPreferences.WorkoutFilterMode.SESSION -> rgFilter.check(rbSession.id)
            WorkoutPreferences.WorkoutFilterMode.DAY -> rgFilter.check(rbPickDay.id)
        }
        updatePickerVisibility(usesDays, rgFilter.checkedRadioButtonId, rbPickDay.id, rbSession.id, layoutDay, layoutSession)

        rgFilter.setOnCheckedChangeListener { _, checkedId ->
            updatePickerVisibility(usesDays, checkedId, rbPickDay.id, rbSession.id, layoutDay, layoutSession)
        }

        sliderSession.addOnChangeListener { _, value, _ ->
            tvSession.text = "Sesión ${value.toInt()}"
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
            val mode = when (rgFilter.checkedRadioButtonId) {
                rbToday.id -> WorkoutPreferences.WorkoutFilterMode.TODAY
                rbPickDay.id -> WorkoutPreferences.WorkoutFilterMode.DAY
                rbSession.id -> WorkoutPreferences.WorkoutFilterMode.SESSION
                else -> WorkoutPreferences.WorkoutFilterMode.ALL
            }
            listener?.onFilterApplied(
                mode,
                sliderSession.value.toInt(),
                if (mode == WorkoutPreferences.WorkoutFilterMode.DAY) selectedDayKey else null
            )
            dismiss()
        }
    }

    private fun updatePickerVisibility(
        usesDays: Boolean,
        checkedId: Int,
        pickDayId: Int,
        sessionId: Int,
        layoutDay: View,
        layoutSession: View
    ) {
        layoutDay.isVisible = usesDays && checkedId == pickDayId
        layoutSession.isVisible = !usesDays && checkedId == sessionId
    }
}
