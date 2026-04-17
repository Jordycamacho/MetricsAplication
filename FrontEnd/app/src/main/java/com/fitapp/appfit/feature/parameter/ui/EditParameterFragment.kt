package com.fitapp.appfit.feature.parameter.ui

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
import androidx.navigation.fragment.navArgs
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentEditParameterBinding
import com.fitapp.appfit.feature.parameter.ParameterViewModel
import com.fitapp.appfit.core.session.SessionManager
import com.fitapp.appfit.feature.parameter.model.request.CustomParameterRequest
import com.fitapp.appfit.feature.parameter.model.response.CustomParameterResponse
import com.fitapp.appfit.feature.parameter.util.ParameterValidation

class EditParameterFragment : Fragment() {

    private var _binding: FragmentEditParameterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ParameterViewModel by viewModels()
    private val args: EditParameterFragmentArgs by navArgs()

    private val parameterTypes = mutableListOf<String>()
    private var selectedType: String? = null
    private var selectedAggregation: String? = null
    private var currentParameter: CustomParameterResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditParameterBinding.inflate(inflater, container, false)
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
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.toolbar.title = "Editar Parámetro Personal"
    }

    private fun setupForm() {
        // Adapter para tipos
        val typeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            arrayOf("Cargando tipos...")
        )
        binding.spinnerParameterType.setAdapter(typeAdapter)

        // Listener para tipo seleccionado
        binding.spinnerParameterType.setOnItemClickListener { _, _, position, _ ->
            if (position >= 0 && position < parameterTypes.size) {
                selectedType = parameterTypes[position]
                onTypeSelected(selectedType!!)
            }
        }

        // Validación en tiempo real del nombre
        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val error = ParameterValidation.validateName(s.toString())
                binding.tilName.error = if (error?.contains("Advertencia") == true) null else error
                if (error?.contains("Advertencia") == true) {
                    binding.tilName.helperText = error
                } else {
                    binding.tilName.helperText = null
                }
            }
        })

        // Switch trackeable
        binding.switchTrackable.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutAggregation.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) {
                selectedAggregation = null
                binding.spinnerAggregation.setText("", false)
            } else {
                // Auto-seleccionar agregación según el tipo actual
                selectedType?.let { type ->
                    val defaultAgg = ParameterValidation.getDefaultAggregation(type)
                    if (defaultAgg != null) {
                        val aggLabel = ParameterValidation.MetricAggregation.fromString(defaultAgg)?.label
                        binding.spinnerAggregation.setText(aggLabel, false)
                        selectedAggregation = defaultAgg
                    }
                }
            }
        }

        // Agregación
        val aggAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            ParameterValidation.MetricAggregation.getAllLabels()
        )
        binding.spinnerAggregation.setAdapter(aggAdapter)
        binding.spinnerAggregation.setOnItemClickListener { _, _, position, _ ->
            selectedAggregation = ParameterValidation.MetricAggregation.values()[position].value
        }

        // Botón guardar
        binding.btnSave.setOnClickListener {
            updateParameter()
        }
    }

    private fun onTypeSelected(type: String) {
        Log.d("EditParam", "Tipo seleccionado: $type")

        // Actualizar hint de unidad
        binding.tilUnit.helperText = ParameterValidation.getUnitHint(type)

        // Deshabilitar unidad para BOOLEAN y PERCENTAGE
        binding.etUnit.isEnabled = !ParameterValidation.isUnitDisabled(type)

        // Auto-completar unidad para PERCENTAGE
        if (type == "PERCENTAGE") {
            binding.etUnit.setText("%")
        } else if (type == "BOOLEAN") {
            binding.etUnit.setText("")
        }

        // Mostrar mensaje de ayuda
        val helpMsg = ParameterValidation.getTypeHelp(type)
        if (helpMsg != null) {
            binding.tvTypeHelp.text = helpMsg
            binding.tvTypeHelp.visibility = View.VISIBLE
        } else {
            binding.tvTypeHelp.visibility = View.GONE
        }

        // Si aún no se ha inicializado el switch con el valor actual, no sobreescribir
        if (currentParameter == null) return

        // Configurar trackeable por defecto (solo si es nuevo, pero aquí ya tenemos currentParameter)
        // No cambiar el valor del switch si ya tiene el valor del parámetro cargado
        val isTrackable = currentParameter?.isTrackable ?: ParameterValidation.isTrackableByDefault(type)
        binding.switchTrackable.isChecked = isTrackable

        // Auto-seleccionar aggregation si es trackeable
        if (isTrackable) {
            val defaultAgg = currentParameter?.metricAggregation ?: ParameterValidation.getDefaultAggregation(type)
            if (defaultAgg != null) {
                val aggLabel = ParameterValidation.MetricAggregation.fromString(defaultAgg)?.label
                binding.spinnerAggregation.setText(aggLabel, false)
                selectedAggregation = defaultAgg
            }
        }
    }

    private fun loadInitialData() {
        Log.d("EditParam", "Cargando datos para editar ID: ${args.parameterId}")
        viewModel.getParameterTypes()
        viewModel.getParameterById(args.parameterId)
    }

    private fun setupObservers() {
        // Tipos de parámetro
        viewModel.parameterTypesState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { types ->
                        updateParameterTypesSpinner(types)
                    } ?: run {
                        loadDefaultParameterTypes()
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    loadDefaultParameterTypes()
                }
                else -> {}
            }
        }

        viewModel.parameterDetailState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { parameter ->
                        currentParameter = parameter

                        val currentUserId = SessionManager.getUserId()
                        if (parameter.ownerId != null && parameter.ownerId != currentUserId) {
                            Toast.makeText(requireContext(), "No puedes editar parámetros de otro usuario", Toast.LENGTH_LONG).show()
                            findNavController().navigateUp()
                            return@let
                        }

                        if (parameter.isGlobal && parameter.ownerId == null) {
                            Toast.makeText(requireContext(), "No puedes editar parámetros globales del sistema", Toast.LENGTH_LONG).show()
                            findNavController().navigateUp()
                            return@let
                        }
                        if (parameter.isGlobal) {
                            Toast.makeText(requireContext(), "No tienes permiso para editar este parámetro", Toast.LENGTH_LONG).show()
                            findNavController().navigateUp()
                            return@let
                        }

                        populateForm(parameter)
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Error al cargar parámetro: ${resource.message}", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                else -> {}
            }
        }

        // Estado de actualización
        viewModel.updateParameterState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "✅ Parámetro actualizado", Toast.LENGTH_SHORT).show()
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

    private fun updateParameterTypesSpinner(types: List<String>) {
        parameterTypes.clear()
        parameterTypes.addAll(types)

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            ParameterValidation.ParameterType.getAllLabels()
        )
        binding.spinnerParameterType.setAdapter(adapter)

        // Si ya tenemos el parámetro cargado, seleccionamos el tipo actual
        currentParameter?.let { param ->
            val typeValue = param.parameterType
            val typeLabel = ParameterValidation.ParameterType.fromString(typeValue)?.label ?: typeValue
            selectedType = typeValue
            binding.spinnerParameterType.setText(typeLabel, false)
            onTypeSelected(typeValue)
        }
    }

    private fun loadDefaultParameterTypes() {
        val defaultTypes = ParameterValidation.ParameterType.getAllValues()
        updateParameterTypesSpinner(defaultTypes)
    }

    private fun populateForm(parameter: CustomParameterResponse) {
        binding.etName.setText(parameter.name)
        binding.etDescription.setText(parameter.description ?: "")
        binding.etUnit.setText(parameter.unit ?: "")

        // Trackeable y agregación
        binding.switchTrackable.isChecked = parameter.isTrackable
        binding.layoutAggregation.visibility = if (parameter.isTrackable) View.VISIBLE else View.GONE

        if (parameter.isTrackable && parameter.metricAggregation != null) {
            val aggLabel = ParameterValidation.MetricAggregation.fromString(parameter.metricAggregation)?.label
            binding.spinnerAggregation.setText(aggLabel, false)
            selectedAggregation = parameter.metricAggregation
        }

        // Seleccionar tipo en el spinner (ya se hace en updateParameterTypesSpinner, pero por si acaso)
        val typeValue = parameter.parameterType
        val typeLabel = ParameterValidation.ParameterType.fromString(typeValue)?.label ?: typeValue
        binding.spinnerParameterType.setText(typeLabel, false)
        selectedType = typeValue
        onTypeSelected(typeValue)

        binding.textViewPersonalInfo.visibility = View.VISIBLE
        binding.textViewPersonalInfo.text = "Este parámetro es personal y solo tú puedes verlo y editarlo."
    }

    private fun updateParameter() {
        val name = binding.etName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val type = selectedType
        var unit = binding.etUnit.text.toString().trim()
        val isTrackable = binding.switchTrackable.isChecked

        // Validaciones
        val nameError = ParameterValidation.validateName(name)
        if (nameError != null && !nameError.contains("Advertencia")) {
            binding.etName.error = nameError
            return
        }

        if (type == null) {
            Toast.makeText(requireContext(), "Selecciona un tipo de parámetro", Toast.LENGTH_SHORT).show()
            return
        }

        val descError = ParameterValidation.validateDescription(description)
        if (descError != null) {
            binding.etDescription.error = descError
            return
        }

        val unitError = ParameterValidation.validateUnit(unit)
        if (unitError != null) {
            binding.etUnit.error = unitError
            return
        }

        // Normalizar unidad
        unit = ParameterValidation.normalizeUnit(type, unit) ?: ""

        // Determinar aggregation
        val aggregation = if (isTrackable) {
            selectedAggregation ?: ParameterValidation.getDefaultAggregation(type)
        } else {
            null
        }

        val parameterRequest = CustomParameterRequest(
            name = name,
            description = if (description.isEmpty()) null else description,
            parameterType = type,
            unit = if (unit.isEmpty()) null else unit,
            isGlobal = false,
            isFavorite = currentParameter?.isFavorite ?: false,
            metricAggregation = aggregation,
            isTrackable = isTrackable
        )

        Log.d("EditParam", "Actualizando parámetro: $parameterRequest")
        viewModel.updateParameter(args.parameterId, parameterRequest)
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
        viewModel.clearUpdateState()
        viewModel.clearDetailState()
        _binding = null
    }
}