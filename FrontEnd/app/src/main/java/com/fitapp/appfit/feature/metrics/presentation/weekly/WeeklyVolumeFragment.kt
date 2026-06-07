package com.fitapp.appfit.feature.metrics.presentation.weekly

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.databinding.FragmentMetricsWeeklyBinding
import com.fitapp.appfit.feature.metrics.data.MetricsReadRepositoryImpl
import com.fitapp.appfit.feature.metrics.domain.usecase.GetWeeklyVolumeUseCase
import kotlinx.coroutines.launch

class WeeklyVolumeFragment : Fragment() {

    private var _binding: FragmentMetricsWeeklyBinding? = null
    private val binding get() = _binding!!
    private val adapter = WeeklyVolumeAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMetricsWeeklyBinding.inflate(inflater, container, false)
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
            val points = GetWeeklyVolumeUseCase(MetricsReadRepositoryImpl(requireContext()))()
            adapter.submitList(points)
            binding.progressBar.isVisible = false
            binding.tvEmpty.isVisible = points.isEmpty()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
