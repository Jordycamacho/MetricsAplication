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
import androidx.navigation.fragment.navArgs
import com.fitapp.appfit.databinding.FragmentEditExerciseBinding
import com.fitapp.appfit.model.ExerciseCategoryViewModel
import com.fitapp.appfit.model.ExerciseViewModel
import com.fitapp.appfit.model.ParameterViewModel
import com.fitapp.appfit.model.SportViewModel
import com.fitapp.appfit.response.exercise.request.ExerciseRequest
import com.fitapp.appfit.response.exercise.response.ExerciseResponse
import com.fitapp.appfit.response.exercise.response.ExerciseType
import com.fitapp.appfit.response.parameter.request.CustomParameterFilterRequest
import com.fitapp.appfit.ui.exercises.components.MultiSelectDropdown
import com.fitapp.appfit.utils.Resource

class EditExerciseFragment : Fragment() {

    private var _binding: FragmentEditExerciseBinding? = null
    private val binding get() = _binding!!
    private val args: EditExerciseFragmentArgs by navArgs()

    private val exerciseViewModel:  ExerciseViewModel         by viewModels()
    private val sportViewModel:     SportViewModel            by viewModels()
    private val parameterViewModel: ParameterViewModel        by viewModels()
    private val categoryViewModel:  ExerciseCategoryViewModel by viewModels()

    private var selectedType: ExerciseType?         = null
    private var originalExercise: ExerciseResponse? = null

    private lateinit var sportsDropdown:     MultiSelectDropdown
    private lateinit var categoriesDropdown: MultiSelectDropdown
    private lateinit var parametersDropdown: MultiSelectDropdown

    // Sincronización: setSelected() solo funciona después de setItems()
    // Esperamos a que AMBOS (items + ejercicio) hayan llegado antes de pre-seleccionar
    private var sportsItemsLoaded     = false
    private var categoriesItemsLoaded = false
    private var exerciseLoaded        = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // ✅ Ahora infla el layout correcto: fragment_edit_exercise.xml
        _binding = FragmentEditExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.toolbar.title = "Editar Ejercicio"
        binding.btnSave.setOnClickListener { submit() }

        setupTypeSpinner()
        setupDropdowns()
        setupObservers()

