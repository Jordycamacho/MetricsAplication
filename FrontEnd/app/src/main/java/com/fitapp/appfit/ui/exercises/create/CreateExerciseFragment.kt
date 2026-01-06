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

    // Variables para paginación
    private var currentParameterPage = 0
    private var currentCategoryPage = 0
    private val parameterPageSize = 10
    private val categoryPageSize = 10

    // Mapas para selección múltiple
    private val selectedParameterIds = mutableSetOf<Long>()
    private val selectedCategoryIds = mutableSetOf<Long>()
    private var selectedSportId: Long? = null
    private var selectedExerciseType: ExerciseType? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i(TAG, "onCreateView: Creando vista de creación")
        _binding = FragmentCreateExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "onViewCreated: Configurando vista")

        setupToolbar()
        setupForm()
        setupObservers()
        loadInitialData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            Log.d(TAG, "setupToolbar: Navegando hacia atrás")
            findNavController().navigateUp()
        }
        binding.toolbar.title = "Crear Ejercicio"
    }

    private fun setupForm() {
        Log.d(TAG, "setupForm: Configurando formulario")

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

        // Botón de guardar
        binding.btnSave.setOnClickListener {
            Log.d(TAG, "setupForm: Click en guardar")
            createExercise()
        }
    }

    private fun loadInitialData() {
        Log.i(TAG, "loadInitialData: Cargando datos iniciales")

        // Cargar deportes
        sportViewModel.getAllSports()

        // NOTA: No cargamos parámetros ni categorías aquí porque necesitamos un sportId
        // Se cargarán cuando el usuario seleccione un deporte
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

        // Observar parámetros disponibles (SOLO CUANDO HAY SPORT_ID)
        parameterViewModel.availableParametersState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { pageResponse ->
                        pageResponse.content?.let { parameters ->
                            Log.i(TAG, "setupObservers: Parámetros cargados: ${parameters.size}")
                            updateParametersChips(parameters)
                        }
                    }
                }
                is Resource.Error -> {
                    Log.e(TAG, "setupObservers: Error cargando parámetros: ${resource.message}")
                    // No mostrar toast aquí porque es normal si no hay sportId seleccionado
                }
                else -> {}
            }
        })

        // Observar categorías para spinner
        categoryViewModel.categoriesForSpinnerState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { categories ->
                        Log.i(TAG, "setupObservers: Categorías cargadas: ${categories.size}")
                        updateCategoriesChips(categories)
                    }
                }
                is Resource.Error -> {
                    Log.e(TAG, "setupObservers: Error cargando categorías: ${resource.message}")
                    Toast.makeText(requireContext(), "Error cargando categorías", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        })

        // Observar creación de ejercicio
        exerciseViewModel.createExerciseState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    Log.i(TAG, "setupObservers: Ejercicio creado exitosamente")
                    Toast.makeText(requireContext(), "✅ Ejercicio creado exitosamente", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    hideLoading()
                    Log.e(TAG, "setupObservers: Error creando ejercicio: ${resource.message}")
                    Toast.makeText(requireContext(), "❌ Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    Log.d(TAG, "setupObservers: Creando ejercicio...")
                    showLoading()
                }
                else -> {}
            }
        })
    }

    private fun updateSportsSpinner(sports: List<com.fitapp.appfit.response.sport.response.SportResponse>) {
        Log.d(TAG, "updateSportsSpinner: Actualizando spinner con ${sports.size} deportes")

        val sportNames = mutableListOf<String>()
        val sportsMap = mutableMapOf<String, Long>()

        sports.forEach { sport ->
            val displayName = "${sport.name} (${if (sport.isPredefined) "Predefinido" else "Personalizado"})"
            sportNames.add(displayName)
            sportsMap[displayName] = sport.id
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sportNames)
        binding.spinnerSports.setAdapter(adapter)

        // Listener para cuando se selecciona un deporte
        binding.spinnerSports.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = binding.spinnerSports.adapter.getItem(position) as String
            selectedSportId = sportsMap[selectedItem]
            Log.d(TAG, "updateSportsSpinner: Deporte seleccionado: $selectedItem -> $selectedSportId")

        }

        if (sportNames.isNotEmpty()) {
            // Seleccionar el primer deporte por defecto
            binding.spinnerSports.setText(sportNames[0], false)
            selectedSportId = sportsMap[sportNames[0]]
        }
    }

    private fun updateParametersChips(parameters: List<com.fitapp.appfit.response.parameter.response.CustomParameterResponse>) {
        Log.d(TAG, "updateParametersChips: Actualizando chips de parámetros")

        // Limpiar chips existentes
        binding.chipGroupParameters.removeAllViews()

        if (parameters.isEmpty()) {
            // Mostrar mensaje si no hay parámetros
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
            // Mostrar mensaje si no hay categorías
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

    private fun createExercise() {
        val name = binding.etName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        // Validaciones
        if (name.isEmpty()) {
            binding.etName.error = "El nombre es requerido"
            Log.w(TAG, "createExercise: Nombre vacío")
            return
        }

        if (selectedExerciseType == null) {
            Toast.makeText(requireContext(), "Seleccione un tipo de ejercicio", Toast.LENGTH_SHORT).show()
            Log.w(TAG, "createExercise: Tipo no seleccionado")
            return
        }

        if (selectedSportId == null) {
            Toast.makeText(requireContext(), "Seleccione un deporte", Toast.LENGTH_SHORT).show()
            Log.w(TAG, "createExercise: Deporte no seleccionado")
            return
        }

        val exerciseRequest = ExerciseRequest(
            name = name,
            description = if (description.isEmpty()) null else description,
            exerciseType = selectedExerciseType!!,
            sportId = selectedSportId!!,
            categoryIds = selectedCategoryIds,
            supportedParameterIds = selectedParameterIds,
            isPublic = false // Por defecto, los ejercicios creados por usuarios son privados
        )

        Log.i(TAG, "createExercise: Enviando ejercicio - $exerciseRequest")
        exerciseViewModel.createExercise(exerciseRequest)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false
        Log.d(TAG, "showLoading: Mostrando loading")
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnSave.isEnabled = true
        Log.d(TAG, "hideLoading: Ocultando loading")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "onDestroyView: Destruyendo vista")
        exerciseViewModel.clearCreateState()
        _binding = null
    }
}