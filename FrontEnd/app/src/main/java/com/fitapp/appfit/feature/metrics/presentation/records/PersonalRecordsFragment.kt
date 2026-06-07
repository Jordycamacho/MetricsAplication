package com.fitapp.appfit.feature.metrics.presentation.records

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.databinding.FragmentMetricsRecordsBinding
import com.fitapp.appfit.feature.metrics.data.MetricsReadRepositoryImpl
import com.fitapp.appfit.feature.metrics.domain.usecase.GetPersonalRecordsUseCase
import kotlinx.coroutines.launch

class PersonalRecordsFragment : Fragment() {

    private var _binding: FragmentMetricsRecordsBinding? = null
    private val binding get() = _binding!!
    private val adapter = PersonalRecordsAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMetricsRecordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.rvRecords.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecords.adapter = adapter
        loadData()
    }

    private fun loadData() {
        binding.progressBar.isVisible = true
        lifecycleScope.launch {
            val records = GetPersonalRecordsUseCase(MetricsReadRepositoryImpl(requireContext()))()
            adapter.submitList(records)
            binding.progressBar.isVisible = false
            binding.tvEmpty.isVisible = records.isEmpty()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
