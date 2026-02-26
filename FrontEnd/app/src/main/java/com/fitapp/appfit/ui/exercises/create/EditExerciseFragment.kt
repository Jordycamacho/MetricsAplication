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
import androidx.navigation.fragment.navArgs
import com.fitapp.appfit.databinding.FragmentCreateExerciseBinding
import com.fitapp.appfit.model.ExerciseCategoryViewModel
import com.fitapp.appfit.model.ExerciseViewModel
import com.fitapp.appfit.model.ParameterViewModel
import com.fitapp.appfit.model.SportViewModel
import com.fitapp.appfit.response.exercise.request.ExerciseRequest
import com.fitapp.appfit.response.exercise.response.ExerciseResponse
import com.fitapp.appfit.response.exercise.response.ExerciseType
import com.fitapp.appfit.response.parameter.request.CustomParameterFilterRequest
import com.fitapp.appfit.utils.Resource
import com.google.android.material.chip.Chip

class EditExerciseFragment : Fragment() {

    private var _binding: FragmentCreateExerciseBinding? = null
    private val binding get() = _binding!!
    private val exerciseViewModel: ExerciseViewModel by viewModels()
    private val sportViewModel: SportViewModel by viewModels()
    private val parameterViewModel: ParameterViewModel by viewModels()
    private val categoryViewModel: ExerciseCategoryViewModel by viewModels()
    private val args: EditExerciseFragmentArgs by navArgs()

    companion object {
        private const val TAG = "EditExerciseFragment"
    }

    private val selectedParameterIds = mutableSetOf<Long>()
    private val selectedCategoryIds = mutableSetOf<Long>()
    private val selectedSportIds = mutableSetOf<Long>()
    private var selectedExerciseType: ExerciseType? = null
    private var originalExercise: ExerciseResponse? = null

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
        loadExerciseData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.toolbar.title = "Editar Ejercicio"
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
        binding.btnSave.text = "ACTUALIZAR EJERCICIO"
        binding.btnSave.setOnClickListener { updateExercise() }
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

        exerciseViewModel.exerciseDetailState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    resource.data?.let { exercise ->
                        originalExercise = exercise
                        populateForm(exercise)
                        loadDataForExercise(exercise)
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Error al cargar el ejercicio", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> showLoading()
                else -> {}
            }
        }

        exerciseViewModel.updateExerciseState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Ejercicio actualizado", Toast.LENGTH_SHORT).show()
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

    private fun loadExerciseData() {
        exerciseViewModel.getExerciseById(args.exerciseId)
    }

    private fun loadDataForExercise(exercise: ExerciseResponse) {
        sportViewModel.getAllSports()
        categoryViewModel.searchAllCategories()

        exercise.sports.keys.firstOrNull()?.let { sportId ->
            val filterRequest = CustomParameterFilterRequest(sportId = sportId, isActive = true)
            parameterViewModel.searchAvailableParameters(sportId, filterRequest)
        }
    }

    private fun populateForm(exercise: ExerciseResponse) {
        binding.etName.setText(exercise.name)
        binding.etDescription.setText(exercise.description ?: "")

        exercise.exerciseType?.let { exerciseType ->
            selectedExerciseType = exerciseType
            binding.spinnerExerciseType.setText(exerciseType.name, false)
        }

        selectedSportIds.clear()
        selectedSportIds.addAll(exercise.sports.keys)

        selectedParameterIds.clear()
        selectedParameterIds.addAll(exercise.supportedParameterIds)

        selectedCategoryIds.clear()
        selectedCategoryIds.addAll(exercise.categoryIds)
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

            if (selectedSportIds.contains(sportId)) selectedSportIds.remove(sportId)
            else selectedSportIds.add(sportId)

            val filterRequest = CustomParameterFilterRequest(sportId = sportId, isActive = true)
            parameterViewModel.searchAvailableParameters(sportId, filterRequest)
            updateSportsDisplay()
        }

        updateSportsDisplay()

        val exerciseSportNames = sportsMap.entries
            .filter { selectedSportIds.contains(it.value) }
            .map { it.key }
        if (exerciseSportNames.isNotEmpty()) {
            binding.spinnerSports.setText(exerciseSportNames.joinToString(", "), false)
        }
    }

    private fun updateSportsDisplay() {
        val selectedNames = sportsMap.entries
            .filter { selectedSportIds.contains(it.value) }
            .map { it.key }
        binding.spinnerSports.setText(
            if (selectedNames.isEmpty()) "" else selectedNames.joinToString(", "),
            false
        )
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

    private fun updateExercise() {
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
            sportIds = selectedSportIds.toSet(), // ✅ Set<Long>
            categoryIds = selectedCategoryIds.toSet(),
            supportedParameterIds = selectedParameterIds.toSet(),
            isPublic = originalExercise?.isPublic ?: false
        )

        Log.i(TAG, "updateExercise: ${args.exerciseId} - $request")
        exerciseViewModel.updateExercise(args.exerciseId, request)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false
        setFormEnabled(false)
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnSave.isEnabled = true
        setFormEnabled(true)
    }

    private fun setFormEnabled(enabled: Boolean) {
        binding.etName.isEnabled = enabled
        binding.etDescription.isEnabled = enabled
        binding.spinnerExerciseType.isEnabled = enabled
        binding.spinnerSports.isEnabled = enabled
        for (i in 0 until binding.chipGroupParameters.childCount)
            (binding.chipGroupParameters.getChildAt(i) as? Chip)?.isEnabled = enabled
        for (i in 0 until binding.chipGroupCategories.childCount)
            (binding.chipGroupCategories.getChildAt(i) as? Chip)?.isEnabled = enabled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exerciseViewModel.clearUpdateState()
        exerciseViewModel.clearDetailState()
        _binding = null
    }
}