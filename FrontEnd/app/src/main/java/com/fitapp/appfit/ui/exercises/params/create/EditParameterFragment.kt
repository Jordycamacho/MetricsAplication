package com.fitapp.appfit.ui.parameters

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
import androidx.navigation.fragment.navArgs
import com.fitapp.appfit.databinding.FragmentEditParameterBinding
import com.fitapp.appfit.model.ParameterViewModel
import com.fitapp.appfit.response.parameter.request.CustomParameterRequest
import com.fitapp.appfit.utils.Resource

class EditParameterFragment : Fragment() {

    private var _binding: FragmentEditParameterBinding? = null
    private val binding get() = _binding!!
    private val parameterViewModel: ParameterViewModel by viewModels()
    private val args: EditParameterFragmentArgs by navArgs()

    private val parameterTypes = mutableListOf<String>()
    private val categories = mutableListOf<String>()
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
        loadData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.toolbar.title = "Editar Parámetro"
    }

    private fun setupForm() {
        // Configurar adaptadores para AutoCompleteTextView
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, parameterTypes)
        binding.spinnerParameterType.setAdapter(typeAdapter)

        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.spinnerCategory.setAdapter(categoryAdapter)

        // Switch para global
        binding.switchGlobal.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutSport.visibility = if (isChecked) View.GONE else View.VISIBLE
        }

        // Botón de guardar
        binding.btnSave.setOnClickListener {
            updateParameter()
        }
    }

    private fun loadData() {
        parameterViewModel.getParameterTypes()
        parameterViewModel.getCategories()
        parameterViewModel.getParameterById(args.parameterId)
    }

    private fun setupObservers() {
        parameterViewModel.parameterTypesState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { types ->
                        parameterTypes.clear()
                        parameterTypes.addAll(types)
                        (binding.spinnerParameterType.adapter as ArrayAdapter<String>).notifyDataSetChanged()
                        updateTypeSelection()
                    }
                }
                else -> {}
            }
        })

        parameterViewModel.categoriesState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { cats ->
                        categories.clear()
                        categories.addAll(cats)  // CORREGIDO: cats es val, pero podemos asignar la lista
                        (binding.spinnerCategory.adapter as ArrayAdapter<String>).notifyDataSetChanged()
                        updateCategorySelection()
                    }
                }
                else -> {}
            }
        })

        parameterViewModel.parameterDetailState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    resource.data?.let { parameter ->
                        currentParameter = parameter
                        populateForm(parameter)
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Error al cargar parámetro: ${resource.message}", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Loading -> {
                    showLoading()
                }
                else -> {}
            }
        })

        parameterViewModel.updateParameterState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "✅ Parámetro actualizado", Toast.LENGTH_SHORT).show()
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

    private fun populateForm(parameter: com.fitapp.appfit.response.parameter.response.CustomParameterResponse) {
        binding.etName.setText(parameter.name)
        binding.etDisplayName.setText(parameter.displayName ?: "")
        binding.etDescription.setText(parameter.description ?: "")
        binding.etUnit.setText(parameter.unit ?: "")
        binding.switchGlobal.isChecked = parameter.isGlobal
        binding.etSportId.setText(parameter.sportId?.toString() ?: "")

        // Ocultar/mostrar campo deporte según sea global
        binding.layoutSport.visibility = if (parameter.isGlobal) View.GONE else View.VISIBLE

        // Deshabilitar switch si es global por defecto
        if (parameter.isGlobal && parameter.ownerId == null) {
            binding.switchGlobal.isEnabled = false
            binding.textViewGlobalInfo.visibility = View.VISIBLE
            binding.textViewGlobalInfo.text = "Parámetro global por defecto. No se puede modificar."
        }
    }

    private fun updateTypeSelection() {
        currentParameter?.let { parameter ->
            // Establecer el texto del AutoCompleteTextView con el tipo del parámetro
            binding.spinnerParameterType.setText(parameter.parameterType, false)
        }
    }

    private fun updateCategorySelection() {
        currentParameter?.let { parameter ->
            parameter.category?.let { category ->
                // Establecer el texto del AutoCompleteTextView con la categoría
                binding.spinnerCategory.setText(category, false)
            }
        }
    }

    private fun updateParameter() {
        val name = binding.etName.text.toString().trim()
        val displayName = binding.etDisplayName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val parameterType = binding.spinnerParameterType.text.toString().trim()  // Corregido: usar text
        val unit = binding.etUnit.text.toString().trim()
        val category = binding.spinnerCategory.text.toString().trim()  // Corregido: usar text
        val isGlobal = binding.switchGlobal.isChecked
        val sportId = if (isGlobal) null else binding.etSportId.text.toString().toLongOrNull()

        // Validaciones
        if (name.isEmpty()) {
            binding.etName.error = "El nombre es requerido"
            return
        }

        if (parameterType.isEmpty()) {
            Toast.makeText(requireContext(), "Seleccione un tipo de parámetro", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isGlobal && sportId == null) {
            binding.etSportId.error = "El ID del deporte es requerido para parámetros no globales"
            return
        }

        // Verificar si es un parámetro por defecto (no se puede modificar)
        if (currentParameter?.isGlobal == true && currentParameter?.ownerId == null) {
            Toast.makeText(requireContext(), "No se puede modificar un parámetro por defecto", Toast.LENGTH_SHORT).show()
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