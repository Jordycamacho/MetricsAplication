package com.fitapp.appfit.feature.workout.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.R
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentWorkoutHistoryBinding
import com.fitapp.appfit.feature.workout.ui.adapter.WorkoutHistoryAdapter
import kotlinx.coroutines.launch

/**
 * Pantalla de historial de workouts.
 *
 * Muestra:
 * - Lista de sesiones completadas
 * - Filtros por rutina, fecha, performance
 * - Detalles al hacer clic
 * - Estadísticas generales
 */
class WorkoutHistoryFragment : Fragment() {

    private var _binding: FragmentWorkoutHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorkoutHistoryViewModel by viewModels()
    private lateinit var adapter: WorkoutHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupListeners()

        // Cargar historial inicial
        viewModel.loadWorkoutHistory()
        viewModel.loadTotalVolume()
    }

    private fun setupRecyclerView() {
        adapter = WorkoutHistoryAdapter(
            onItemClick = { session ->
                // TODO: Navegar a detalles de sesión
                Toast.makeText(
                    requireContext(),
                    "Detalles de sesión #${session.id}",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDeleteClick = { session ->
                showDeleteConfirmation(session.id)
            }
        )

        binding.rvWorkoutHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@WorkoutHistoryFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        // Observar historial
        viewModel.workoutHistoryState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.rvWorkoutHistory.isVisible = false
                    binding.layoutEmpty.isVisible = false
                    binding.tvError.isVisible = false
                }

                is Resource.Success -> {
                    binding.progressBar.isVisible = false

                    val sessions = resource.data?.content ?: emptyList()

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

        // Observar volumen total
        viewModel.totalVolumeState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val volume = resource.data ?: 0.0
                    binding.tvTotalVolume.text = "${String.format("%.1f", volume)} kg"
                }
                else -> {
                    binding.tvTotalVolume.text = "-- kg"
                }
            }
        }

        // Observar eliminación
        viewModel.deleteState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Mostrar loading si quieres
                }
                is Resource.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Sesión eliminada",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.loadWorkoutHistory() // Recargar lista
                }
                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Error al eliminar: ${resource.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun setupListeners() {
        // Swipe to refresh
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadWorkoutHistory()
            viewModel.loadTotalVolume()
            binding.swipeRefresh.isRefreshing = false
        }

        // Botón de filtros (opcional)
        binding.btnFilter?.setOnClickListener {
            // TODO: Mostrar diálogo de filtros
            Toast.makeText(
                requireContext(),
                "Filtros - próximamente",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showDeleteConfirmation(sessionId: Long) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar sesión")
            .setMessage("¿Estás seguro de eliminar esta sesión? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteWorkoutSession(sessionId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}