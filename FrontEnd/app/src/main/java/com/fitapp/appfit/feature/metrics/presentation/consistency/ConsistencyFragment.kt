package com.fitapp.appfit.feature.metrics.presentation.consistency

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.databinding.FragmentMetricsConsistencyBinding
import com.fitapp.appfit.feature.metrics.data.MetricsReadRepositoryImpl
import com.fitapp.appfit.feature.metrics.domain.usecase.GetConsistencyStatsUseCase
import kotlinx.coroutines.launch

class ConsistencyFragment : Fragment() {

    private var _binding: FragmentMetricsConsistencyBinding? = null
    private val binding get() = _binding!!
    private val adapter = WeekActivityAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMetricsConsistencyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.rvWeeks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWeeks.adapter = adapter
        loadData()
    }

    private fun loadData() {
        binding.progressBar.isVisible = true
        lifecycleScope.launch {
            val stats = GetConsistencyStatsUseCase(MetricsReadRepositoryImpl(requireContext()))()
            binding.tvStreak.text = "${stats.streakWeeks} semanas"
            binding.tvSessions4w.text = "${stats.sessionsLast4Weeks}"
            binding.tvActiveDays.text = "${stats.activeDaysLast4Weeks} días"
            adapter.submitList(stats.activityByWeek)
            binding.progressBar.isVisible = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
