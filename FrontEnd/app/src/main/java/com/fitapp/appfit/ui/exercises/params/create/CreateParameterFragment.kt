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
import com.fitapp.appfit.databinding.FragmentCreateParameterBinding
import com.fitapp.appfit.model.ParameterViewModel
import com.fitapp.appfit.model.SportViewModel
import com.fitapp.appfit.response.parameter.request.CustomParameterRequest
import com.fitapp.appfit.response.sport.response.SportResponse
import com.fitapp.appfit.utils.Resource

class CreateParameterFragment : Fragment() {

    private var _binding: FragmentCreateParameterBinding? = null
    private val binding get() = _binding!!
    private val parameterViewModel: ParameterViewModel by viewModels()
    private val sportViewModel: SportViewModel by viewModels()

    private val parameterTypes = mutableListOf<String>()
    private val sportsMap = mutableMapOf<String, Long>()
    private var selectedSportId: Long? = null
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
        // Inicializar el AutoCompleteTextView con un adaptador vacío
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, arrayOf("Cargando tipos..."))
        binding.spinnerParameterType.setAdapter(typeAdapter)

        // Configurar listener para cuando se selecciona un tipo
        binding.spinnerParameterType.setOnItemClickListener { _, _, position, _ ->
            if (position >= 0 && position < parameterTypes.size) {
                selectedParameterType = parameterTypes[position]
                Log.d("CreateParam", "Tipo seleccionado: $selectedParameterType")
            }
        }

        // Configurar AutoCompleteTextView para deportes
        val sportAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, emptyList<String>())
        binding.spinnerSports.setAdapter(sportAdapter)

        // Botón de guardar
        binding.btnSave.setOnClickListener {
            createParameter()
        }

        // Listener para cuando se selecciona un deporte
        binding.spinnerSports.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = binding.spinnerSports.adapter.getItem(position) as String
            selectedSportId = sportsMap[selectedItem]
            Log.d("CreateParam", "Deporte seleccionado: $selectedItem -> $selectedSportId")
        }
    }

    private fun loadInitialData() {
        Log.d("CreateParam", "Cargando datos iniciales...")
        parameterViewModel.getParameterTypes()
        sportViewModel.getAllSports()
    }

    private fun setupObservers() {
        parameterViewModel.parameterTypesState.observe(viewLifecycleOwner, Observer { resource ->
            Log.d("CreateParam", "Observer de tipos: $resource")
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { types ->
                        Log.d("CreateParam", "Tipos recibidos: ${types.size} - $types")
                        updateParameterTypesSpinner(types)

                        // Mostrar Toast solo en desarrollo
                        Toast.makeText(requireContext(), "Tipos cargados: ${types.size}", Toast.LENGTH_SHORT).show()
                    } ?: run {
                        Log.w("CreateParam", "Tipos recibidos nulos")
                        loadDefaultParameterTypes()
                    }
                }
                is Resource.Error -> {
                    Log.e("CreateParam", "Error cargando tipos: ${resource.message}")
                    Toast.makeText(requireContext(), "Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                    loadDefaultParameterTypes()
                }
                else -> {
                    // Loading state
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        })

        sportViewModel.allSportsState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { sports ->
                        updateSportsSpinner(sports)
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Error al cargar deportes: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        })

        parameterViewModel.createParameterState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "✅ Parámetro personal creado exitosamente", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "❌ Error: ${resource.message}", Toast.LENGTH_SHORT).show()
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

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types)
        binding.spinnerParameterType.setAdapter(adapter)

        // Si hay tipos, seleccionar el primero
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

    private fun updateSportsSpinner(sports: List<SportResponse>) {
        sportsMap.clear()
        val sportNames = mutableListOf<String>()

        // Agregar opción "Sin deporte específico" para parámetros personales sin deporte
        sportNames.add("Sin deporte específico (personal)")
        sportsMap["Sin deporte específico (personal)"] = 0

        sports.forEach { sport ->
            val displayName = "${sport.name} (${if (sport.isPredefined) "Predefinido" else "Personalizado"})"
            sportNames.add(displayName)
            sportsMap[displayName] = sport.id
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sportNames)
        binding.spinnerSports.setAdapter(adapter)
        binding.spinnerSports.setText("Sin deporte específico (personal)", false)
    }

    private fun createParameter() {
        val name = binding.etName.text.toString().trim()
        val displayName = binding.etDisplayName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val parameterType = binding.spinnerParameterType.text.toString().trim()
        val unit = binding.etUnit.text.toString().trim()
        val category = binding.etCategory.text.toString().trim()

        // Los usuarios solo pueden crear parámetros personales (NO globales)
        val isGlobal = false

        // Si seleccionó "Sin deporte específico", sportId = null
        val sportId = selectedSportId?.takeIf { it > 0 }

        // Validaciones
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
            displayName = if (displayName.isEmpty()) null else displayName,
            description = if (description.isEmpty()) null else description,
            parameterType = parameterType,
            unit = if (unit.isEmpty()) null else unit,
            validationRules = null,
            isGlobal = isGlobal,
            sportId = sportId,
            category = if (category.isEmpty()) null else category,
            icon = null
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