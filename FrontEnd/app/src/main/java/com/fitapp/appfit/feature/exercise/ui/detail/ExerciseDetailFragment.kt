package com.fitapp.appfit.feature.exercise.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fitapp.appfit.R
import com.fitapp.appfit.core.util.DateUtils
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentExerciseDetailBinding
import com.fitapp.appfit.feature.exercise.model.exercise.response.ExerciseResponse
import com.fitapp.appfit.feature.exercise.ExerciseViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class ExerciseDetailFragment : Fragment() {

    private var _binding: FragmentExerciseDetailBinding? = null
    private val binding get() = _binding!!
    private val exerciseViewModel: ExerciseViewModel by viewModels()
    private val args: ExerciseDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupClickListeners()
        setupObservers()
        exerciseViewModel.getExerciseById(args.exerciseId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.toolbar.title = "Detalle del ejercicio"
    }

    private fun setupClickListeners() {
        binding.btnEdit.setOnClickListener {
            findNavController().navigate(
                ExerciseDetailFragmentDirections.actionExerciseDetailToEditExercise(args.exerciseId)
            )
        }
        binding.btnToggleStatus.setOnClickListener {
            exerciseViewModel.toggleExerciseStatus(args.exerciseId)
        }
        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun setupObservers() {
        exerciseViewModel.exerciseDetailState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    resource.data?.let { displayExercise(it) }
                }
                is Resource.Error -> {
                    hideLoading()
                    showError("Error al cargar: ${resource.message}")
                }
                is Resource.Loading -> showLoading()
                else -> {}
            }
        }

        exerciseViewModel.toggleStatusState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success<Unit> -> {
                    exerciseViewModel.clearToggleState()
                    exerciseViewModel.getExerciseById(args.exerciseId)
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
    }

    private fun displayExercise(exercise: ExerciseResponse) {
        binding.tvExerciseName.text        = exercise.name
        binding.tvExerciseType.text        = exercise.exerciseType?.name ?: "SIN TIPO"
        binding.tvExerciseDescription.text = exercise.description?.ifEmpty { "Sin descripción" } ?: "Sin descripción"

        binding.tvSport.text = exercise.sports.values
            .joinToString(", ")
            .ifEmpty { "—" }

        binding.tvCreator.text = exercise.createdById
            ?.let { "Usuario #$it" }
            ?: "—"

        val isPublic = exercise.isPublic == true
        binding.tvVisibility.text = if (isPublic) "Público" else "Personal"
        binding.tvVisibility.setTextColor(
            resources.getColor(
                if (isPublic) R.color.gold_primary else R.color.text_secondary_dark, null
            )
        )

        val isActive = exercise.isActive == true
        binding.tvStatus.text = if (isActive) "Activo" else "Inactivo"
        binding.tvStatus.setTextColor(
            resources.getColor(
                if (isActive) R.color.gold_primary else R.color.text_secondary_dark, null
            )
        )
        binding.btnToggleStatus.text = if (isActive) "Desactivar" else "Activar"

        binding.tvUsage.text  = (exercise.usageCount ?: 0).toString()
        binding.tvRating.text = String.format("%.1f", exercise.rating ?: 0.0)

        binding.tvCreatedAt.text = "Creado: ${DateUtils.formatForDisplay(exercise.createdAt)}"
        binding.tvUpdatedAt.text = "Actualizado: ${DateUtils.formatForDisplay(exercise.updatedAt)}"
        binding.tvLastUsed.text  = "Último uso: ${
            if (!exercise.lastUsedAt.isNullOrEmpty())
                DateUtils.formatForDisplay(exercise.lastUsedAt)
            else "Nunca"
        }"

        setupChips(
            binding.chipGroupCategories,
            exercise.categoryNames.toList(),
            binding.tvNoCategories
        )

        setupChips(
            binding.chipGroupParameters,
            exercise.supportedParameterNames.toList(),
            binding.tvNoParameters
        )

        val canEdit = exercise.isPublic == false
        binding.btnEdit.visibility   = if (canEdit) View.VISIBLE else View.GONE
        binding.btnDelete.visibility = if (canEdit) View.VISIBLE else View.GONE
        binding.btnToggleStatus.visibility = if (canEdit) View.VISIBLE else View.GONE
    }

    private fun setupChips(
        chipGroup: ChipGroup,
        items: List<String>,
        emptyLabel: TextView
    ) {
        chipGroup.removeAllViews()
        if (items.isEmpty()) {
            emptyLabel.visibility = View.VISIBLE
            chipGroup.visibility  = View.GONE
        } else {
            emptyLabel.visibility = View.GONE
            chipGroup.visibility  = View.VISIBLE
            items.forEach { label ->
                chipGroup.addView(Chip(requireContext()).apply {
                    text           = label
                    isCheckable    = false
                    isClickable    = false
                    chipStrokeWidth = 1f
                    setChipBackgroundColorResource(R.color.surface_dark)
                    setTextColor(resources.getColor(R.color.gold_primary, null))
                    setChipStrokeColorResource(R.color.gold_primary)
                })
            }
        }
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar ejercicio")
            .setMessage("Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                exerciseViewModel.deleteExercise(args.exerciseId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showLoading() {
        binding.progressBar.visibility    = View.VISIBLE
        binding.contentContainer.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility    = View.GONE
        binding.contentContainer.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exerciseViewModel.clearDetailState()
        _binding = null
    }
}