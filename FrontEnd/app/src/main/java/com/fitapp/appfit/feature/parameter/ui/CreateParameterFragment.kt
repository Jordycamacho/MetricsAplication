package com.fitapp.appfit.feature.parameter.ui

import android.R
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
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentCreateParameterBinding
import com.fitapp.appfit.feature.parameter.ParameterViewModel
import com.fitapp.appfit.feature.parameter.model.request.CustomParameterRequest

class CreateParameterFragment : Fragment() {

    private var _binding: FragmentCreateParameterBinding? = null
    private val binding get() = _binding!!
    private val parameterViewModel: ParameterViewModel by viewModels()
    private val parameterTypes = mutableListOf<String>()
    private var selectedParameterType: String? = null

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
        binding.toolbar.title = "Crear Parámetro Personal"
    }

    private fun setupForm() {
        val typeAdapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_dropdown_item_1line,
            arrayOf("Cargando tipos...")
        )
        binding.spinnerParameterType.setAdapter(typeAdapter)

        binding.spinnerParameterType.setOnItemClickListener { _, _, position, _ ->
            if (position >= 0 && position < parameterTypes.size) {
                selectedParameterType = parameterTypes[position]
                Log.d("CreateParam", "Tipo seleccionado: $selectedParameterType")
            }
        }

        binding.btnSave.setOnClickListener {
            createParameter()
        }
    }

    private fun loadInitialData() {
        Log.d("CreateParam", "Cargando datos iniciales...")
        parameterViewModel.getParameterTypes()
    }

    private fun setupObservers() {
        parameterViewModel.parameterTypesState.observe(viewLifecycleOwner, Observer { resource ->
            Log.d("CreateParam", "Observer de tipos: $resource")
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { types ->
                        Log.d("CreateParam", "Tipos recibidos: ${types.size} - $types")
                        updateParameterTypesSpinner(types)

                        Toast.makeText(
                            requireContext(),
                            "Tipos cargados: ${types.size}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } ?: run {
                        Log.w("CreateParam", "Tipos recibidos nulos")
                        loadDefaultParameterTypes()
                    }
                }

                is Resource.Error -> {
                    Log.e("CreateParam", "Error cargando tipos: ${resource.message}")
                    Toast.makeText(
                        requireContext(),
                        "Error: ${resource.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadDefaultParameterTypes()
                }

                else -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        })

        parameterViewModel.createParameterState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    Toast.makeText(
                        requireContext(),
                        "✅ Parámetro personal creado exitosamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigateUp()
                }

                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(
                        requireContext(),
                        "Error: ${resource.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is Resource.Loading -> {
                    showLoading()
                }

                else -> {}
            }
        })
    }

    private fun updateParameterTypesSpinner(types: List<String>) {
        binding.progressBar.visibility = View.GONE

        parameterTypes.clear()
        parameterTypes.addAll(types)

        Log.d("CreateParam", "Actualizando spinner con ${types.size} tipos")

        val adapter = ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, types)
        binding.spinnerParameterType.setAdapter(adapter)

        if (types.isNotEmpty()) {
            selectedParameterType = types[0]
            binding.spinnerParameterType.setText(selectedParameterType, false)
            Log.d("CreateParam", "Tipo predeterminado: $selectedParameterType")
        }
    }

    private fun loadDefaultParameterTypes() {
        binding.progressBar.visibility = View.GONE

        val defaultTypes = listOf(
            "NUMBER",
            "INTEGER",
            "TEXT",
            "BOOLEAN",
            "DURATION",
            "DISTANCE",
            "PERCENTAGE"
        )

        updateParameterTypesSpinner(defaultTypes)
        Toast.makeText(requireContext(), "Usando tipos predeterminados", Toast.LENGTH_SHORT).show()
    }

    private fun createParameter() {
        val name = binding.etName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val parameterType = binding.spinnerParameterType.text.toString().trim()
        val unit = binding.etUnit.text.toString().trim()

        val isGlobal = false

        if (name.isEmpty()) {
            binding.etName.error = "El nombre es requerido"
            return
        }

        if (parameterType.isEmpty()) {
            Toast.makeText(requireContext(), "Seleccione un tipo de parámetro", Toast.LENGTH_SHORT).show()
            return
        }

        val parameterRequest = CustomParameterRequest(
            name = name,
            description = if (description.isEmpty()) null else description,
            parameterType = parameterType,
            unit = if (unit.isEmpty()) null else unit,
            isGlobal = false
        )

        Log.d("CreateParam", "Enviando parámetro PERSONAL: $parameterRequest")
        parameterViewModel.createParameter(parameterRequest)
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
        parameterViewModel.clearCreateState()
        _binding = null
    }
}