        // Lanzar las tres cargas en paralelo — cada observer reacciona cuando llega
        showLoading()
        exerciseViewModel.getExerciseById(args.exerciseId)
        sportViewModel.getAllSports()
        categoryViewModel.searchAllCategories()
    }

    // ── Spinner tipo ──────────────────────────────────────────────────────────

    private fun setupTypeSpinner() {
        val types = ExerciseType.values().map { it.name }
        binding.spinnerExerciseType.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types)
        )
        binding.spinnerExerciseType.setOnItemClickListener { _, _, pos, _ ->
            selectedType = ExerciseType.valueOf(types[pos])
        }
    }

    // ── Dropdowns ─────────────────────────────────────────────────────────────

    private fun setupDropdowns() {
        sportsDropdown = MultiSelectDropdown(requireContext()).apply {
            onSelectionChanged = { sportIds ->
                // Al cambiar deporte recargamos parámetros disponibles
                val firstId = sportIds.firstOrNull()
                if (firstId != null) {
                    parameterViewModel.searchAvailableParameters(
                        firstId,
                        CustomParameterFilterRequest(sportId = firstId, isActive = true)
                    )
                } else {
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

    // ── Observers ─────────────────────────────────────────────────────────────

    private fun setupObservers() {

        // Datos del ejercicio a editar
        exerciseViewModel.exerciseDetailState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { exercise ->
                        originalExercise = exercise
                        exerciseLoaded   = true

                        // Campos de texto — se pueden rellenar inmediatamente
                        binding.etName.setText(exercise.name)
                        binding.etDescription.setText(exercise.description ?: "")
                        exercise.exerciseType?.let { type ->
                            selectedType = type
                            binding.spinnerExerciseType.setText(type.name, false)
                        }

                        // Dropdowns — solo si sus items ya cargaron
                        tryPreselectSports()
                        tryPreselectCategories()

                        // Cargar parámetros del primer deporte del ejercicio
                        exercise.sportIds().firstOrNull()?.let { sportId ->
                            parameterViewModel.searchAvailableParameters(
                                sportId,
                                CustomParameterFilterRequest(sportId = sportId, isActive = true)
                            )
                        }
                    }
                    hideLoading()
                }
                is Resource.Error -> {
                    hideLoading()
                    toast("Error al cargar ejercicio: ${resource.message}")
                }
                is Resource.Loading -> { /* ya está mostrando loading */ }
                else -> {}
            }
        }

        // Lista de deportes
        sportViewModel.allSportsState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    sportsDropdown.setItems(
                        resource.data?.map { MultiSelectDropdown.Item(it.id, it.name) } ?: emptyList()
                    )
                    sportsItemsLoaded = true
                    tryPreselectSports()
                }
                is Resource.Error -> toast("Error cargando deportes")
                else -> {}
            }
        }

        // Lista de categorías
        categoryViewModel.allCategoriesState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    categoriesDropdown.setItems(
                        resource.data?.content?.map { MultiSelectDropdown.Item(it.id, it.name) } ?: emptyList()
                    )
                    categoriesItemsLoaded = true
                    tryPreselectCategories()
                }
                is Resource.Error -> toast("Error cargando categorías")
                else -> {}
            }
        }

        // Parámetros — se actualiza cuando se selecciona un deporte
        parameterViewModel.availableParametersState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    parametersDropdown.setItems(
                        resource.data?.content?.map { MultiSelectDropdown.Item(it.id, it.name) } ?: emptyList()
                    )
                    // Pre-seleccionar parámetros previos del ejercicio si ya cargó
                    originalExercise?.let { ex ->
                        if (ex.supportedParameterIds.isNotEmpty()) {
                            parametersDropdown.setSelected(ex.supportedParameterIds)
                        }
                    }
                }
                else -> {}
            }
        }

        // Resultado de actualizar
        exerciseViewModel.updateExerciseState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    toast("Ejercicio actualizado")
                    exerciseViewModel.clearUpdateState()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    hideLoading()
                    toast("Error: ${resource.message}")
                    exerciseViewModel.clearUpdateState()
                }
                is Resource.Loading -> showLoading()
                else -> {}
            }
        }
    }

    // ── Pre-selección sincronizada ─────────────────────────────────────────────
    // Cada función espera a que TANTO los items del dropdown COMO el ejercicio
    // hayan llegado. Sin esto, setSelected() se llama con lista vacía y no hace nada.

    private fun tryPreselectSports() {
        if (!sportsItemsLoaded || !exerciseLoaded) return
        originalExercise?.let { sportsDropdown.setSelected(it.sportIds()) }
    }

    private fun tryPreselectCategories() {
        if (!categoriesItemsLoaded || !exerciseLoaded) return
        originalExercise?.let { categoriesDropdown.setSelected(it.categoryIds) }
    }

    // ── Guardar ───────────────────────────────────────────────────────────────

    private fun submit() {
        val name = binding.etName.text.toString().trim()
        val desc = binding.etDescription.text.toString().trim()

        if (name.isEmpty())                         { binding.tilName.error = "Requerido"; return }
        binding.tilName.error = null
        if (selectedType == null)                   { toast("Selecciona un tipo de ejercicio"); return }
        if (sportsDropdown.getSelected().isEmpty()) { toast("Selecciona al menos un deporte"); return }

        exerciseViewModel.updateExercise(
            args.exerciseId,
            ExerciseRequest(
                name                  = name,
                description           = desc.ifEmpty { null },
                exerciseType          = selectedType!!,
                sportIds              = sportsDropdown.getSelected(),
                categoryIds           = categoriesDropdown.getSelected(),
                supportedParameterIds = parametersDropdown.getSelected(),
                isPublic              = originalExercise?.isPublic ?: false
            )
        )
    }

    private fun showLoading() { binding.progressBar.visibility = View.VISIBLE;  binding.btnSave.isEnabled = false }
    private fun hideLoading() { binding.progressBar.visibility = View.GONE;     binding.btnSave.isEnabled = true }
    private fun toast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        exerciseViewModel.clearUpdateState()
        exerciseViewModel.clearDetailState()
        _binding = null
    }
}