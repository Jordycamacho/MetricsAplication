package com.fitapp.appfit.feature.routine.ui.sets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentRoutineSetsBinding
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse
import com.fitapp.appfit.feature.routine.ui.RoutineSetTemplateViewModel
import com.fitapp.appfit.feature.routine.ui.sets.SetTemplateAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class RoutineSetsFragment : Fragment() {

    private var _binding: FragmentRoutineSetsBinding? = null
    private val binding get() = _binding!!

    private val args: RoutineSetsFragmentArgs by navArgs()
    private val viewModel: RoutineSetTemplateViewModel by viewModels()
    private lateinit var adapter: SetTemplateAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoutineSetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupFab()

        viewModel.loadSets(args.routineExerciseId)
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupRecyclerView() {
        adapter = SetTemplateAdapter(
            onEditClick = { set -> navigateToEdit(set) },
            onDeleteClick = { set -> confirmDelete(set) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAddSet.setOnClickListener {
            val action = RoutineSetsFragmentDirections.actionRoutineSetsToAddEditSet(
                routineExerciseId = args.routineExerciseId,
                exerciseId = args.exerciseId,
                setId = -1L
            )
            findNavController().navigate(action)
        }
    }

    // ── Observers ─────────────────────────────────────────────────────────────

    private fun setupObservers() {
        viewModel.setsState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading()
                is Resource.Success -> {
                    hideLoading()
                    val list = resource.data ?: emptyList()
                    adapter.submitList(list)
                    binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    binding.recyclerView.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                }
                is Resource.Error -> {
                    hideLoading()
                    Snackbar.make(binding.root, resource.message ?: "Error cargando sets", Snackbar.LENGTH_LONG).show()
                }
                else -> {}
            }
        }

        viewModel.deleteState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    Snackbar.make(binding.root, "Set eliminado", Snackbar.LENGTH_SHORT).show()
                    viewModel.loadSets(args.routineExerciseId)
                }
                is Resource.Error ->
                    Snackbar.make(binding.root, resource.message ?: "Error al eliminar", Snackbar.LENGTH_LONG).show()
                else -> {}
            }
            if (resource != null) viewModel.clearDeleteState()
        }
    }

    // ── Navegación ────────────────────────────────────────────────────────────

    private fun navigateToEdit(set: RoutineSetTemplateResponse) {
        val action = RoutineSetsFragmentDirections.actionRoutineSetsToAddEditSet(
            routineExerciseId = args.routineExerciseId,
            exerciseId = args.exerciseId,
            setId = set.id
        )
        findNavController().navigate(action)
    }

    private fun confirmDelete(set: RoutineSetTemplateResponse) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar set")
            .setMessage("¿Eliminar Set ${set.position}?")
            .setPositiveButton("Eliminar") { _, _ -> viewModel.deleteSet(set.id) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}