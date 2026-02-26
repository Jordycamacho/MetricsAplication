package com.fitapp.appfit.ui.exercises.create

import android.os.Bundle
import android.util.Log
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
import com.fitapp.appfit.utils.Resource
import com.google.android.material.chip.Chip

class CreateExerciseFragment : Fragment() {

    private var _binding: FragmentCreateExerciseBinding? = null
    private val binding get() = _binding!!
    private val exerciseViewModel: ExerciseViewModel by viewModels()
    private val sportViewModel: SportViewModel by viewModels()
    private val parameterViewModel: ParameterViewModel by viewModels()
    private val categoryViewModel: ExerciseCategoryViewModel by viewModels()

    companion object {
        private const val TAG = "CreateExerciseFragment"
    }

    private val selectedParameterIds = mutableSetOf<Long>()
    private val selectedCategoryIds = mutableSetOf<Long>()
    private val selectedSportIds = mutableSetOf<Long>()
    private var selectedExerciseType: ExerciseType? = null
    private val sportsMap = mutableMapOf<String, Long>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupForm()
        setupObservers()
        loadInitialData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.toolbar.title = "Crear Ejercicio"
    }

    private fun setupForm() {
        val exerciseTypes = ExerciseType.values().map { it.name }
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, exerciseTypes)
        binding.spinnerExerciseType.setAdapter(typeAdapter)
        binding.spinnerExerciseType.setOnItemClickListener { _, _, position, _ ->
            selectedExerciseType = ExerciseType.valueOf(exerciseTypes[position])
        }

        binding.spinnerSports.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, emptyList<String>())
        )

        binding.btnSave.setOnClickListener { createExercise() }
    }

    private fun loadInitialData() {
        sportViewModel.getAllSports()
        categoryViewModel.searchAllCategories()
    }

    private fun setupObservers() {
        sportViewModel.allSportsState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> resource.data?.let { updateSportsSpinner(it) }
                is Resource.Error -> Toast.makeText(requireContext(), "Error al cargar deportes", Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }

        parameterViewModel.availableParametersState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> resource.data?.content?.let { updateParametersChips(it) }
                else -> {}
            }
        }

        categoryViewModel.allCategoriesState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> resource.data?.content?.let { updateCategoriesChips(it) }
                is Resource.Error -> Toast.makeText(requireContext(), "Error cargando categorías", Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }

        exerciseViewModel.createExerciseState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Ejercicio creado", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> showLoading()
                else -> {}
            }
        }
    }

    private fun updateSportsSpinner(sports: List<com.fitapp.appfit.response.sport.response.SportResponse>) {
        val sportNames = sports.map { sport ->
            "${sport.name} (${if (sport.isPredefined == true) "Predefinido" else "Personalizado"})"
        }

        sportsMap.clear()
        sports.forEachIndexed { index, sport -> sportsMap[sportNames[index]] = sport.id }

        binding.spinnerSports.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sportNames)
        )

        binding.spinnerSports.setOnItemClickListener { _, _, position, _ ->
            val selectedName = sportNames[position]
            val sportId = sportsMap[selectedName] ?: return@setOnItemClickListener

            if (selectedSportIds.contains(sportId)) {
                selectedSportIds.remove(sportId)
                Log.d(TAG, "Deporte deseleccionado: $selectedName")
            } else {
                selectedSportIds.add(sportId)
                Log.d(TAG, "Deporte seleccionado: $selectedName")
            }

            val filterRequest = CustomParameterFilterRequest(sportId = sportId, isActive = true)
            parameterViewModel.searchAvailableParameters(sportId, filterRequest)

            updateSportsDisplay()
        }

        if (sportNames.isNotEmpty()) {
            val firstSportId = sportsMap[sportNames[0]]
            if (firstSportId != null) {
                selectedSportIds.add(firstSportId)
                binding.spinnerSports.setText(sportNames[0], false)
                updateSportsDisplay()
                val filterRequest = CustomParameterFilterRequest(sportId = firstSportId, isActive = true)
                parameterViewModel.searchAvailableParameters(firstSportId, filterRequest)
            }
        }
    }

    private fun updateSportsDisplay() {
        val selectedNames = sportsMap.entries
            .filter { selectedSportIds.contains(it.value) }
            .map { it.key }
        if (selectedNames.isEmpty()) {
            binding.spinnerSports.setText("", false)
        } else {
            binding.spinnerSports.setText(selectedNames.joinToString(", "), false)
        }
    }

    private fun updateParametersChips(parameters: List<com.fitapp.appfit.response.parameter.response.CustomParameterResponse>) {
        binding.chipGroupParameters.removeAllViews()

        if (parameters.isEmpty()) {
            binding.chipGroupParameters.addView(Chip(requireContext()).apply {
                text = "No hay parámetros disponibles para este deporte"
                isCheckable = false
                isEnabled = false
            })
            return
        }

        parameters.forEach { parameter ->
            val chip = Chip(requireContext()).apply {
                text = parameter.name
                isCheckable = true
                isChecked = selectedParameterIds.contains(parameter.id)
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) selectedParameterIds.add(parameter.id)
                    else selectedParameterIds.remove(parameter.id)
                }
            }
            binding.chipGroupParameters.addView(chip)
        }
    }

    private fun updateCategoriesChips(categories: List<com.fitapp.appfit.response.category.response.ExerciseCategoryResponse>) {
        binding.chipGroupCategories.removeAllViews()

        if (categories.isEmpty()) {
            binding.chipGroupCategories.addView(Chip(requireContext()).apply {
                text = "No hay categorías disponibles"
                isCheckable = false
                isEnabled = false
            })
            return
        }

        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category.name
                isCheckable = true
                isChecked = selectedCategoryIds.contains(category.id)
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) selectedCategoryIds.add(category.id)
                    else selectedCategoryIds.remove(category.id)
                }
            }
            binding.chipGroupCategories.addView(chip)
        }
    }

    private fun createExercise() {
        val name = binding.etName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (name.isEmpty()) { binding.etName.error = "El nombre es requerido"; return }
        if (selectedExerciseType == null) {
            Toast.makeText(requireContext(), "Seleccione un tipo de ejercicio", Toast.LENGTH_SHORT).show(); return
        }
        if (selectedSportIds.isEmpty()) {
            Toast.makeText(requireContext(), "Seleccione al menos un deporte", Toast.LENGTH_SHORT).show(); return
        }

        val request = ExerciseRequest(
            name = name,
            description = if (description.isEmpty()) null else description,
            exerciseType = selectedExerciseType!!,
            sportIds = selectedSportIds.toSet(),
            categoryIds = selectedCategoryIds.toSet(),
            supportedParameterIds = selectedParameterIds.toSet(),
            isPublic = false
        )

        Log.i(TAG, "createExercise: $request")
        exerciseViewModel.createExercise(request)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnSave.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exerciseViewModel.clearCreateState()
        _binding = null
    }
}