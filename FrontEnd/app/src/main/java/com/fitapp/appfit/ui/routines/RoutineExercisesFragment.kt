package com.fitapp.appfit.ui.routines

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.model.RoutineExerciseViewModel
import com.fitapp.appfit.databinding.FragmentRoutineExercisesBinding
import com.fitapp.appfit.response.routine.response.RoutineExerciseResponse
import com.fitapp.appfit.ui.routines.adapter.RoutineExerciseAdapter
import com.fitapp.appfit.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class RoutineExercisesFragment : Fragment() {

    private var _binding: FragmentRoutineExercisesBinding? = null
    private val binding get() = _binding!!

    private val args: RoutineExercisesFragmentArgs by navArgs()
    private val viewModel: RoutineExerciseViewModel by viewModels()
    private var routineTrainingDays: String = ""
    private lateinit var adapter: RoutineExerciseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoutineExercisesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupFab()

        viewModel.loadRoutineExercises(args.routineId)
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupRecyclerView() {
        adapter = RoutineExerciseAdapter(
            onEditClick = { exercise -> navigateToEdit(exercise) },
            onDeleteClick = { exercise -> confirmDelete(exercise) },
            onAddSetClick = { exercise -> navigateToConfigureSets(exercise) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAddExercise.setOnClickListener {
            val action = RoutineExercisesFragmentDirections
                .actionRoutineExercisesToAddExercises(args.routineId)
            findNavController().navigate(action)
        }
    }

    // ── Observers ─────────────────────────────────────────────────────────────

    private fun setupObservers() {
        viewModel.exercisesState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading()
                is Resource.Success -> {
                    hideLoading()
                    val list = resource.data ?: emptyList()
                    adapter.submitList(list)
                    showEmptyState(list.isEmpty())

                    routineTrainingDays = list
                        .mapNotNull { it.dayOfWeek }
                        .distinct()
                        .joinToString(",")
                }
                is Resource.Error -> {
                    hideLoading()
                    Snackbar.make(
                        binding.root,
                        resource.message ?: "Error cargando ejercicios",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                else -> {}
            }
        }

        viewModel.deleteState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success ->
                    Snackbar.make(binding.root, "Ejercicio eliminado", Snackbar.LENGTH_SHORT).show()
                is Resource.Error ->
                    Snackbar.make(
                        binding.root,
                        resource.message ?: "Error al eliminar",
                        Snackbar.LENGTH_LONG
                    ).show()
                else -> {}
            }
            if (resource != null) viewModel.clearDeleteState()
        }
    }

    // ── Navegación ────────────────────────────────────────────────────────────

    private fun navigateToEdit(exercise: RoutineExerciseResponse) {
        val action = RoutineExercisesFragmentDirections
            .actionRoutineExercisesToEditExercise(
                routineId         = args.routineId,
                routineExerciseId = exercise.id,
                exerciseId        = exercise.exerciseId,
                exerciseName      = exercise.exerciseName ?: "",
                dayOfWeek         = exercise.dayOfWeek ?: "",
                sessionOrder      = exercise.sessionOrder ?: 1,
                restAfterExercise = exercise.restAfterExercise ?: 60,
                trainingDays      = routineTrainingDays
            )
        findNavController().navigate(action)
    }

    private fun navigateToConfigureSets(exercise: RoutineExerciseResponse) {
        val action = RoutineExercisesFragmentDirections.actionRoutineExercisesToRoutineSets(
            routineExerciseId = exercise.id,
            exerciseId = exercise.exerciseId
        )
        findNavController().navigate(action)
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private fun confirmDelete(exercise: RoutineExerciseResponse) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar ejercicio")
            .setMessage("¿Eliminar \"${exercise.exerciseName}\" de la rutina?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteExercise(args.routineId, exercise.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showEmptyState(isEmpty: Boolean) {
        binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}