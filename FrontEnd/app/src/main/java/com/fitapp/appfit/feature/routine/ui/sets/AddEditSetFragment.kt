package com.fitapp.appfit.feature.routine.ui.sets

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.DialogAddParameterBinding
import com.fitapp.appfit.databinding.FragmentAddEditSetBinding
import com.fitapp.appfit.feature.routine.model.rutinexercise.request.SetParameterRequest
import com.fitapp.appfit.feature.routine.model.setemplate.request.CreateSetTemplateRequest
import com.fitapp.appfit.feature.routine.model.setemplate.request.UpdateSetTemplateRequest
import com.fitapp.appfit.feature.routine.model.setparameter.request.UpdateSetParameterRequest
import com.fitapp.appfit.feature.exercise.ExerciseViewModel
import com.fitapp.appfit.feature.parameter.ParameterViewModel
import com.fitapp.appfit.feature.routine.ui.RoutineSetTemplateViewModel
import com.fitapp.appfit.feature.parameter.model.request.CustomParameterFilterRequest
import com.fitapp.appfit.feature.parameter.model.response.CustomParameterResponse
import com.fitapp.appfit.feature.parameter.ui.ParameterAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class AddEditSetFragment : Fragment() {

    private var _binding: FragmentAddEditSetBinding? = null
    private val binding get() = _binding!!

    private val args: AddEditSetFragmentArgs by navArgs()
    private val setViewModel: RoutineSetTemplateViewModel by viewModels()
    private val parameterViewModel: ParameterViewModel by viewModels()
    private val exerciseViewModel: ExerciseViewModel by viewModels()

    private var editingSetId: Long? = null
    private lateinit var parameterAdapter: ParameterAdapter
    private var supportedParameterIds = setOf<Long>()

    private val pendingParameters = mutableListOf<UpdateSetParameterRequest>()

    companion object {
        private val SET_TYPES = listOf(
            "NORMAL", "WARM_UP", "DROP_SET", "SUPER_SET", "GIANT_SET",
            "PYRAMID", "REVERSE_PYRAMID", "CLUSTER", "REST_PAUSE", "ECCENTRIC", "ISOMETRIC"
        )
        private val SET_TYPE_LABELS = listOf(
            "Normal", "Calentamiento", "Drop Set", "Super Set", "Giant Set",
            "Pirámide", "Pirámide inversa", "Cluster", "Rest-Pause", "Excéntrico", "Isométrico"
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditSetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editingSetId = if (args.setId == -1L) null else args.setId

        setupToolbar()
        setupSetTypeSpinner()
        setupParameterList()
        setupParameterSearch()
        setupListeners()
        setupObservers()

        exerciseViewModel.getExerciseById(args.exerciseId)

        editingSetId?.let { setViewModel.loadSetDetail(it) }
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private fun setupToolbar() {
        binding.toolbar.title = if (editingSetId == null) "Nuevo Set" else "Editar Set"
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupSetTypeSpinner() {
        binding.spinnerSetType.adapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_item,
            SET_TYPE_LABELS
        ).also { it.setDropDownViewResource(R.layout.simple_spinner_dropdown_item) }
    }

    private fun setupParameterList() {
        parameterAdapter = ParameterAdapter(
            onItemClick = { param -> showAddParameterDialog(param) },
            onEditClick = {},
            onDeleteClick = {},
            showActions = false
        )
        binding.recyclerParameters.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = parameterAdapter
        }
    }

    private fun setupParameterSearch() {
        binding.chipAll.setOnClickListener { loadParameters() }
        binding.chipMy.setOnClickListener { loadMyParameters() }
        binding.chipNumber.setOnClickListener { loadParametersByType("NUMBER") }
        binding.chipInteger.setOnClickListener { loadParametersByType("INTEGER") }
        binding.chipDuration.setOnClickListener { loadParametersByType("DURATION") }

        binding.btnClearSearch.setOnClickListener {
            binding.etParameterSearch.text?.clear()
            loadParameters()
        }

        binding.etParameterSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                loadParameters(); true
            } else false
        }
    }

    private fun setupListeners() {
        binding.btnAddParameter.setOnClickListener {
            binding.layoutParameterSection.visibility = View.VISIBLE
            loadParameters()
        }

        binding.btnCloseParameters.setOnClickListener {
            binding.layoutParameterSection.visibility = View.GONE
        }

        binding.btnSaveSet.setOnClickListener { saveSet() }
    }

    // ── Observers ─────────────────────────────────────────────────────────────

    private fun setupObservers() {
        exerciseViewModel.exerciseDetailState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                supportedParameterIds = resource.data?.supportedParameterIds ?: emptySet()
                loadParameters()
            }
        }

        parameterViewModel.allParametersState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val all = resource.data?.content ?: emptyList()
                    val filtered = if (supportedParameterIds.isNotEmpty())
                        all.filter { supportedParameterIds.contains(it.id) }
                    else all

                    binding.tvNoParameters.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
                    binding.recyclerParameters.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
                    if (filtered.isNotEmpty()) parameterAdapter.updateList(filtered)
                }
                is Resource.Error -> showError(resource.message ?: "Error cargando parámetros")
                else -> {}
            }
        }

        setViewModel.setDetailState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> setFormEnabled(false)
                is Resource.Success -> {
                    setFormEnabled(true)
                    resource.data?.let { set ->
                        binding.etSetPosition.setText(set.position.toString())
                        binding.etRestAfterSet.setText(set.restAfterSet?.toString() ?: "")
                        binding.etSubSetNumber.setText(set.subSetNumber?.toString() ?: "")
                        binding.etGroupId.setText(set.groupId ?: "")

                        val idx = SET_TYPES.indexOf(set.setType)
                        if (idx >= 0) binding.spinnerSetType.setSelection(idx)

                        pendingParameters.clear()
                        set.parameters?.mapTo(pendingParameters) { param ->
                            UpdateSetParameterRequest(
                                id = param.id,
                                parameterId = param.parameterId,
                                repetitions = param.repetitions,
                                numericValue = param.numericValue,
                                integerValue = param.integerValue,
                                durationValue = param.durationValue
                            )
                        }
                        refreshParameterChips()
                    }
                    setViewModel.clearDetailState()
                }
                is Resource.Error -> {
                    setFormEnabled(true)
                    showError(resource.message ?: "Error cargando set")
                    setViewModel.clearDetailState()
                }
                null -> {}
                else -> {}
            }
        }

        setViewModel.saveState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> setFormEnabled(false)
                is Resource.Success -> {
                    setFormEnabled(true)
                    val msg = if (editingSetId == null) "✅ Set creado" else "✅ Set actualizado"
                    Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
                    setViewModel.clearSaveState()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    setFormEnabled(true)
                    showError(resource.message ?: "Error al guardar")
                    setViewModel.clearSaveState()
                }
                null -> {}
                else -> {}
            }
        }
    }

    // ── Guardar ───────────────────────────────────────────────────────────────

    private fun saveSet() {
        val position = binding.etSetPosition.text.toString().toIntOrNull() ?: 1
        val setType = SET_TYPES.getOrElse(binding.spinnerSetType.selectedItemPosition) { "NORMAL" }
        val restAfterSet = binding.etRestAfterSet.text.toString().toIntOrNull()
        val subSetNumber = binding.etSubSetNumber.text.toString().toIntOrNull()
        val groupId = binding.etGroupId.text.toString().takeIf { it.isNotEmpty() }

        if (editingSetId == null) {
            val params = pendingParameters.map { p ->
                SetParameterRequest(
                    parameterId = p.parameterId,
                    repetitions = p.repetitions,
                    numericValue = p.numericValue,
                    integerValue = p.integerValue,
                    durationValue = p.durationValue
                )
            }
            setViewModel.createSet(
                CreateSetTemplateRequest(
                    routineExerciseId = args.routineExerciseId,
                    position = position,
                    setType = setType,
                    restAfterSet = restAfterSet,
                    subSetNumber = subSetNumber,
                    groupId = groupId,
                    parameters = params.ifEmpty { null }
                )
            )
        } else {
            setViewModel.updateSet(
                editingSetId!!,
                UpdateSetTemplateRequest(
                    position = position,
                    setType = setType,
                    restAfterSet = restAfterSet,
                    subSetNumber = subSetNumber,
                    groupId = groupId,
                    parameters = pendingParameters.ifEmpty { null }
                )
            )
        }
    }

    // ── Parámetros ────────────────────────────────────────────────────────────

    private fun showAddParameterDialog(parameter: CustomParameterResponse) {
        val dialogBinding = DialogAddParameterBinding.inflate(layoutInflater)

        dialogBinding.tvParameterName.text = parameter.name
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
            else -> dialogBinding.layoutNumericValue.visibility = View.VISIBLE
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Añadir ${parameter.name}")
            .setView(dialogBinding.root)
            .setPositiveButton("Añadir") { _, _ ->
                val newParam = UpdateSetParameterRequest(
                    id = null,
                    parameterId = parameter.id,
                    repetitions = dialogBinding.etRepetitions.text.toString().toIntOrNull(),
                    numericValue = when (parameter.parameterType) {
                        "NUMBER", "DISTANCE", "PERCENTAGE" ->
                            dialogBinding.etNumericValue.text.toString().toDoubleOrNull()

                        else -> null
                    },
                    integerValue = when (parameter.parameterType) {
                        "INTEGER" -> dialogBinding.etIntegerValue.text.toString().toIntOrNull()
                        else -> null
                    },
                    durationValue = when (parameter.parameterType) {
                        "DURATION" -> dialogBinding.etDurationValue.text.toString().toLongOrNull()
                        else -> null
                    }
                )
                pendingParameters.add(newParam)
                refreshParameterChips()
                binding.layoutParameterSection.visibility = View.GONE
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun refreshParameterChips() {
        binding.chipGroupSelected.removeAllViews()
        if (pendingParameters.isEmpty()) {
            binding.tvNoSelectedParameters.visibility = View.VISIBLE
            binding.layoutSelectedParameters.visibility = View.GONE
        } else {
            binding.tvNoSelectedParameters.visibility = View.GONE
            binding.layoutSelectedParameters.visibility = View.VISIBLE
            pendingParameters.forEach { param ->
                val chip = Chip(requireContext()).apply {
                    text = "Parámetro ${param.parameterId}" +
                            (param.repetitions?.let { " × $it" } ?: "")
                    isCloseIconVisible = true
                    setOnCloseIconClickListener {
                        pendingParameters.remove(param)
                        refreshParameterChips()
                    }
                }
                binding.chipGroupSelected.addView(chip)
            }
        }
    }

    // ── Carga de parámetros ───────────────────────────────────────────────────

    private fun loadParameters(type: String? = null, onlyMine: Boolean = false) {
        parameterViewModel.searchAllParameters(
            CustomParameterFilterRequest(
                search = binding.etParameterSearch.text.toString().takeIf { it.isNotEmpty() },
                page = 0, size = 50, sortBy = "name", direction = "ASC",
                parameterType = type,
                onlyMine = onlyMine
            )
        )
    }

    private fun loadMyParameters() = loadParameters(onlyMine = true)
    private fun loadParametersByType(type: String) = loadParameters(type = type)

    // ── UI helpers ────────────────────────────────────────────────────────────

    private fun setFormEnabled(enabled: Boolean) {
        binding.progressBar.visibility = if (enabled) View.GONE else View.VISIBLE
        binding.btnSaveSet.isEnabled = enabled
    }

    private fun showError(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}