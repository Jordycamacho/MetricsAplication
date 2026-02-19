package com.fitapp.appfit.ui.routines.sets

import android.os.Bundle
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
import com.fitapp.appfit.databinding.FragmentAddEditSetBinding
import com.fitapp.appfit.databinding.DialogAddParameterBinding
import com.fitapp.appfit.model.ExerciseViewModel
import com.fitapp.appfit.model.ParameterViewModel
import com.fitapp.appfit.model.RoutineSetTemplateViewModel
import com.fitapp.appfit.response.parameter.request.CustomParameterFilterRequest
import com.fitapp.appfit.response.parameter.response.CustomParameterResponse
import com.fitapp.appfit.response.routine.request.SetParameterRequest
import com.fitapp.appfit.response.sets.request.CreateSetTemplateRequest
import com.fitapp.appfit.response.sets.request.UpdateSetParameterRequest
import com.fitapp.appfit.response.sets.request.UpdateSetTemplateRequest
import com.fitapp.appfit.ui.exercises.params.ParameterAdapter
import com.fitapp.appfit.utils.Resource
import com.google.android.material.chip.Chip

class AddEditSetFragment : Fragment() {

    private var _binding: FragmentAddEditSetBinding? = null
    private val binding get() = _binding!!

    private val args: AddEditSetFragmentArgs by navArgs()
    private val setTemplateViewModel: RoutineSetTemplateViewModel by viewModels()
    private val parameterViewModel: ParameterViewModel by viewModels()
    private val exerciseViewModel: ExerciseViewModel by viewModels()

    private var selectedSetId: Long? = null
    private lateinit var parameterAdapter: ParameterAdapter
    private var supportedParameterIds = setOf<Long>()
    private var currentSetParameters = mutableListOf<UpdateSetParameterRequest>()

