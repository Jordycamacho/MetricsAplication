package com.fitapp.appfit.ui.exercises.params.create

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
import com.fitapp.appfit.databinding.FragmentEditParameterBinding
import com.fitapp.appfit.model.ParameterViewModel
import com.fitapp.appfit.response.parameter.request.CustomParameterRequest
import com.fitapp.appfit.response.parameter.response.CustomParameterResponse
import com.fitapp.appfit.utils.Resource

class EditParameterFragment : Fragment() {

    private var _binding: FragmentEditParameterBinding? = null
    private val binding get() = _binding!!
    private val parameterViewModel: ParameterViewModel by viewModels()
    private val args: EditParameterFragmentArgs by navArgs()
    private val parameterTypes = mutableListOf<String>()
    private var selectedParameterType: String? = null
    private var currentParameter: com.fitapp.appfit.response.parameter.response.CustomParameterResponse? = null

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
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, arrayOf("Cargando tipos..."))
        binding.spinnerParameterType.setAdapter(typeAdapter)

        binding.spinnerParameterType.setOnItemClickListener { _, _, position, _ ->
            if (position >= 0 && position < parameterTypes.size) {
                selectedParameterType = parameterTypes[position]
                Log.d("EditParam", "Tipo seleccionado: $selectedParameterType")
            }
        }


        binding.btnSave.setOnClickListener {
            updateParameter()
        }
    }

    private fun loadInitialData() {
        Log.d("EditParam", "Cargando datos iniciales para editar parámetro ID: ${args.parameterId}")
        parameterViewModel.getParameterTypes()
        parameterViewModel.getParameterById(args.parameterId)
    }

    private fun setupObservers() {
        parameterViewModel.parameterTypesState.observe(viewLifecycleOwner, Observer { resource ->
            Log.d("EditParam", "Observer de tipos: $resource")
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { types ->
                        Log.d("EditParam", "Tipos recibidos: ${types.size} - $types")
                        updateParameterTypesSpinner(types)
                    } ?: run {
                        Log.w("EditParam", "Tipos recibidos nulos")
                        loadDefaultParameterTypes()
                    }
                }
                is Resource.Error -> {
                    Log.e("EditParam", "Error cargando tipos: ${resource.message}")
                    Toast.makeText(requireContext(), "Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                    loadDefaultParameterTypes()
                }
                else -> {}
            }
        })

        parameterViewModel.parameterDetailState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { parameter ->
                        currentParameter = parameter
                        Log.d("EditParam", "Parámetro cargado: ${parameter.name}, Global: ${parameter.isGlobal}, Owner: ${parameter.ownerId}")

                        if (parameter.isGlobal && parameter.ownerId == null) {
                            Toast.makeText(requireContext(), "No puedes editar parámetros globales del sistema", Toast.LENGTH_LONG).show()
                            findNavController().navigateUp()
                            return@Observer
                        }

                        if (parameter.isGlobal) {
                            Toast.makeText(requireContext(), "No tienes permiso para editar este parámetro", Toast.LENGTH_LONG).show()
                            findNavController().navigateUp()
                            return@Observer
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
        })

        parameterViewModel.updateParameterState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Parámetro actualizado exitosamente", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    showLoading()
                }
                else -> {}
            }
        })
    }

    private fun updateParameterTypesSpinner(types: List<String>) {
        parameterTypes.clear()
        parameterTypes.addAll(types)

        Log.d("EditParam", "Actualizando spinner con ${types.size} tipos")

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types)
        binding.spinnerParameterType.setAdapter(adapter)

        updateTypeSelection()
    }

    private fun loadDefaultParameterTypes() {
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
    }

    private fun populateForm(parameter: CustomParameterResponse) {
        binding.etName.setText(parameter.name)
        binding.etDescription.setText(parameter.description ?: "")
        binding.etUnit.setText(parameter.unit ?: "")

        updateTypeSelection()

        binding.textViewPersonalInfo.visibility = View.VISIBLE
        binding.textViewPersonalInfo.text = "Este parámetro es personal y solo tú puedes verlo y editarlo."
    }

    private fun updateTypeSelection() {
        currentParameter?.let { parameter ->
            val type = parameter.parameterType
            val position = parameterTypes.indexOfFirst { it == type }
            if (position >= 0) {
                selectedParameterType = type
                binding.spinnerParameterType.setText(type, false)
            } else {
                selectedParameterType = type
                binding.spinnerParameterType.setText(type, false)
            }
        }
    }


    private fun updateParameter() {
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

        if (currentParameter?.isGlobal == true && currentParameter?.ownerId == null) {
            Toast.makeText(requireContext(), "No puedes editar parámetros globales del sistema", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentParameter?.isGlobal == true) {
            Toast.makeText(requireContext(), "No tienes permiso para editar este parámetro", Toast.LENGTH_SHORT).show()
            return
        }

        val parameterRequest = CustomParameterRequest(
            name = name,
            description = if (description.isEmpty()) null else description,
            parameterType = parameterType,
            unit = if (unit.isEmpty()) null else unit,
            isGlobal = isGlobal
        )

        Log.d("EditParam", "Actualizando parámetro PERSONAL ID ${args.parameterId}: $parameterRequest")
        parameterViewModel.updateParameter(args.parameterId, parameterRequest)
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
        parameterViewModel.clearUpdateState()
        parameterViewModel.clearDetailState()
        _binding = null
    }
}