package com.fitapp.appfit.ui.exercises.params

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.databinding.FragmentExerciseParamsBinding
import com.fitapp.appfit.model.ParameterViewModel
import com.fitapp.appfit.response.parameter.request.CustomParameterFilterRequest
import com.fitapp.appfit.response.parameter.response.CustomParameterResponse
import com.fitapp.appfit.ui.parameters.adapter.ParameterAdapter
import com.fitapp.appfit.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ExerciseParamsFragment: Fragment() {
    private var _binding: FragmentExerciseParamsBinding? = null
    private val binding get() = _binding!!
    private val parameterViewModel: ParameterViewModel by viewModels()
    private lateinit var parameterAdapter: ParameterAdapter

    // Filtro actual
    private var currentFilter = "all" // "all", "my", "available"
    private var currentSportId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseParamsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        setupSearchListener()

        // Cargar parámetros iniciales
        loadParameters()
    }

    private fun setupRecyclerView() {
        parameterAdapter = ParameterAdapter(
            onItemClick = { parameter ->
                showParameterDetail(parameter)
            },
            onEditClick = { parameter ->
                editParameter(parameter)
            },
            onDeleteClick = { parameter ->
                showDeleteConfirmation(parameter)
            }
        )

        binding.recyclerParameters.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = parameterAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        // Botón flotante para crear parámetro
        binding.fabCreateParameter.setOnClickListener {
            navigateToCreateParameter()
        }

        // Filtros
        binding.chipAll.setOnClickListener {
            currentFilter = "all"
            binding.chipAll.isChecked = true
            binding.chipMy.isChecked = false
            binding.chipAvailable.isChecked = false
            loadParameters()
        }

        binding.chipMy.setOnClickListener {
            currentFilter = "my"
            binding.chipAll.isChecked = false
            binding.chipMy.isChecked = true
            binding.chipAvailable.isChecked = false
            loadParameters()
        }

        binding.chipAvailable.setOnClickListener {
            currentFilter = "available"
            binding.chipAll.isChecked = false
            binding.chipMy.isChecked = false
            binding.chipAvailable.isChecked = true

            // Aquí podrías mostrar un diálogo para seleccionar deporte
            // Por ahora cargamos sin deporte (se cargará empty)
            currentSportId = null
            loadParameters()
        }
    }

    private fun setupSearchListener() {
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    private fun setupObservers() {
        // Observar todos los parámetros
        parameterViewModel.allParametersState.observe(viewLifecycleOwner, Observer { resource ->
            resource?.let {
                handleParametersResponse(it, "No hay parámetros disponibles")
            }
        })

        // Observar mis parámetros
        parameterViewModel.myParametersState.observe(viewLifecycleOwner, Observer { resource ->
            resource?.let {
                handleParametersResponse(it, "No has creado parámetros personalizados")
            }
        })

        // Observar parámetros disponibles
        parameterViewModel.availableParametersState.observe(viewLifecycleOwner, Observer { resource ->
            resource?.let {
                handleParametersResponse(it, "No hay parámetros disponibles para este deporte")
            }
        })

        // Observar estado de eliminación
        parameterViewModel.deleteParameterState.observe(viewLifecycleOwner, Observer { resource ->
            resource?.let {
                when (it) {
                    is Resource.Success -> {
                        Toast.makeText(requireContext(), "✅ Parámetro eliminado", Toast.LENGTH_SHORT).show()
                        loadParameters() // Recargar lista
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), "❌ Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        })
    }

    private fun handleParametersResponse(resource: Resource<com.fitapp.appfit.response.parameter.response.CustomParameterPageResponse>, emptyMessage: String) {
        when (resource) {
            is Resource.Success -> {
                hideLoading()
                resource.data?.let { pageResponse ->
                    val parameters = pageResponse.content
                    if (parameters.isEmpty()) {
                        showEmptyState(emptyMessage)
                    } else {
                        showParametersList()
                        parameterAdapter.updateList(parameters)
                    }
                }
            }
            is Resource.Error -> {
                hideLoading()
                showError(resource.message ?: "Error al cargar parámetros")
                showEmptyState("Error al cargar")
            }
            is Resource.Loading -> {
                showLoading()
            }
        }
    }

    private fun loadParameters() {
        val searchQuery = binding.etSearch.text.toString().trim()

        // Crear filtro con búsqueda si existe
        val filterRequest = CustomParameterFilterRequest(
            search = searchQuery,
            page = 0,
            size = 20,
            sortBy = "name",  // CORREGIDO: era "sort" ahora es "sortBy"
            direction = "ASC"
        )

        when (currentFilter) {
            "all" -> {
                parameterViewModel.searchAllParameters(filterRequest)
            }
            "my" -> {
                // Para "mis parámetros", usar onlyMine = true
                val myFilter = filterRequest.copy(onlyMine = true)
                parameterViewModel.searchMyParameters(myFilter)
            }
            "available" -> {
                currentSportId?.let { sportId ->
                    val sportFilter = filterRequest.copy(sportId = sportId)
                    parameterViewModel.searchAvailableParameters(sportId, sportFilter)
                } ?: run {
                    // Para parámetros disponibles sin deporte específico
                    parameterViewModel.searchAllParameters(filterRequest)
                }
            }
        }
    }

    private fun performSearch() {
        loadParameters()
    }

    private fun navigateToCreateParameter() {
        showCreateParameterDialog()
    }

    private fun showCreateParameterDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Crear Parámetro")
            .setMessage("¿Deseas crear un parámetro personalizado?")
            .setPositiveButton("Crear") { dialog, _ ->
                // Navegar a pantalla de creación
                // findNavController().navigate(R.id.navigation_create_parameter)
                Toast.makeText(requireContext(), "Pantalla de creación en desarrollo", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showParameterDetail(parameter: CustomParameterResponse) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(parameter.displayName ?: parameter.name)
            .setMessage(
                """
                Tipo: ${parameter.parameterType}
                Unidad: ${parameter.unit ?: "No especificada"}
                Categoría: ${parameter.category ?: "Sin categoría"}
                Global: ${if (parameter.isGlobal) "Sí" else "No"}
                Activo: ${if (parameter.isActive) "Sí" else "No"}
                Deporte: ${parameter.sportName ?: "Todos"}
                Creado por: ${parameter.ownerName ?: "Desconocido"}
                Usos: ${parameter.usageCount}
                """.trimIndent()
            )
            .setPositiveButton("Aceptar", null)
            .show()
    }

    private fun editParameter(parameter: CustomParameterResponse) {
        Toast.makeText(requireContext(), "Editando: ${parameter.name}", Toast.LENGTH_SHORT).show()
        // Navegar a pantalla de edición
        // findNavController().navigate(R.id.navigation_edit_parameter, bundleOf("parameterId" to parameter.id))
    }

    private fun showDeleteConfirmation(parameter: CustomParameterResponse) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar Parámetro")
            .setMessage("¿Estás seguro de que quieres eliminar '${parameter.displayName ?: parameter.name}'?\n\nEsta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { dialog, _ ->
                parameterViewModel.deleteParameter(parameter.id)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerParameters.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showParametersList() {
        binding.recyclerParameters.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
    }

    private fun showEmptyState(message: String) {
        binding.recyclerParameters.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.tvEmptyState.text = message
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), "❌ $message", Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        loadParameters()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}