    companion object {
        private const val TAG = "AddEditSetFragment"
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
        _binding = FragmentAddEditSetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedSetId = if (args.setId == -1L) null else args.setId

        setupToolbar()
        setupSpinners()
        setupRecyclerView()
        setupListeners()
        setupObservers()
        loadSupportedParameters()

        if (selectedSetId != null) {
            loadSetForEdit()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.toolbar.title = if (selectedSetId == null) "Nuevo Set" else "Editar Set"
    }

    private fun loadSupportedParameters() {
        exerciseViewModel.getExerciseById(args.exerciseId)
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
            onEditClick = {},
            onDeleteClick = {},
            showActions = false
        )

        binding.recyclerParameters.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = parameterAdapter
        }
    }

    private fun setupListeners() {
        binding.btnSaveSet.setOnClickListener {
            saveSet()
        }

        binding.btnAddParameter.setOnClickListener {
            if (selectedSetId == null) {
                Toast.makeText(requireContext(), "Primero guarda el set", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loadAllParameters()
            binding.layoutParameterSection.visibility = View.VISIBLE
        }

        binding.btnCloseParameters.setOnClickListener {
            binding.layoutParameterSection.visibility = View.GONE
        }

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

    private fun setupObservers() {
        exerciseViewModel.exerciseDetailState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { exercise ->
                        supportedParameterIds = exercise.supportedParameterIds ?: emptySet()
                        loadAllParameters()
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Error al cargar ejercicio: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        parameterViewModel.allParametersState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    resource.data?.let { pageResponse ->
                        val allParameters = pageResponse.content
                        val filtered = if (supportedParameterIds.isNotEmpty()) {
                            allParameters.filter { supportedParameterIds.contains(it.id) }
                        } else {
                            allParameters
                        }
                        if (filtered.isEmpty()) {
                            binding.tvNoParameters.visibility = View.VISIBLE
                            binding.recyclerParameters.visibility = View.GONE
                        } else {
                            binding.tvNoParameters.visibility = View.GONE
                            binding.recyclerParameters.visibility = View.VISIBLE
                            parameterAdapter.updateList(filtered)
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
        }

        setTemplateViewModel.createSetTemplateState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Set creado", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> showLoading()
                else -> {}
            }
        })

        // Observar actualización de set
        setTemplateViewModel.updateSetTemplateState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Set actualizado", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> showLoading()
                else -> {}
            }
        })

        setTemplateViewModel.getSetTemplateState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    resource.data?.let { set ->
                        binding.etSetPosition.setText(set.position.toString())
                        val typeIndex = SET_TYPES.indexOf(set.setType)
                        if (typeIndex >= 0) binding.spinnerSetType.setSelection(typeIndex)
                        binding.etRestAfterSet.setText(set.restAfterSet?.toString() ?: "")
                        binding.etSubSetNumber.setText(set.subSetNumber?.toString() ?: "")
                        binding.etGroupId.setText(set.groupId ?: "")

                        set.parameters?.let { params ->
                            currentSetParameters.clear()
                            currentSetParameters.addAll(params.map { param ->
                                UpdateSetParameterRequest(
                                    id = param.id,
                                    parameterId = param.parameterId,
                                    repetitions = param.repetitions,
                                    numericValue = param.numericValue,
                                    integerValue = param.integerValue,
                                    durationValue = param.durationValue
                                )
                            })
                            updateParametersChips()
                        }
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Error cargando set: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> showLoading()
                else -> {}
            }
        })
    }

    private fun loadSetForEdit() {
        selectedSetId?.let { id ->
            setTemplateViewModel.getSetTemplate(id)
        }
    }

    private fun saveSet() {
        val position = binding.etSetPosition.text.toString().toIntOrNull() ?: 1
        val setType = binding.spinnerSetType.selectedItem as String
        val restAfterSet = binding.etRestAfterSet.text.toString().toIntOrNull()
        val subSetNumber = binding.etSubSetNumber.text.toString().toIntOrNull()
        val groupId = binding.etGroupId.text.toString().takeIf { it.isNotEmpty() }

        if (selectedSetId == null) {
            val parametersForCreation = currentSetParameters.map { param ->
                SetParameterRequest(
                    parameterId = param.parameterId,
                    repetitions = param.repetitions,
                    numericValue = param.numericValue,
                    integerValue = param.integerValue,
                    durationValue = param.durationValue
                )
            }

            val request = CreateSetTemplateRequest(
                routineExerciseId = args.routineExerciseId,
                position = position,
                setType = setType,
                restAfterSet = restAfterSet,
                subSetNumber = subSetNumber,
                groupId = groupId,
                parameters = if (parametersForCreation.isNotEmpty()) parametersForCreation else null
            )
            setTemplateViewModel.createSetTemplate(request)
        } else {
            val request = UpdateSetTemplateRequest(
                position = position,
                subSetNumber = subSetNumber,
                groupId = groupId,
                setType = setType,
                restAfterSet = restAfterSet,
                parameters = currentSetParameters
            )
            setTemplateViewModel.updateSetTemplate(selectedSetId!!, request)
        }
    }

    private fun showAddParameterDialog(parameter: CustomParameterResponse) {
        val dialogBinding = DialogAddParameterBinding.inflate(layoutInflater)

        dialogBinding.tvParameterName.text = parameter.displayName ?: parameter.name
        dialogBinding.tvParameterType.text = "Tipo: ${parameter.parameterType}"
        if (!parameter.unit.isNullOrEmpty()) {
            dialogBinding.tvParameterUnit.text = "Unidad: ${parameter.unit}"
            dialogBinding.tvParameterUnit.visibility = View.VISIBLE
        }

        dialogBinding.layoutRepetitions.visibility = View.VISIBLE

        when (parameter.parameterType) {
            "NUMBER", "DISTANCE", "PERCENTAGE" -> dialogBinding.layoutNumericValue.visibility = View.VISIBLE
            "INTEGER" -> dialogBinding.layoutIntegerValue.visibility = View.VISIBLE
            "DURATION" -> dialogBinding.layoutDurationValue.visibility = View.VISIBLE
            else -> {
                dialogBinding.layoutNumericValue.visibility = View.VISIBLE
                dialogBinding.layoutNumericValue.hint = "Valor"
            }
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setTitle("Agregar ${parameter.displayName ?: parameter.name}")
            .setPositiveButton("Agregar") { dialog, _ ->
                addParameterToSet(parameter, dialogBinding)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun addParameterToSet(parameter: CustomParameterResponse, binding: DialogAddParameterBinding) {
        val newParameter = UpdateSetParameterRequest(
            id = null,
            parameterId = parameter.id,
            repetitions = binding.etRepetitions.text.toString().toIntOrNull(),
            numericValue = when (parameter.parameterType) {
                "NUMBER", "DISTANCE", "PERCENTAGE" -> binding.etNumericValue.text.toString().toDoubleOrNull()
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

        currentSetParameters.add(newParameter)
        updateParametersChips()
        Toast.makeText(requireContext(), "Parámetro agregado", Toast.LENGTH_SHORT).show()
    }

    private fun updateParametersChips() {
        binding.chipGroupSelected.removeAllViews()
        if (currentSetParameters.isEmpty()) {
            binding.tvNoSelectedParameters.visibility = View.VISIBLE
            binding.layoutSelectedParameters.visibility = View.GONE
        } else {
            binding.tvNoSelectedParameters.visibility = View.GONE
            binding.layoutSelectedParameters.visibility = View.VISIBLE
            currentSetParameters.forEach { param ->
                val chip = Chip(requireContext())
                chip.text = "Parámetro ${param.parameterId}" +
                        if (param.repetitions != null) " (${param.repetitions} reps)" else ""
                chip.isCloseIconVisible = true
                chip.setOnCloseIconClickListener {
                    currentSetParameters.remove(param)
                    updateParametersChips()
                }
                binding.chipGroupSelected.addView(chip)
            }
        }
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

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSaveSet.isEnabled = false
        binding.btnAddParameter.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnSaveSet.isEnabled = true
        binding.btnAddParameter.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}