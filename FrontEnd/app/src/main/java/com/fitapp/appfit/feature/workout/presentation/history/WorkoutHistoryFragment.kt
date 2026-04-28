package com.fitapp.appfit.feature.workout.presentation.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentWorkoutHistoryBinding
import com.fitapp.appfit.feature.workout.data.repository.WorkoutRepositoryImpl

class WorkoutHistoryFragment : Fragment() {

    private var _binding: FragmentWorkoutHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: WorkoutHistoryViewModel
    private lateinit var adapter: WorkoutHistoryAdapter

    companion object {
        private const val TAG = "WorkoutHistoryFragment"
    }

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

        Log.i(TAG, "WORKOUT_HISTORY_FRAGMENT_CREATED")

        val repository = WorkoutRepositoryImpl(requireContext())
        val factory = WorkoutHistoryViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[WorkoutHistoryViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupListeners()

        viewModel.loadWorkoutHistory()
        viewModel.loadTotalVolume()
    }

    private fun setupRecyclerView() {
        adapter = WorkoutHistoryAdapter(
            onItemClick = { session ->
                Log.d(TAG, "SESSION_CLICKED | sessionId=${session.id}")
                val action =
                    WorkoutHistoryFragmentDirections.actionWorkoutHistoryToDetail(session.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { session ->
                Log.d(TAG, "DELETE_CLICKED | sessionId=${session.id}")
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
            Log.d(TAG, "HISTORY_STATE_CHANGED | state=${resource.javaClass.simpleName}")

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
                    Log.i(TAG, "✅ SESSIONS_LOADED | count=${sessions.size}")

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
                    Log.e(TAG, "❌ HISTORY_ERROR | error=${resource.message}")
                    binding.progressBar.isVisible = false
                    binding.rvWorkoutHistory.isVisible = false
                    binding.layoutEmpty.isVisible = false
                    binding.tvError.isVisible = true
                    binding.tvError.text = resource.message ?: "Error al cargar historial"
                }
            }
        }

        viewModel.totalVolumeState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val volume = resource.data ?: 0.0
                    Log.i(TAG, "✅ TOTAL_VOLUME | volume=$volume")
                    binding.tvTotalVolume?.text = "${String.format("%.1f", volume)} kg"
                }
                else -> {
                    binding.tvTotalVolume?.text = "-- kg"
                }
            }
        }

        viewModel.deleteState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    Log.d(TAG, "DELETE_LOADING")
                }
                is Resource.Success -> {
                    Log.i(TAG, "✅ SESSION_DELETED_SUCCESS")
                    Toast.makeText(
                        requireContext(),
                        "Sesión eliminada",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.loadWorkoutHistory() // Recargar lista
                }
                is Resource.Error -> {
                    Log.e(TAG, "❌ DELETE_ERROR | error=${resource.message}")
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
        binding.swipeRefresh?.setOnRefreshListener {
            Log.d(TAG, "SWIPE_REFRESH")
            viewModel.loadWorkoutHistory()
            viewModel.loadTotalVolume()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.btnFilter?.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Filtros - próximamente",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showDeleteConfirmation(sessionId: Long) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar sesión")
            .setMessage("¿Estás seguro de eliminar esta sesión? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                Log.i(TAG, "DELETE_CONFIRMED | sessionId=$sessionId")
                viewModel.deleteWorkoutSession(sessionId)
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                Log.d(TAG, "DELETE_CANCELLED | sessionId=$sessionId")
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}