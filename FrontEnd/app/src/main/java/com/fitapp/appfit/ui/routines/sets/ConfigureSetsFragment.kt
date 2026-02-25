package com.fitapp.appfit.ui.routines.sets

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.databinding.*
import com.fitapp.appfit.model.ParameterViewModel
import com.fitapp.appfit.model.RoutineSetTemplateViewModel
import com.fitapp.appfit.response.parameter.request.CustomParameterFilterRequest
import com.fitapp.appfit.response.parameter.response.CustomParameterResponse
import com.fitapp.appfit.response.routine.response.RoutineSetTemplateResponse
import com.fitapp.appfit.response.sets.request.CreateSetTemplateRequest
import com.fitapp.appfit.response.sets.request.UpdateSetParameterRequest
import com.fitapp.appfit.response.sets.request.UpdateSetTemplateRequest
import com.fitapp.appfit.ui.exercises.params.ParameterAdapter
import com.fitapp.appfit.utils.Resource
import kotlin.properties.Delegates

class ConfigureSetsFragment : Fragment() {

    private var _binding: FragmentConfigureSetsBinding? = null
    private val binding get() = _binding!!

    private val args: ConfigureSetsFragmentArgs by navArgs()
    private val setTemplateViewModel: RoutineSetTemplateViewModel by viewModels()
    private val parameterViewModel: ParameterViewModel by viewModels()

    private var routineExerciseId by Delegates.notNull<Long>()
    private var currentSets = mutableListOf<RoutineSetTemplateResponse>()
    private var selectedSetId: Long? = null // Set seleccionado para agregar parámetros
    private lateinit var parameterAdapter: ParameterAdapter

    companion object {
        private const val TAG = "ConfigureSetsFragment"
        private val SET_TYPES = listOf(
            "NORMAL", "WARM_UP", "DROP_SET", "SUPER_SET", "GIANT_SET",
            "PYRAMID", "REVERSE_PYRAMID", "CLUSTER", "REST_PAUSE",
            "ECCENTRIC", "ISOMETRIC"
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfigureSetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        routineExerciseId = args.routineExerciseId
        Log.d(TAG, "Configurando sets para ejercicio: $routineExerciseId")

        setupToolbar()
        setupSpinners()
        setupRecyclerView()
        setupListeners()
        setupObservers()
        loadExistingSets()
        setupParameterSearch()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.toolbar.title = "Configurar Sets"
    }

    private fun setupSpinners() {
        val setTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            SET_TYPES
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerSetType.adapter = setTypeAdapter
        binding.spinnerSetType.setSelection(0)
    }

    private fun setupRecyclerView() {
        parameterAdapter = ParameterAdapter(
            onItemClick = { parameter ->
                showAddParameterDialog(parameter)
            },
            onEditClick = { /* No necesitamos editar aquí */ },
            onDeleteClick = { /* No necesitamos eliminar aquí */ },
            showActions = false
        )

        binding.recyclerParameters.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = parameterAdapter
        }
    }

