package com.fitapp.appfit.feature.metrics.presentation.history.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.FragmentManager
import com.fitapp.appfit.databinding.SheetMetricsHistoryFilterBinding
import com.fitapp.appfit.feature.metrics.domain.model.SessionHistoryFilter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.DayOfWeek

class MetricsHistoryFilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: SheetMetricsHistoryFilterBinding? = null
    private val binding get() = _binding!!

    private var onApply: ((SessionHistoryFilter) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SheetMetricsHistoryFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val days = listOf("Todos") + DayOfWeek.values().map { it.name }
        binding.spinnerDay.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, days)

        binding.btnApply.setOnClickListener {
            val dayIndex = binding.spinnerDay.selectedItemPosition
            val dayOfWeek = if (dayIndex <= 0) null else DayOfWeek.values()[dayIndex - 1].name
            val sessionNumber = binding.etSessionNumber.text?.toString()?.toIntOrNull()
            val filter = SessionHistoryFilter(
                dayOfWeek = dayOfWeek,
                sessionNumber = sessionNumber
            )
            onApply?.invoke(filter)
            dismiss()
        }
        binding.btnClear.setOnClickListener {
            onApply?.invoke(SessionHistoryFilter())
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun show(manager: FragmentManager, onApply: (SessionHistoryFilter) -> Unit) {
            MetricsHistoryFilterBottomSheet().apply {
                this.onApply = onApply
            }.show(manager, "metrics_history_filter")
        }
    }
}
