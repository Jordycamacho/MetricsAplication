package com.fitapp.appfit.feature.metrics.presentation.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.R
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentMetricsSessionHistoryBinding
import com.fitapp.appfit.feature.metrics.domain.model.SessionHistoryFilter
import com.fitapp.appfit.feature.metrics.presentation.history.filter.MetricsHistoryFilterBottomSheet

class SessionHistoryFragment : Fragment() {

    private var _binding: FragmentMetricsSessionHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SessionHistoryViewModel by viewModels {
        SessionHistoryViewModelFactory(requireContext())
    }
    private lateinit var adapter: SessionHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMetricsSessionHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        setupRecyclerView()
        setupObservers()
        setupListeners()
        viewModel.loadHistory()
        viewModel.loadTotalVolume()
    }

    private fun setupRecyclerView() {
        adapter = SessionHistoryAdapter(
            onItemClick = { session ->
                val action = SessionHistoryFragmentDirections
                    .actionMetricsSessionHistoryToDetail(session.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { session -> showDeleteConfirmation(session.id) }
        )
        binding.rvWorkoutHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SessionHistoryFragment.adapter
        }
    }

    private fun setupObservers() {
        viewModel.historyState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.rvWorkoutHistory.isVisible = false
                    binding.layoutEmpty.isVisible = false
                    binding.tvError.isVisible = false
                }
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    val sessions = resource.data?.content.orEmpty()
                    binding.tvSessionCount.text = "${sessions.size}"
                    if (sessions.isEmpty()) {
                        binding.rvWorkoutHistory.isVisible = false
                        binding.layoutEmpty.isVisible = true
                        binding.tvError.isVisible = false
                    } else {
                        binding.rvWorkoutHistory.isVisible = true
                        binding.layoutEmpty.isVisible = false
                        binding.tvError.isVisible = false
                        adapter.submitList(sessions)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    binding.rvWorkoutHistory.isVisible = false
                    binding.layoutEmpty.isVisible = false
                    binding.tvError.isVisible = true
                    binding.tvError.text = resource.message ?: "Error al cargar historial"
                }
            }
        }

        viewModel.totalVolumeState.observe(viewLifecycleOwner) { resource ->
            binding.tvTotalVolume.text = when (resource) {
                is Resource.Success -> "${String.format("%.1f", resource.data ?: 0.0)} kg"
                else -> "-- kg"
            }
        }

        viewModel.deleteState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                Toast.makeText(requireContext(), "Sesión eliminada", Toast.LENGTH_SHORT).show()
            } else if (resource is Resource.Error) {
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadHistory()
            viewModel.loadTotalVolume()
            binding.swipeRefresh.isRefreshing = false
        }
        binding.btnFilter.setOnClickListener {
            MetricsHistoryFilterBottomSheet.show(childFragmentManager) { filter ->
                viewModel.loadHistory(filter)
            }
        }
    }

    private fun showDeleteConfirmation(sessionId: Long) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar sesión")
            .setMessage("¿Estás seguro de eliminar esta sesión?")
            .setPositiveButton("Eliminar") { _, _ -> viewModel.deleteWorkoutSession(sessionId) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class SessionHistoryViewModelFactory(
    private val context: android.content.Context
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionHistoryViewModel::class.java)) {
            return SessionHistoryViewModel.create(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
