package com.fitapp.appfit.feature.metrics.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fitapp.appfit.R
import com.fitapp.appfit.databinding.FragmentMetricsBinding
import com.fitapp.appfit.feature.metrics.domain.util.DaySessionLabelFormatter
import com.fitapp.appfit.feature.metrics.presentation.hub.MetricsViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MetricsFragment : Fragment() {

    private var _binding: FragmentMetricsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MetricsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMetricsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupNavigation()
        observeOverview()
        viewModel.loadOverview()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadOverview()
    }

    private fun observeOverview() {
        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            binding.layoutOverview.isVisible = !loading
            binding.progressOverview.isVisible = loading == true
        }
        viewModel.overview.observe(viewLifecycleOwner) { overview ->
            if (overview == null) return@observe
            binding.tvWeeklySessions.text = "${overview.weeklySessions}"
            binding.tvWeeklyVolume.text = String.format("%.0f kg", overview.weeklyVolumeKg)
            binding.tvStreakWeeks.text = "${overview.streakWeeks}"
            binding.tvTotalVolume.text = overview.totalVolumeKg?.let {
                String.format("%.0f kg", it)
            } ?: "--"
            overview.lastSession?.let { session ->
                val dayLabel = DaySessionLabelFormatter.shortLabel(
                    session.dayOfWeek, session.sessionNumber, session.dayLabel
                )
                binding.tvLastSession.text = "${session.routineName} · $dayLabel · ${formatShortDate(session.startTime)}"
                binding.tvLastSession.isVisible = true
            } ?: run {
                binding.tvLastSession.isVisible = false
            }
        }
    }

    private fun setupNavigation() {
        binding.cardWorkoutHistory.setOnClickListener {
            findNavController().navigate(R.id.action_metrics_to_session_history)
        }
        binding.cardWeeklyVolume.setOnClickListener {
            findNavController().navigate(R.id.action_metrics_to_weekly_volume)
        }
        binding.cardConsistency.setOnClickListener {
            findNavController().navigate(R.id.action_metrics_to_consistency)
        }
        binding.cardPersonalRecords.setOnClickListener {
            findNavController().navigate(R.id.action_metrics_to_personal_records)
        }
        binding.cardExerciseProgress.setOnClickListener {
            findNavController().navigate(R.id.action_metrics_to_exercise_progress)
        }
        binding.cardAdvancedAnalytics.setOnClickListener {
            findNavController().navigate(R.id.action_metrics_to_advanced_analytics)
        }
        binding.cardMetricsSettings.setOnClickListener {
            findNavController().navigate(R.id.action_metrics_to_settings)
        }
    }

    private fun formatShortDate(iso: String): String = try {
        LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("d MMM"))
    } catch (_: Exception) { "" }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
