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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fitapp.appfit.R
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentCreateParameterBinding
import com.fitapp.appfit.feature.parameter.ParameterViewModel
import com.fitapp.appfit.feature.parameter.model.request.CustomParameterRequest
import com.fitapp.appfit.feature.parameter.util.ParameterValidation
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CreateParameterFragment : Fragment() {

    private var _binding: FragmentCreateParameterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ParameterViewModel by viewModels()

    private val parameterTypes = mutableListOf<String>()
    private var selectedType: String? = null
    private var selectedAggregation: String? = null

    companion object {
        private const val TAG = "CreateParameter"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateParameterBinding.inflate(inflater, container, false)
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
        binding.toolbar.title = "Nuevo Parámetro Personal"
    }

    private fun setupForm() {
        // Adapter para tipos de parámetro
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

        // Switch de trackeable
        binding.switchTrackable.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutAggregation.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) {
                selectedAggregation = null
                binding.spinnerAggregation.setText("", false)
            } else {
                // Auto-seleccionar aggregation según tipo
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
            createParameter()
        }
    }

    private fun onTypeSelected(type: String) {
        Log.d(TAG, "Type selected: $type")

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

        // Configurar trackeable por defecto
        val isTrackable = ParameterValidation.isTrackableByDefault(type)
        binding.switchTrackable.isChecked = isTrackable

        // Auto-seleccionar aggregation si es trackeable
        if (isTrackable) {
            val defaultAgg = ParameterValidation.getDefaultAggregation(type)
            if (defaultAgg != null) {
                val aggLabel = ParameterValidation.MetricAggregation.fromString(defaultAgg)?.label
                binding.spinnerAggregation.setText(aggLabel, false)
                selectedAggregation = defaultAgg
            }
        }
    }

    private fun loadInitialData() {
        Log.d(TAG, "Loading initial data...")
        viewModel.getParameterTypes()
    }

    private fun setupObservers() {
        // Observer de tipos
        viewModel.parameterTypesState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { types ->
                        Log.d(TAG, "Types received: ${types.size}")
                        updateParameterTypesSpinner(types)
                    } ?: run {
                        Log.w(TAG, "Types data is null")
                        loadDefaultParameterTypes()
                    }
                }
                is Resource.Error -> {
                    Log.e(TAG, "Error loading types: ${resource.message}")
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    loadDefaultParameterTypes()
                }
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                else -> {}
            }
        }

        // Observer de creación
        viewModel.createParameterState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    Toast.makeText(
                        requireContext(),
                        "✅ Parámetro creado exitosamente",
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

    private fun updateParameterTypesSpinner(types: List<String>) {
        binding.progressBar.visibility = View.GONE
        parameterTypes.clear()
        parameterTypes.addAll(types)

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            ParameterValidation.ParameterType.getAllLabels()
        )
        binding.spinnerParameterType.setAdapter(adapter)

        if (types.isNotEmpty()) {
            selectedType = types[0]
            binding.spinnerParameterType.setText(
                ParameterValidation.ParameterType.fromString(types[0])?.label ?: types[0],
                false
            )
            onTypeSelected(types[0])
        }
    }

    private fun loadDefaultParameterTypes() {
        binding.progressBar.visibility = View.GONE
        val defaultTypes = ParameterValidation.ParameterType.getAllValues()
        updateParameterTypesSpinner(defaultTypes)
    }

    private fun createParameter() {
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
            isFavorite = false,
            metricAggregation = aggregation,
            isTrackable = isTrackable
        )

        Log.d(TAG, "Creating parameter: $parameterRequest")
        viewModel.createParameter(parameterRequest)
    }

    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("Entendido", null)
            .setIcon(R.drawable.ic_close)
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
        viewModel.clearCreateState()
        _binding = null
    }
}