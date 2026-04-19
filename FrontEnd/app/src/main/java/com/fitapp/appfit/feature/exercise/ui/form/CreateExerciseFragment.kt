package com.fitapp.appfit.feature.exercise.ui.form

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fitapp.appfit.R
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentCreateExerciseBinding
import com.fitapp.appfit.feature.exercise.ExerciseViewModel
import com.fitapp.appfit.feature.exercise.model.exercise.request.ExerciseRequest
import com.fitapp.appfit.feature.exercise.model.exercise.response.ExerciseType
import com.fitapp.appfit.feature.exercise.util.ExerciseValidation
import com.fitapp.appfit.feature.parameter.ParameterViewModel
import com.fitapp.appfit.feature.parameter.model.request.CustomParameterFilterRequest
import com.fitapp.appfit.feature.exercise.ExerciseCategoryViewModel
import com.fitapp.appfit.feature.sport.SportViewModel
import com.fitapp.appfit.shared.ui.MultiSelectDropdown
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CreateExerciseFragment : Fragment() {

    private var _binding: FragmentCreateExerciseBinding? = null
    private val binding get() = _binding!!

    private val exerciseViewModel: ExerciseViewModel by viewModels()
    private val sportViewModel: SportViewModel by viewModels()
    private val parameterViewModel: ParameterViewModel by viewModels()
    private val categoryViewModel: ExerciseCategoryViewModel by viewModels()

    private var selectedType: ExerciseType? = null

    private lateinit var sportsDropdown: MultiSelectDropdown
    private lateinit var categoriesDropdown: MultiSelectDropdown
    private lateinit var parametersDropdown: MultiSelectDropdown

    companion object {
        private const val TAG = "CreateExercise"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupForm()
        setupDropdowns()
        setupObservers()
        setupValidations()
        loadInitialData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.toolbar.title = "Nuevo Ejercicio"
    }

    private fun setupForm() {
        // Adapter de tipos
        val typeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            ExerciseValidation.ExerciseTypeInfo.getAllLabels()
        )
        binding.spinnerExerciseType.setAdapter(typeAdapter)

        // Listener de tipo seleccionado
        binding.spinnerExerciseType.setOnItemClickListener { _, _, position, _ ->
            val types = ExerciseValidation.ExerciseTypeInfo.values()
            if (position >= 0 && position < types.size) {
                selectedType = types[position].type
                onTypeSelected(types[position])
            }
        }

        // Botón guardar
        binding.btnSave.setOnClickListener {
            createExercise()
        }
    }

    private fun setupDropdowns() {
        sportsDropdown = MultiSelectDropdown(requireContext()).apply {
            onSelectionChanged = { sportIds ->
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

    private fun onTypeSelected(typeInfo: ExerciseValidation.ExerciseTypeInfo) {
        Log.d(TAG, "Type selected: ${typeInfo.label}")

        // Mostrar sugerencias de parámetros
        val suggestedParams = ExerciseValidation.getSuggestedParametersMessage(typeInfo.type)
        Toast.makeText(requireContext(), "💡 $suggestedParams", Toast.LENGTH_LONG).show()
    }

    private fun setupValidations() {
        // Validación en tiempo real del nombre
        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val error = ExerciseValidation.validateName(s.toString())
                binding.tilName.error = error
            }
        })

        // Validación de descripción
        binding.etDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val error = ExerciseValidation.validateDescription(s.toString())
                binding.tilDescription.error = error

                // Mostrar contador de caracteres
                val length = s?.length ?: 0
                binding.tilDescription.helperText = "$length / 500 caracteres"
            }
        })
    }

    private fun loadInitialData() {
        sportViewModel.getAllSports()
        categoryViewModel.searchAllCategories()
    }

    private fun setupObservers() {
        // Deportes
        sportViewModel.allSportsState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    sportsDropdown.setItems(
                        resource.data?.map { MultiSelectDropdown.Item(it.id, it.name) } ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Error cargando deportes", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        // Categorías
        categoryViewModel.allCategoriesState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    categoriesDropdown.setItems(
                        resource.data?.content?.map { MultiSelectDropdown.Item(it.id, it.name) } ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Error cargando categorías", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        // Parámetros
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

        // Observer de creación
        exerciseViewModel.createExerciseState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    Toast.makeText(
                        requireContext(),
                        "✅ Ejercicio creado exitosamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    hideLoading()
                    showErrorDialog(resource.message ?: "Error desconocido")
                }
                is Resource.Loading -> {
                    showLoading()
                }
                else -> {}
            }
        }
    }

    private fun createExercise() {
        val name = binding.etName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val type = selectedType

        // Validaciones
        val nameError = ExerciseValidation.validateName(name)
        if (nameError != null) {
            binding.etName.error = nameError
            binding.etName.requestFocus()
            return
        }

        if (type == null) {
            Toast.makeText(requireContext(), "Selecciona un tipo de ejercicio", Toast.LENGTH_SHORT).show()
            return
        }

        val descError = ExerciseValidation.validateDescription(description)
        if (descError != null) {
            binding.etDescription.error = descError
            return
        }

        val sportIds = sportsDropdown.getSelected()
        val sportError = ExerciseValidation.validateSports(sportIds)
        if (sportError != null && !sportError.contains("Advertencia")) {
            Toast.makeText(requireContext(), sportError, Toast.LENGTH_SHORT).show()
            return
        }

        // Crear request - SIEMPRE isPublic = false
        val exerciseRequest = ExerciseRequest(
            name = name,
            description = if (description.isEmpty()) null else description,
            exerciseType = type,
            sportIds = sportIds,
            categoryIds = categoriesDropdown.getSelected(),
            supportedParameterIds = parametersDropdown.getSelected(),
            isPublic = false  // SIEMPRE PERSONAL
        )

        Log.d(TAG, "Creating exercise: $exerciseRequest")
        exerciseViewModel.createExercise(exerciseRequest)
    }

    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("Entendido") { dialog, _ ->
                dialog.dismiss()

                // Si es error de límite de suscripción, ofrecer upgrade
                if (message.contains("límite") || message.contains("plan")) {
                    showUpgradeDialog()
                }
            }
            .setIcon(R.drawable.ic_close)
            .show()
    }

    private fun showUpgradeDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Actualizar Plan")
            .setMessage("¿Deseas ver los planes disponibles?")
            .setPositiveButton("Ver planes") { _, _ ->
                findNavController().navigate(R.id.navigation_subscription)
            }
            .setNegativeButton("Ahora no", null)
            .show()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false
        binding.btnSave.alpha = 0.5f
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnSave.isEnabled = true
        binding.btnSave.alpha = 1.0f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exerciseViewModel.clearCreateState()
        _binding = null
    }
}