    private fun setupListeners() {
        binding.btnAddSet.setOnClickListener {
            addSet()
        }

        binding.btnSaveAll.setOnClickListener {
            saveAllSets()
        }

        binding.btnClearAll.setOnClickListener {
            clearAllSets()
        }

        // Botón para agregar parámetros a un set existente
        binding.btnAddToSet.setOnClickListener {
            if (selectedSetId == null) {
                Toast.makeText(requireContext(), "Selecciona un set primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loadAllParameters()
            binding.layoutParameterSection.visibility = View.VISIBLE
        }

        binding.btnCloseParameters.setOnClickListener {
            binding.layoutParameterSection.visibility = View.GONE
        }
    }

    private fun setupObservers() {
        // Observar creación de set
        setTemplateViewModel.createSetTemplateState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    resource.data?.let { setResponse ->
                        currentSets.add(setResponse)
                        updateSetsList()
                        Toast.makeText(requireContext(), "✅ Set creado", Toast.LENGTH_SHORT).show()
                        clearForm()
                        // Seleccionar automáticamente el nuevo set para agregar parámetros
                        selectedSetId = setResponse.id
                        loadAllParameters()
                        binding.layoutParameterSection.visibility = View.VISIBLE
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "❌ Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> showLoading()
                else -> {}
            }
        })

        // Observar carga de sets existentes
        setTemplateViewModel.getSetTemplatesByExerciseState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    resource.data?.let { sets ->
                        currentSets.clear()
                        currentSets.addAll(sets)
                        updateSetsList()
                        if (sets.isNotEmpty()) {
                            // Seleccionar el primer set por defecto
                            selectedSetId = sets.first().id
                        }
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    Log.e(TAG, "Error cargando sets: ${resource.message}")
                }
                is Resource.Loading -> showLoading()
                else -> {}
            }
        })

        // Observar eliminación de sets
        setTemplateViewModel.deleteSetTemplatesByExerciseState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    currentSets.clear()
                    updateSetsList()
                    selectedSetId = null
                    binding.layoutParameterSection.visibility = View.GONE
                    Toast.makeText(requireContext(), "Todos los sets eliminados", Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> showLoading()
                else -> {}
            }
        })

        // Observar actualización de set (para agregar parámetros)
        setTemplateViewModel.updateSetTemplateState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    resource.data?.let { updatedSet ->
                        // Actualizar la lista de sets
                        val index = currentSets.indexOfFirst { it.id == updatedSet.id }
                        if (index != -1) {
                            currentSets[index] = updatedSet
                            updateSetsList()
                        }
                        Toast.makeText(requireContext(), "✅ Parámetro agregado", Toast.LENGTH_SHORT).show()
                        binding.layoutParameterSection.visibility = View.GONE
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> showLoading()
                else -> {}
            }
        })

        parameterViewModel.allParametersState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    resource.data?.let { pageResponse ->
                        val parameters = pageResponse.content
                        if (parameters.isEmpty()) {
                            binding.tvNoParameters.visibility = View.VISIBLE
                            binding.recyclerParameters.visibility = View.GONE
                        } else {
                            binding.tvNoParameters.visibility = View.GONE
                            binding.recyclerParameters.visibility = View.VISIBLE
                            parameterAdapter.updateList(parameters)
                        }
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> showLoading()
                else -> {}
            }
        })
    }

    private fun setupParameterSearch() {
        binding.etParameterSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performParameterSearch()
                true
            } else {
                false
            }
        }

        binding.btnClearSearch.setOnClickListener {
            binding.etParameterSearch.text.clear()
            loadAllParameters()
        }

        binding.chipAll.setOnClickListener { loadAllParameters() }
        binding.chipMy.setOnClickListener { loadMyParameters() }
        binding.chipNumber.setOnClickListener { loadParametersByType("NUMBER") }
        binding.chipInteger.setOnClickListener { loadParametersByType("INTEGER") }
        binding.chipDuration.setOnClickListener { loadParametersByType("DURATION") }
    }

    private fun loadExistingSets() {
        setTemplateViewModel.getSetTemplatesByRoutineExercise(routineExerciseId)
    }

    private fun addSet() {
        val position = binding.etSetPosition.text.toString().toIntOrNull() ?: 1
        val setType = binding.spinnerSetType.selectedItem as String
        val restAfterSet = binding.etRestAfterSet.text.toString().toIntOrNull()
        val subSetNumber = binding.etSubSetNumber.text.toString().toIntOrNull()
        val groupId = binding.etGroupId.text.toString().takeIf { it.isNotEmpty() }

        val request = CreateSetTemplateRequest(
            routineExerciseId = routineExerciseId,
            position = position,
            setType = setType,
            restAfterSet = restAfterSet,
            subSetNumber = subSetNumber,
            groupId = groupId,
            parameters = null
        )

        Log.d(TAG, "Creando set básico: posición=$position, tipo=$setType")
        setTemplateViewModel.createSetTemplate(request)
    }

    private fun showAddParameterDialog(parameter: CustomParameterResponse) {
        val dialogBinding = DialogAddParameterBinding.inflate(layoutInflater)

        dialogBinding.tvParameterName.text = parameter.unit ?: parameter.name
        dialogBinding.tvParameterType.text = "Tipo: ${parameter.parameterType}"
        if (!parameter.unit.isNullOrEmpty()) {
            dialogBinding.tvParameterUnit.text = "Unidad: ${parameter.unit}"
            dialogBinding.tvParameterUnit.visibility = View.VISIBLE
        }

        // Configurar campos según el tipo de parámetro
        when (parameter.parameterType) {
            "NUMBER", "DISTANCE", "PERCENTAGE" -> {
                dialogBinding.layoutNumericValue.visibility = View.VISIBLE
            }
            "INTEGER" -> {
                dialogBinding.layoutIntegerValue.visibility = View.VISIBLE
            }
            "DURATION" -> {
                dialogBinding.layoutDurationValue.visibility = View.VISIBLE
            }
            else -> {
                dialogBinding.layoutNumericValue.visibility = View.VISIBLE
                dialogBinding.layoutNumericValue.hint = "Valor"
            }
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setTitle("Agregar ${parameter.unit ?: parameter.name}")
            .setPositiveButton("Agregar") { dialog, _ ->
                addParameterToSet(parameter, dialogBinding)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun addParameterToSet(parameter: CustomParameterResponse, binding: DialogAddParameterBinding) {
        val setId = selectedSetId
        if (setId == null) {
            Toast.makeText(requireContext(), "No hay set seleccionado", Toast.LENGTH_SHORT).show()
            return
        }

        val currentSet = currentSets.find { it.id == setId }
        if (currentSet == null) {
            Toast.makeText(requireContext(), "Set no encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        val newParameter = UpdateSetParameterRequest(
            id = null,
            parameterId = parameter.id,
            repetitions = binding.etRepetitions.text.toString().toIntOrNull(),
            numericValue = when (parameter.parameterType) {
                "NUMBER", "DISTANCE", "PERCENTAGE" ->
                    binding.etNumericValue.text.toString().toDoubleOrNull()
                else -> null
            },
            integerValue = when (parameter.parameterType) {
                "INTEGER" -> binding.etIntegerValue.text.toString().toIntOrNull()
                else -> null
            },
            durationValue = when (parameter.parameterType) {
                "DURATION" -> binding.etDurationValue.text.toString().toLongOrNull()
                else -> null
            }
        )

        val currentParameters = currentSet.parameters?.map { param ->
            UpdateSetParameterRequest(
                id = param.id,
                parameterId = param.parameterId,
                repetitions = param.repetitions,
                numericValue = param.numericValue,
                integerValue = param.integerValue,
                durationValue = param.durationValue
            )
        }?.toMutableList() ?: mutableListOf()

        currentParameters.add(newParameter)

        val updateRequest = UpdateSetTemplateRequest(
            position = currentSet.position,
            subSetNumber = currentSet.subSetNumber,
            groupId = currentSet.groupId,
            setType = currentSet.setType,
            restAfterSet = currentSet.restAfterSet,
            parameters = currentParameters
        )

        setTemplateViewModel.updateSetTemplate(setId, updateRequest)
    }

    private fun saveAllSets() {
        if (currentSets.isEmpty()) {
            Toast.makeText(requireContext(), "No hay sets para guardar", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(requireContext(), "✅ ${currentSets.size} sets configurados", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    private fun clearAllSets() {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Todos los Sets")
            .setMessage("¿Estás seguro de que quieres eliminar todos los sets de este ejercicio?")
            .setPositiveButton("Eliminar") { dialog, _ ->
                setTemplateViewModel.deleteSetTemplatesByRoutineExercise(routineExerciseId)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun loadAllParameters() {
        val searchQuery = binding.etParameterSearch.text.toString().trim()
        val filterRequest = CustomParameterFilterRequest(
            search = if (searchQuery.isNotEmpty()) searchQuery else null,
            page = 0,
            size = 50,
            sortBy = "name",
            direction = "ASC"
        )
        parameterViewModel.searchAllParameters(filterRequest)
    }

    private fun loadMyParameters() {
        val searchQuery = binding.etParameterSearch.text.toString().trim()
        val filterRequest = CustomParameterFilterRequest(
            search = if (searchQuery.isNotEmpty()) searchQuery else null,
            page = 0,
            size = 50,
            sortBy = "name",
            direction = "ASC",
            onlyMine = true
        )
        parameterViewModel.searchMyParameters(filterRequest)
    }

    private fun loadParametersByType(type: String) {
        val searchQuery = binding.etParameterSearch.text.toString().trim()
        val filterRequest = CustomParameterFilterRequest(
            search = if (searchQuery.isNotEmpty()) searchQuery else null,
            page = 0,
            size = 50,
            sortBy = "name",
            direction = "ASC",
            parameterType = type
        )
        parameterViewModel.searchAllParameters(filterRequest)
    }

    private fun performParameterSearch() {
        val query = binding.etParameterSearch.text.toString().trim()
        binding.btnClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
        loadAllParameters()
    }

    private fun updateSetsList() {
        if (currentSets.isEmpty()) {
            binding.tvSetsList.text = "No hay sets configurados"
            binding.tvSetCount.text = "Sets: 0"
            return
        }

        val setsText = currentSets.joinToString("\n\n") { set ->
            val isSelected = set.id == selectedSetId
            val params = if (set.parameters.isNullOrEmpty()) {
                "Sin parámetros"
            } else {
                set.parameters!!.joinToString(", ") { param ->
                    "${param.parameterName ?: "Parámetro"}: ${param.numericValue ?: param.integerValue ?: param.durationValue ?: "-"}"
                }
            }
            val prefix = if (isSelected) "▶ " else "○ "
            "$prefix Set ${set.position}: ${set.setType}\n   $params"
        }

        binding.tvSetsList.text = setsText
        binding.tvSetCount.text = "Sets: ${currentSets.size}"

        // Hacer clickable la lista de sets
        binding.tvSetsList.setOnClickListener {
            showSelectSetDialog()
        }
    }

    private fun showSelectSetDialog() {
        if (currentSets.isEmpty()) {
            Toast.makeText(requireContext(), "No hay sets para seleccionar", Toast.LENGTH_SHORT).show()
            return
        }

        val setsArray = currentSets.map { "Set ${it.position}: ${it.setType}" }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar Set")
            .setItems(setsArray) { dialog, which ->
                selectedSetId = currentSets[which].id
                updateSetsList()
                Toast.makeText(requireContext(), "Set ${which + 1} seleccionado", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun clearForm() {
        binding.etSetPosition.text?.clear()
        binding.etSetPosition.setText("1")
        binding.etRestAfterSet.text?.clear()
        binding.etRestAfterSet.setText("60")
        binding.etSubSetNumber.text?.clear()
        binding.etGroupId.text?.clear()
        binding.spinnerSetType.setSelection(0)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnAddSet.isEnabled = false
        binding.btnAddToSet.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnAddSet.isEnabled = true
        binding.btnAddToSet.isEnabled = (selectedSetId != null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}