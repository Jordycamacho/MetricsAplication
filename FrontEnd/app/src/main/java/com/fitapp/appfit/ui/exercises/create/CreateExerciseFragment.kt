package com.fitapp.appfit.ui.exercises.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fitapp.appfit.databinding.FragmentCreateExerciseBinding
import com.fitapp.appfit.model.ExerciseCategoryViewModel
import com.fitapp.appfit.model.ExerciseViewModel
import com.fitapp.appfit.model.ParameterViewModel
import com.fitapp.appfit.model.SportViewModel
import com.fitapp.appfit.response.exercise.request.ExerciseRequest
import com.fitapp.appfit.response.exercise.response.ExerciseType
import com.fitapp.appfit.response.parameter.request.CustomParameterFilterRequest
import com.fitapp.appfit.ui.exercises.components.MultiSelectDropdown
import com.fitapp.appfit.utils.Resource

class CreateExerciseFragment : Fragment() {

    private var _binding: FragmentCreateExerciseBinding? = null
    private val binding get() = _binding!!

    private val exerciseViewModel:  ExerciseViewModel         by viewModels()
    private val sportViewModel:     SportViewModel            by viewModels()
    private val parameterViewModel: ParameterViewModel        by viewModels()
    private val categoryViewModel:  ExerciseCategoryViewModel by viewModels()

    private var selectedType: ExerciseType? = null

    private lateinit var sportsDropdown:     MultiSelectDropdown
    private lateinit var categoriesDropdown: MultiSelectDropdown
    private lateinit var parametersDropdown: MultiSelectDropdown

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.toolbar.title = "Crear Ejercicio"
        binding.btnSave.setOnClickListener { submit() }

        setupTypeSpinner()
        setupDropdowns()
        setupObservers()

        // Cargar datos iniciales — sports y categories en paralelo
        sportViewModel.getAllSports()
        categoryViewModel.searchAllCategories()
    }

    private fun setupTypeSpinner() {
        val types = ExerciseType.values().map { it.name }
        binding.spinnerExerciseType.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types)
        )
        binding.spinnerExerciseType.setOnItemClickListener { _, _, pos, _ ->
            selectedType = ExerciseType.valueOf(types[pos])
        }
    }

    private fun setupDropdowns() {
        sportsDropdown = MultiSelectDropdown(requireContext()).apply {
            onSelectionChanged = { sportIds ->
                // Al cambiar deporte, recargar parámetros disponibles
                val firstId = sportIds.firstOrNull()
                if (firstId != null) {
                    parameterViewModel.searchAvailableParameters(
                        firstId,
                        CustomParameterFilterRequest(sportId = firstId, isActive = true)
                    )
                } else {
                    // Sin deporte seleccionado: limpiar parámetros
                    parametersDropdown.setItems(emptyList())
                }
            }
        }
        binding.containerSports.addView(sportsDropdown)

        categoriesDropdown = MultiSelectDropdown(requireContext())
        binding.containerCategories.addView(categoriesDropdown)

        parametersDropdown = MultiSelectDropdown(requireContext())
        binding.containerParameters.addView(parametersDropdown)
    }

    private fun setupObservers() {

        // ── Deportes ─────────────────────────────────────────────────────────
        // allSportsState combina predefinidos + personales
        sportViewModel.allSportsState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    sportsDropdown.setItems(
                        resource.data?.map { MultiSelectDropdown.Item(it.id, it.name) } ?: emptyList()
                    )
                }
                is Resource.Error -> toast("Error cargando deportes")
                else -> {}
            }
        }

        // ── Categorías ────────────────────────────────────────────────────────
        // allCategoriesState devuelve ExerciseCategoryPageResponse
        categoryViewModel.allCategoriesState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    categoriesDropdown.setItems(
                        resource.data?.content?.map { MultiSelectDropdown.Item(it.id, it.name) } ?: emptyList()
                    )
                }
                is Resource.Error -> toast("Error cargando categorías")
                else -> {}
            }
        }

        // ── Parámetros ────────────────────────────────────────────────────────
        // availableParametersState se actualiza cuando el usuario selecciona un deporte
        parameterViewModel.availableParametersState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    parametersDropdown.setItems(
                        resource.data?.content?.map { MultiSelectDropdown.Item(it.id, it.name) } ?: emptyList()
                    )
                }
                else -> {}
            }
        }

        // ── Resultado crear ───────────────────────────────────────────────────
        exerciseViewModel.createExerciseState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    toast("Ejercicio creado")
                    exerciseViewModel.clearCreateState()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    hideLoading()
                    toast("Error: ${resource.message}")
                    exerciseViewModel.clearCreateState()
                }
                is Resource.Loading -> showLoading()
                else -> {}
            }
        }
    }

    private fun submit() {
        val name = binding.etName.text.toString().trim()
        val desc = binding.etDescription.text.toString().trim()

        if (name.isEmpty())                         { binding.tilName.error = "Requerido"; return }
        binding.tilName.error = null
        if (selectedType == null)                   { toast("Selecciona un tipo de ejercicio"); return }
        if (sportsDropdown.getSelected().isEmpty()) { toast("Selecciona al menos un deporte"); return }

        exerciseViewModel.createExercise(ExerciseRequest(
            name                  = name,
            description           = desc.ifEmpty { null },
            exerciseType          = selectedType!!,
            sportIds              = sportsDropdown.getSelected(),
            categoryIds           = categoriesDropdown.getSelected(),
            supportedParameterIds = parametersDropdown.getSelected(),
            isPublic              = false
        ))
    }

    private fun showLoading() { binding.progressBar.visibility = View.VISIBLE;  binding.btnSave.isEnabled = false }
    private fun hideLoading() { binding.progressBar.visibility = View.GONE;     binding.btnSave.isEnabled = true }
    private fun toast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        exerciseViewModel.clearCreateState()
        _binding = null
    }
}