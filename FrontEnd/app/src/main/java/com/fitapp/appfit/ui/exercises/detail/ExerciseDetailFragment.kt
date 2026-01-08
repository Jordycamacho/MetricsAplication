package com.fitapp.appfit.ui.exercises.detail

import android.os.Bundle
import android.util.Log
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
import com.fitapp.appfit.utils.DateUtils
import com.fitapp.appfit.utils.Resource
import com.google.android.material.chip.Chip

class ExerciseDetailFragment : Fragment() {

    private var _binding: FragmentExerciseDetailBinding? = null
    private val binding get() = _binding!!
    private val exerciseViewModel: ExerciseViewModel by viewModels()
    private val args: ExerciseDetailFragmentArgs by navArgs()

    companion object {
        private const val TAG = "ExerciseDetailFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i(TAG, "onCreateView: Detalle del ejercicio ${args.exerciseId}")
        _binding = FragmentExerciseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "onViewCreated: Configurando vista de detalle")

        setupToolbar()
        setupClickListeners()
        setupObservers()
        loadExerciseDetails()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.toolbar.title = "Detalle del Ejercicio"
    }

    private fun setupClickListeners() {
        binding.btnEdit.setOnClickListener {
            navigateToEdit()
        }

        binding.btnToggleStatus.setOnClickListener {
            toggleExerciseStatus()
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
                    resource.data?.let { exercise ->
                        Log.i(TAG, "setupObservers: Ejercicio cargado exitosamente")
                        displayExerciseDetails(exercise)
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    Log.e(TAG, "setupObservers: Error al cargar ejercicio: ${resource.message}")
                    showError("Error al cargar ejercicio: ${resource.message}")
                }
                is Resource.Loading -> {
                    Log.d(TAG, "setupObservers: Cargando ejercicio...")
                    showLoading()
                }
                else -> {}
            }
        }

        // Observar estado de cambios
        exerciseViewModel.toggleExerciseState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    Log.i(TAG, "setupObservers: Estado cambiado exitosamente")
                    loadExerciseDetails() // Recargar datos
                }
                is Resource.Error -> {
                    Log.e(TAG, "setupObservers: Error cambiando estado: ${resource.message}")
                    showError("Error cambiando estado: ${resource.message}")
                }
                else -> {}
            }
        }

        exerciseViewModel.deleteExerciseState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    Log.i(TAG, "setupObservers: Ejercicio eliminado")
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    Log.e(TAG, "setupObservers: Error eliminando ejercicio: ${resource.message}")
                    showError("Error eliminando ejercicio: ${resource.message}")
                }
                else -> {}
            }
        }

        exerciseViewModel.makePublicState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    Log.i(TAG, "setupObservers: Ejercicio hecho público")
                    loadExerciseDetails() // Recargar datos
                }
                is Resource.Error -> {
                    Log.e(TAG, "setupObservers: Error haciendo público: ${resource.message}")
                    showError("Error haciendo público: ${resource.message}")
                }
                else -> {}
            }
        }
    }

    private fun loadExerciseDetails() {
        Log.d(TAG, "loadExerciseDetails: Solicitando detalles del ejercicio ${args.exerciseId}")
        exerciseViewModel.getExerciseByIdWithRelations(args.exerciseId)
    }

    private fun displayExerciseDetails(exercise: com.fitapp.appfit.response.exercise.response.ExerciseResponse) {
        Log.d(TAG, "displayExerciseDetails: Mostrando detalles del ejercicio ${exercise.id}")

        // Información básica
        binding.tvExerciseName.text = exercise.name
        binding.tvExerciseType.text = exercise.exerciseType?.name ?: "SIN TIPO"
        binding.tvExerciseDescription.text = exercise.description ?: "Sin descripción"
        binding.tvSport.text = exercise.sportName ?: "Sin deporte"
        binding.tvCreator.text = exercise.createdByEmail ?: "Usuario desconocido"

        // Estado
        if (exercise.isActive == true) {
            binding.tvStatus.text = "✅ ACTIVO"
            binding.btnToggleStatus.text = "DESACTIVAR"
        } else {
            binding.tvStatus.text = "❌ INACTIVO"
            binding.btnToggleStatus.text = "ACTIVAR"
        }

        // Estadísticas
        binding.tvUsage.text = "Usado ${exercise.usageCount ?: 0} veces"
        binding.tvRating.text = "⭐ ${exercise.rating ?: 0.0} (${exercise.ratingCount ?: 0} votos)"

        // Fechas
        binding.tvCreatedAt.text = "Creado: ${DateUtils.formatForDisplay(exercise.createdAt)}"
        binding.tvUpdatedAt.text = "Actualizado: ${DateUtils.formatForDisplay(exercise.updatedAt)}"
        binding.tvLastUsed.text = "Último uso: ${DateUtils.formatForDisplay(exercise.lastUsedAt)}"

        // Configurar chips de categorías
        setupCategoryChips(exercise.categoryNames)

        // Configurar chips de parámetros
        setupParameterChips(exercise.supportedParameterNames)

        // Mostrar botón de editar si el usuario es el creador (esto es una simplificación)
        // En una app real deberías verificar el usuario actual
        val canEdit = exercise.createdByEmail != null // Ejemplo simple
        binding.btnEdit.visibility = if (canEdit) View.VISIBLE else View.GONE
    }

    private fun setupCategoryChips(categoryNames: Set<String>) {
        binding.chipGroupCategories.removeAllViews()

        if (categoryNames.isEmpty()) {
            binding.tvNoCategories.visibility = View.VISIBLE
            binding.chipGroupCategories.visibility = View.GONE
        } else {
            binding.tvNoCategories.visibility = View.GONE
            binding.chipGroupCategories.visibility = View.VISIBLE

            categoryNames.forEach { categoryName ->
                val chip = Chip(requireContext()).apply {
                    text = categoryName
                    isCheckable = false
                    isClickable = false
                    chipBackgroundColor = resources.getColorStateList(com.fitapp.appfit.R.color.gold_primary, null)
                    setTextColor(resources.getColor(com.fitapp.appfit.R.color.white, null))
                    chipStrokeWidth = 0f
                }
                binding.chipGroupCategories.addView(chip)
            }
        }
    }

    private fun setupParameterChips(parameterNames: Set<String>) {
        binding.chipGroupParameters.removeAllViews()

        if (parameterNames.isEmpty()) {
            binding.tvNoParameters.visibility = View.VISIBLE
            binding.chipGroupParameters.visibility = View.GONE
        } else {
            binding.tvNoParameters.visibility = View.GONE
            binding.chipGroupParameters.visibility = View.VISIBLE

            parameterNames.forEach { paramName ->
                val chip = Chip(requireContext()).apply {
                    text = paramName
                    isCheckable = false
                    isClickable = false
                    chipBackgroundColor = resources.getColorStateList(com.fitapp.appfit.R.color.blue_500, null)
                    setTextColor(resources.getColor(com.fitapp.appfit.R.color.white, null))
                    chipStrokeWidth = 0f
                }
                binding.chipGroupParameters.addView(chip)
            }
        }
    }

    private fun navigateToEdit() {
        Log.d(TAG, "navigateToEdit: Navegando a editar ejercicio ${args.exerciseId}")

        // Opción 1: Usando Safe Args (recomendado)
        try {
            val action = ExerciseDetailFragmentDirections.actionExerciseDetailToEditExercise(args.exerciseId)
            findNavController().navigate(action)
        } catch (e: Exception) {
            Log.e(TAG, "Error usando Safe Args: ${e.message}")

            // Opción 2: Fallback con Bundle
            val bundle = Bundle().apply {
                putLong("exerciseId", args.exerciseId)
            }
            findNavController().navigate(
                R.id.action_exercise_detail_to_edit_exercise,
                bundle
            )
        }
    }

    private fun toggleExerciseStatus() {
        Log.d(TAG, "toggleExerciseStatus: Cambiando estado del ejercicio ${args.exerciseId}")
        exerciseViewModel.toggleExerciseStatus(args.exerciseId)
    }

    private fun showDeleteConfirmation() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar ejercicio")
            .setMessage("¿Estás seguro de que quieres eliminar este ejercicio? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                exerciseViewModel.deleteExercise(args.exerciseId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun makeExercisePublic() {
        Log.d(TAG, "makeExercisePublic: Haciendo público ejercicio ${args.exerciseId}")
        exerciseViewModel.makeExercisePublic(args.exerciseId)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.contentContainer.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.contentContainer.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        // Aquí podrías mostrar un Snackbar o un mensaje en la interfaz
        Log.e(TAG, "showError: $message")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "onDestroyView: Destruyendo vista")
        exerciseViewModel.clearDetailState()
        _binding = null
    }
}