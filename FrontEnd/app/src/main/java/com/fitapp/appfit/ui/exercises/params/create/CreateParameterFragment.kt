package com.fitapp.appfit.ui.exercises.params.create

import android.os.Bundle
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
    // Lists para los spinners
    private val parameterTypes = mutableListOf<String>()
    private val sportsMap = mutableMapOf<String, Long>()
    private var selectedSportId: Long? = null

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
        binding.toolbar.title = "Crear Parámetro"
    }

    private fun setupForm() {
        // Configurar adaptadores para los AutoCompleteTextView
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, parameterTypes)
        binding.spinnerParameterType.setAdapter(typeAdapter)

        // Configurar Spinner de deportes
        val sportAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, emptyList<String>())
        binding.spinnerSports.setAdapter(sportAdapter)

        // Botón de guardar
        binding.btnSave.setOnClickListener {
            createParameter()
        }

        // Switch para global
        binding.switchGlobal.setOnCheckedChangeListener { _, isChecked ->
            // Si es global, ocultar el selector de deportes
            binding.layoutSport.visibility = if (isChecked) View.GONE else View.VISIBLE
        }

        // Listener para cuando se selecciona un deporte
        binding.spinnerSports.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = binding.spinnerSports.adapter.getItem(position) as String
            selectedSportId = sportsMap[selectedItem]
        }
    }


    // Elimina el observer de categoriesState ya que no lo necesitas

    private fun loadInitialData() {
        parameterViewModel.getParameterTypes()
        parameterViewModel.getCategories()
        sportViewModel.getAllSports() // Cargar todos los deportes
    }

    private fun setupObservers() {
        parameterViewModel.parameterTypesState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { types ->
                        parameterTypes.clear()
                        parameterTypes.addAll(types)
                        (binding.spinnerParameterType.adapter as ArrayAdapter<*>).notifyDataSetChanged()
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Error al cargar tipos: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                else -> {}
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
                    Toast.makeText(requireContext(), "✅ Parámetro creado exitosamente", Toast.LENGTH_SHORT).show()
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

    private fun updateSportsSpinner(sports: List<SportResponse>) {
        sportsMap.clear()
        val sportNames = mutableListOf<String>()

        // Agregar opción vacía para parámetros globales
        sportNames.add("Seleccionar deporte (opcional para globales)")
        sportsMap["Seleccionar deporte (opcional para globales)"] = 0

        sports.forEach { sport ->
            val displayName = if (sport.isPredefined) {
                "${sport.name} (Predefinido)"
            } else {
                "${sport.name} (Personalizado)"
            }
            sportNames.add(displayName)
            sportsMap[displayName] = sport.id
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sportNames)
        binding.spinnerSports.setAdapter(adapter)
    }

    private fun createParameter() {
        val name = binding.etName.text.toString().trim()
        val displayName = binding.etDisplayName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val parameterType = binding.spinnerParameterType.text.toString().trim()
        val unit = binding.etUnit.text.toString().trim()
        val category = binding.etCategory.text.toString().trim()
        val isGlobal = binding.switchGlobal.isChecked
        val sportId = if (isGlobal) null else selectedSportId?.takeIf { it > 0 }

        // Validaciones básicas
        if (name.isEmpty()) {
            binding.etName.error = "El nombre es requerido"
            return
        }

        if (parameterType.isEmpty()) {
            Toast.makeText(requireContext(), "Seleccione un tipo de parámetro", Toast.LENGTH_SHORT).show()
            return
        }

        // Si no es global y no se seleccionó deporte, mostrar error
        if (!isGlobal && (selectedSportId == null || selectedSportId == 0L)) {
            Toast.makeText(requireContext(), "Seleccione un deporte para parámetros no globales", Toast.LENGTH_SHORT).show()
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