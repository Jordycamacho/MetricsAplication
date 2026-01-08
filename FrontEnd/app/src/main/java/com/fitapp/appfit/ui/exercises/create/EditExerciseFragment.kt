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
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fitapp.appfit.databinding.FragmentCreateExerciseBinding
import com.fitapp.appfit.model.ExerciseCategoryViewModel
import com.fitapp.appfit.model.ExerciseViewModel
import com.fitapp.appfit.model.ParameterViewModel
import com.fitapp.appfit.model.SportViewModel
import com.fitapp.appfit.response.category.request.ExerciseCategoryFilterRequest
import com.fitapp.appfit.response.exercise.request.ExerciseRequest
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

    // Variables para selección
    private val selectedParameterIds = mutableSetOf<Long>()
    private val selectedCategoryIds = mutableSetOf<Long>()
    private var selectedSportId: Long? = null
    private var selectedExerciseType: ExerciseType? = null

    // Almacenar los datos del ejercicio original
    private var originalExercise: com.fitapp.appfit.response.exercise.response.ExerciseResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i(TAG, "onCreateView: Editando ejercicio ${args.exerciseId}")
        _binding = FragmentCreateExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "onViewCreated: Configurando vista de edición")

        setupToolbar()
        setupForm()
        setupObservers()
        loadExerciseData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.toolbar.title = "Editar Ejercicio"
    }

    private fun setupForm() {
        Log.d(TAG, "setupForm: Configurando formulario de edición")

        // Configurar spinner de tipos de ejercicio
        val exerciseTypes = ExerciseType.values().map { it.name }
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, exerciseTypes)
        binding.spinnerExerciseType.setAdapter(typeAdapter)
        binding.spinnerExerciseType.setOnItemClickListener { _, _, position, _ ->
            selectedExerciseType = ExerciseType.valueOf(exerciseTypes[position])
            Log.d(TAG, "setupForm: Tipo seleccionado: $selectedExerciseType")
        }

        // Configurar spinner de deportes
        val sportAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, emptyList<String>())
        binding.spinnerSports.setAdapter(sportAdapter)

        // Cambiar texto del botón
        binding.btnSave.text = "ACTUALIZAR EJERCICIO"

        // Botón de guardar
        binding.btnSave.setOnClickListener {
            Log.d(TAG, "setupForm: Click en actualizar")
            updateExercise()
        }
    }

    private fun setupObservers() {
        Log.d(TAG, "setupObservers: Configurando observadores")

        // Observar deportes
        sportViewModel.allSportsState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { sports ->
                        Log.i(TAG, "setupObservers: Deportes cargados: ${sports.size}")
                        updateSportsSpinner(sports)
                    }
                }
                is Resource.Error -> {
                    Log.e(TAG, "setupObservers: Error cargando deportes: ${resource.message}")
                    Toast.makeText(requireContext(), "Error al cargar deportes", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        })

        // Observar parámetros disponibles
        parameterViewModel.availableParametersState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { pageResponse ->
                        Log.i(TAG, "setupObservers: Parámetros cargados: ${pageResponse.content?.size ?: 0}")
                        pageResponse.content?.let { parameters ->
                            updateParametersChips(parameters)
                        }
                    }
                }
                is Resource.Error -> {
                    Log.e(TAG, "setupObservers: Error cargando parámetros: ${resource.message}")
                }
                else -> {}
            }
        })

        // Observar categorías
        categoryViewModel.allCategoriesState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { pageResponse ->
                        Log.i(TAG, "setupObservers: Categorías cargadas: ${pageResponse.content?.size ?: 0}")
                        pageResponse.content?.let { categories ->
                            updateCategoriesChips(categories)
                        }
                    }
                }
                is Resource.Error -> {
                    Log.e(TAG, "setupObservers: Error cargando categorías: ${resource.message}")
                    Toast.makeText(requireContext(), "Error cargando categorías", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        })

        // Observar detalle del ejercicio
        exerciseViewModel.exerciseDetailState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    resource.data?.let { exercise ->
                        Log.i(TAG, "setupObservers: Ejercicio cargado: ${exercise.id}")
                        originalExercise = exercise
                        populateForm(exercise)

                        // Cargar deportes, parámetros y categorías después de tener el ejercicio
                        loadInitialData(exercise)
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    Log.e(TAG, "setupObservers: Error cargando ejercicio: ${resource.message}")
                    Toast.makeText(requireContext(), "Error al cargar el ejercicio", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    Log.d(TAG, "setupObservers: Cargando ejercicio...")
                    showLoading()
                }
                else -> {}
            }
        })

        // Observar actualización del ejercicio
        exerciseViewModel.updateExerciseState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    Log.i(TAG, "setupObservers: Ejercicio actualizado exitosamente")
                    Toast.makeText(requireContext(), "✅ Ejercicio actualizado", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    hideLoading()
                    Log.e(TAG, "setupObservers: Error actualizando ejercicio: ${resource.message}")
                    Toast.makeText(requireContext(), "❌ Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    Log.d(TAG, "setupObservers: Actualizando ejercicio...")
                    showLoading()
                }
                else -> {}
            }
        })
    }

    private fun loadExerciseData() {
        Log.i(TAG, "loadExerciseData: Cargando datos del ejercicio ${args.exerciseId}")
        exerciseViewModel.getExerciseByIdWithRelations(args.exerciseId)
    }

    private fun loadInitialData(exercise: com.fitapp.appfit.response.exercise.response.ExerciseResponse) {
        Log.i(TAG, "loadInitialData: Cargando datos iniciales para edición")

        // Cargar deportes
        sportViewModel.getAllSports()

        // Cargar categorías
        categoryViewModel.searchAllCategories()

        // Cargar parámetros para el deporte del ejercicio
        exercise.sportId?.let { sportId ->
            val filterRequest = CustomParameterFilterRequest(
                sportId = sportId,
                isActive = true
            )
            parameterViewModel.searchAvailableParameters(sportId, filterRequest)
        }
    }

    private fun populateForm(exercise: com.fitapp.appfit.response.exercise.response.ExerciseResponse) {
        Log.d(TAG, "populateForm: Rellenando formulario con ejercicio ${exercise.id}")

        // Datos básicos
        binding.etName.setText(exercise.name)
        binding.etDescription.setText(exercise.description ?: "")

        // Tipo de ejercicio
        exercise.exerciseType?.let { exerciseType ->
            selectedExerciseType = exerciseType
            val typePosition = ExerciseType.values().indexOf(exerciseType)
            if (typePosition >= 0) {
                binding.spinnerExerciseType.setText(exerciseType.name, false)
            }
        }

        // Guardar IDs seleccionados
        selectedSportId = exercise.sportId
        selectedParameterIds.clear()
        selectedParameterIds.addAll(exercise.supportedParameterIds)
        selectedCategoryIds.clear()
        selectedCategoryIds.addAll(exercise.categoryIds)
    }

    private fun updateSportsSpinner(sports: List<com.fitapp.appfit.response.sport.response.SportResponse>) {
        Log.d(TAG, "updateSportsSpinner: Actualizando spinner con ${sports.size} deportes")

        val sportNames = sports.map { sport ->
            "${sport.name} (${if (sport.isPredefined == true) "Predefinido" else "Personalizado"})"
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sportNames)
        binding.spinnerSports.setAdapter(adapter)

        // Mapa para obtener el ID desde el nombre mostrado
        val sportsMap = mutableMapOf<String, Long>()
        sports.forEachIndexed { index, sport ->
            sportsMap[sportNames[index]] = sport.id
        }

        binding.spinnerSports.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = binding.spinnerSports.adapter.getItem(position) as String
            selectedSportId = sportsMap[selectedItem]
            Log.d(TAG, "updateSportsSpinner: Deporte seleccionado: $selectedItem -> $selectedSportId")

            // Cuando se selecciona un deporte, cargar los parámetros para ese deporte
            selectedSportId?.let { sportId ->
                val filterRequest = CustomParameterFilterRequest(
                    sportId = sportId,
                    isActive = true
                )
                parameterViewModel.searchAvailableParameters(sportId, filterRequest)
            }
        }

        // Seleccionar el deporte del ejercicio
        originalExercise?.sportId?.let { sportId ->
            val sport = sports.find { it.id == sportId }
            sport?.let {
                val sportNameToSelect = "${it.name} (${if (it.isPredefined == true) "Predefinido" else "Personalizado"})"
                binding.spinnerSports.setText(sportNameToSelect, false)
                Log.d(TAG, "updateSportsSpinner: Deporte seleccionado automáticamente: $sportNameToSelect")
            }
        }
    }

    private fun updateParametersChips(parameters: List<com.fitapp.appfit.response.parameter.response.CustomParameterResponse>) {
        Log.d(TAG, "updateParametersChips: Actualizando chips de parámetros")

        // Limpiar chips existentes
        binding.chipGroupParameters.removeAllViews()

        if (parameters.isEmpty()) {
            val chip = Chip(requireContext()).apply {
                text = "No hay parámetros disponibles para este deporte"
                isCheckable = false
                isEnabled = false
            }
            binding.chipGroupParameters.addView(chip)
            return
        }

        parameters.forEach { parameter ->
            val chip = Chip(requireContext()).apply {
                text = parameter.name
                isCheckable = true
                // Marcar como seleccionado si está en los parámetros del ejercicio original
                isChecked = selectedParameterIds.contains(parameter.id)

                setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        selectedParameterIds.add(parameter.id)
                        Log.d(TAG, "updateParametersChips: Parámetro ${parameter.id} seleccionado")
                    } else {
                        selectedParameterIds.remove(parameter.id)
                        Log.d(TAG, "updateParametersChips: Parámetro ${parameter.id} deseleccionado")
                    }
                }
            }
            binding.chipGroupParameters.addView(chip)
        }
    }

    private fun updateCategoriesChips(categories: List<com.fitapp.appfit.response.category.response.ExerciseCategoryResponse>) {
        Log.d(TAG, "updateCategoriesChips: Actualizando chips de categorías")

        // Limpiar chips existentes
        binding.chipGroupCategories.removeAllViews()

        if (categories.isEmpty()) {
            val chip = Chip(requireContext()).apply {
                text = "No hay categorías disponibles"
                isCheckable = false
                isEnabled = false
            }
            binding.chipGroupCategories.addView(chip)
            return
        }

        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category.name
                isCheckable = true
                // Marcar como seleccionado si está en las categorías del ejercicio original
                isChecked = selectedCategoryIds.contains(category.id)

                setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        selectedCategoryIds.add(category.id)
                        Log.d(TAG, "updateCategoriesChips: Categoría ${category.id} seleccionada")
                    } else {
                        selectedCategoryIds.remove(category.id)
                        Log.d(TAG, "updateCategoriesChips: Categoría ${category.id} deseleccionada")
                    }
                }
            }
            binding.chipGroupCategories.addView(chip)
        }
    }

    private fun updateExercise() {
        val name = binding.etName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        // Validaciones
        if (name.isEmpty()) {
            binding.etName.error = "El nombre es requerido"
            Log.w(TAG, "updateExercise: Nombre vacío")
            return
        }

        if (selectedExerciseType == null) {
            Toast.makeText(requireContext(), "Seleccione un tipo de ejercicio", Toast.LENGTH_SHORT).show()
            Log.w(TAG, "updateExercise: Tipo no seleccionado")
            return
        }

        if (selectedSportId == null) {
            Toast.makeText(requireContext(), "Seleccione un deporte", Toast.LENGTH_SHORT).show()
            Log.w(TAG, "updateExercise: Deporte no seleccionado")
            return
        }

        val exerciseRequest = ExerciseRequest(
            name = name,
            description = if (description.isEmpty()) null else description,
            exerciseType = selectedExerciseType!!,
            sportId = selectedSportId!!,
            categoryIds = selectedCategoryIds,
            supportedParameterIds = selectedParameterIds,
            isPublic = originalExercise?.isPublic ?: false
        )

        Log.i(TAG, "updateExercise: Actualizando ejercicio ${args.exerciseId} - $exerciseRequest")
        exerciseViewModel.updateExercise(args.exerciseId, exerciseRequest)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false
        // Deshabilitar campos del formulario
        setFormEnabled(false)
        Log.d(TAG, "showLoading: Mostrando loading")
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnSave.isEnabled = true
        // Habilitar campos del formulario
        setFormEnabled(true)
        Log.d(TAG, "hideLoading: Ocultando loading")
    }

    private fun setFormEnabled(enabled: Boolean) {
        binding.etName.isEnabled = enabled
        binding.etDescription.isEnabled = enabled
        binding.spinnerExerciseType.isEnabled = enabled
        binding.spinnerSports.isEnabled = enabled

        // Habilitar/deshabilitar chips
        for (i in 0 until binding.chipGroupParameters.childCount) {
            val child = binding.chipGroupParameters.getChildAt(i)
            if (child is Chip) {
                child.isEnabled = enabled
            }
        }

        for (i in 0 until binding.chipGroupCategories.childCount) {
            val child = binding.chipGroupCategories.getChildAt(i)
            if (child is Chip) {
                child.isEnabled = enabled
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "onDestroyView: Destruyendo vista")
        exerciseViewModel.clearUpdateState()
        exerciseViewModel.clearDetailState()
        _binding = null
    }
}