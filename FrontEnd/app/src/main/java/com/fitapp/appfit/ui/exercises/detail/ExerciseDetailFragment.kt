package com.fitapp.appfit.ui.exercises.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fitapp.appfit.R
import com.fitapp.appfit.databinding.FragmentExerciseDetailBinding
import com.fitapp.appfit.model.ExerciseViewModel
import com.fitapp.appfit.response.exercise.response.ExerciseResponse
import com.fitapp.appfit.utils.DateUtils
import com.fitapp.appfit.utils.Resource
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ExerciseDetailFragment : Fragment() {

    private var _binding: FragmentExerciseDetailBinding? = null
    private val binding get() = _binding!!
    private val exerciseViewModel: ExerciseViewModel by viewModels()
    private val args: ExerciseDetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExerciseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupClickListeners()
        setupObservers()
        loadExerciseDetails()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.toolbar.title = "Detalle del ejercicio"
    }

    private fun setupClickListeners() {
        binding.btnEdit.setOnClickListener { navigateToEdit() }
        binding.btnToggleStatus.setOnClickListener { toggleExerciseStatus() }
        binding.btnDelete.setOnClickListener { showDeleteConfirmation() }
    }

    private fun setupObservers() {
        exerciseViewModel.exerciseDetailState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    resource.data?.let { displayExerciseDetails(it) }
                }
                is Resource.Error -> {
                    hideLoading()
                    showError(resource.message ?: "Error al cargar el ejercicio")
                }
                is Resource.Loading -> showLoading()
                else -> {}
            }
        }

        exerciseViewModel.toggleExerciseState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    exerciseViewModel.clearToggleState()
                    loadExerciseDetails()
                }
                is Resource.Error -> showError(resource.message ?: "Error al cambiar estado")
                else -> {}
            }
        }

        exerciseViewModel.deleteExerciseState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    exerciseViewModel.clearDeleteState()
                    findNavController().navigateUp()
                }
                is Resource.Error -> showError(resource.message ?: "Error al eliminar")
                else -> {}
            }
        }

        exerciseViewModel.makePublicState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    exerciseViewModel.clearMakePublicState()
                    loadExerciseDetails()
                }
                is Resource.Error -> showError(resource.message ?: "Error")
                else -> {}
            }
        }
    }

    private fun loadExerciseDetails() {
        // ✅ CAMBIADO: getExerciseByIdWithRelations → getExerciseById (ya devuelve todo)
        exerciseViewModel.getExerciseById(args.exerciseId)
    }

    private fun displayExerciseDetails(exercise: ExerciseResponse) {
        binding.tvExerciseName.text = exercise.name
        binding.tvExerciseType.text = exercise.exerciseType?.name ?: "SIN TIPO"
        binding.tvExerciseDescription.text = exercise.description ?: "Sin descripción"

        // ✅ CAMBIADO: sportName → sports Map, mostramos todos los deportes
        binding.tvSport.text = exercise.sports.values.joinToString(", ").ifEmpty { "—" }

        binding.tvVisibility.text = if (exercise.isPublic == true) "Público" else "Personal"
        binding.tvVisibility.setTextColor(
            if (exercise.isPublic == true)
                resources.getColor(R.color.gold_primary, null)
            else
                resources.getColor(R.color.text_secondary_dark, null)
        )

        val isActive = exercise.isActive == true
        binding.tvStatus.text = if (isActive) "Activo" else "Inactivo"
        binding.tvStatus.setTextColor(
            if (isActive) resources.getColor(R.color.gold_primary, null)
            else resources.getColor(R.color.text_secondary_dark, null)
        )
        binding.btnToggleStatus.text = if (isActive) "Pausar" else "Activar"

        binding.tvUsage.text = (exercise.usageCount ?: 0).toString()
        binding.tvRating.text = String.format("%.1f", exercise.rating ?: 0.0)

        binding.tvCreatedAt.text = "Creado: ${DateUtils.formatForDisplay(exercise.createdAt)}"
        binding.tvUpdatedAt.text = "Actualizado: ${DateUtils.formatForDisplay(exercise.updatedAt)}"
        binding.tvLastUsed.text = "Último uso: ${
            if (!exercise.lastUsedAt.isNullOrEmpty()) DateUtils.formatForDisplay(exercise.lastUsedAt)
            else "Nunca"
        }"

        setupChips(binding.chipGroupCategories, exercise.categoryNames.toList(), binding.tvNoCategories)
        setupChips(binding.chipGroupParameters, exercise.supportedParameterNames.toList(), binding.tvNoParameters)

        binding.btnEdit.visibility = if (exercise.isPublic == false) View.VISIBLE else View.GONE
        binding.btnDelete.visibility = if (exercise.isPublic == false) View.VISIBLE else View.GONE
    }

    private fun setupChips(
        chipGroup: com.google.android.material.chip.ChipGroup,
        items: List<String>,
        emptyView: android.widget.TextView
    ) {
        chipGroup.removeAllViews()
        if (items.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            chipGroup.visibility = View.GONE
            return
        }
        emptyView.visibility = View.GONE
        chipGroup.visibility = View.VISIBLE
        items.forEach { label ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = false
                isClickable = false
                setChipBackgroundColorResource(R.color.gold_primary)
                setTextColor(resources.getColor(android.R.color.black, null))
                chipStrokeWidth = 0f
                textSize = 12f
            }
            chipGroup.addView(chip)
        }
    }

    private fun navigateToEdit() {
        try {
            val action = ExerciseDetailFragmentDirections.actionExerciseDetailToEditExercise(args.exerciseId)
            findNavController().navigate(action)
        } catch (e: Exception) {
            findNavController().navigate(
                R.id.action_exercise_detail_to_edit_exercise,
                Bundle().apply { putLong("exerciseId", args.exerciseId) }
            )
        }
    }

    private fun toggleExerciseStatus() {
        exerciseViewModel.toggleExerciseStatus(args.exerciseId)
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar ejercicio")
            .setMessage("Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ -> exerciseViewModel.deleteExercise(args.exerciseId) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showError(message: String) {
        com.google.android.material.snackbar.Snackbar
            .make(binding.root, message, com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
            .show()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.contentContainer.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.contentContainer.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exerciseViewModel.clearDetailState()
        _binding = null
    }
}