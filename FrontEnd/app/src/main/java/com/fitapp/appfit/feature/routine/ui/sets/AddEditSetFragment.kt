package com.fitapp.appfit.feature.routine.ui.sets

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
    private var nextPosition: Int = 1
    private var positionInitialized: Boolean = false

    private var retainedTypeIndex: Int = 0
    private var retainedGroupId: String = ""
    private var retainedRest: String = ""
    private var retainedSubSetBase: Int? = null

    private var pendingSaveNavigateBack: Boolean? = null

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

    // ── Ciclo de vida ─────────────────────────────────────────────────────────

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

        if (editingSetId != null) {
            setViewModel.loadSetDetail(editingSetId!!)
        } else if (!positionInitialized) {
            setViewModel.loadSetCountForExercise(args.routineExerciseId)
        } else {
            refreshPositionUI()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private fun setupToolbar() {
        binding.toolbar.title = if (editingSetId == null) "Nuevo Set" else "Editar Set"
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupSetTypeSpinner() {
        binding.spinnerSetType.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            SET_TYPE_LABELS
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

    private fun setupParameterList() {
        parameterAdapter = ParameterAdapter(
            onItemClick    = { param -> showAddParameterDialog(param) },
            onEditClick    = {},
            onDeleteClick  = {},
            showActions    = false
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
                loadParameters()
                true
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

        binding.btnSaveSet.setOnClickListener {
            if (editingSetId == null) captureRetainedFields()
            saveSet(navigateBack = false)
        }

        binding.btnSaveAndExit?.setOnClickListener {
            saveSet(navigateBack = true)
        }
    }

    // ── Observers ─────────────────────────────────────────────────────────────

    private fun setupObservers() {

        setViewModel.setCountState.observe(viewLifecycleOwner) { count ->
            if (!positionInitialized) {
                nextPosition = (count ?: 0) + 1
                positionInitialized = true
                refreshPositionUI()
            }
        }

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

                    binding.tvNoParameters.visibility =
                        if (filtered.isEmpty()) View.VISIBLE else View.GONE
                    binding.recyclerParameters.visibility =
                        if (filtered.isEmpty()) View.GONE else View.VISIBLE
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
                                id            = param.id,
                                parameterId   = param.parameterId,
                                repetitions   = param.repetitions,
                                numericValue  = param.numericValue,
                                integerValue  = param.integerValue,
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
                null  -> {}
                else  -> {}
            }
        }

        setViewModel.saveState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> setFormEnabled(false)

                is Resource.Success -> {
                    setFormEnabled(true)
                    setViewModel.clearSaveState()

                    if (editingSetId != null) {
                        Snackbar.make(binding.root, "✅ Set actualizado", Snackbar.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                        return@observe
                    }

                    if (pendingSaveNavigateBack == true) {
                        Snackbar.make(binding.root, "✅ Set #$nextPosition creado", Snackbar.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    } else {
                        nextPosition++
                        retainedSubSetBase = retainedSubSetBase?.plus(1)
                        restoreRetainedFields()

                        pendingParameters.clear()
                        refreshParameterChips()

                        showSavedFeedback()
                    }

                    pendingSaveNavigateBack = null
                }

                is Resource.Error -> {
                    setFormEnabled(true)
                    showError(resource.message ?: "Error al guardar")
                    setViewModel.clearSaveState()
                    pendingSaveNavigateBack = null
                }
                null -> {}
                else -> {}
            }
        }
    }

    // ── Guardar ───────────────────────────────────────────────────────────────

    private fun saveSet(navigateBack: Boolean) {
        pendingSaveNavigateBack = navigateBack

        val position    = if (editingSetId == null) nextPosition
        else binding.etSetPosition.text.toString().toIntOrNull() ?: 1
        val setType     = SET_TYPES.getOrElse(binding.spinnerSetType.selectedItemPosition) { "NORMAL" }
        val restAfterSet = binding.etRestAfterSet.text.toString().toIntOrNull()
        val subSetNumber = binding.etSubSetNumber.text.toString().toIntOrNull()
        val groupId     = binding.etGroupId.text.toString().takeIf { it.isNotEmpty() }

        if (editingSetId == null) {
            val params = pendingParameters.map { p ->
                SetParameterRequest(
                    parameterId   = p.parameterId,
                    repetitions   = p.repetitions,
                    numericValue  = p.numericValue,
                    integerValue  = p.integerValue,
                    durationValue = p.durationValue
                )
            }
            setViewModel.createSet(
                CreateSetTemplateRequest(
                    routineExerciseId = args.routineExerciseId,
                    position     = position,
                    setType      = setType,
                    restAfterSet = restAfterSet,
                    subSetNumber = subSetNumber,
                    groupId      = groupId,
                    parameters   = params.ifEmpty { null }
                )
            )
        } else {
            setViewModel.updateSet(
                editingSetId!!,
                UpdateSetTemplateRequest(
                    position     = position,
                    setType      = setType,
                    restAfterSet = restAfterSet,
                    subSetNumber = subSetNumber,
                    groupId      = groupId,
                    parameters   = pendingParameters.ifEmpty { null }
                )
            )
        }
    }

    // ── Retención de campos ───────────────────────────────────────────────────

    private fun captureRetainedFields() {
        retainedTypeIndex  = binding.spinnerSetType.selectedItemPosition
        retainedGroupId    = binding.etGroupId.text.toString()
        retainedRest       = binding.etRestAfterSet.text.toString()
        retainedSubSetBase = binding.etSubSetNumber.text.toString().toIntOrNull()
    }

    private fun restoreRetainedFields() {
        refreshPositionUI()
        binding.spinnerSetType.setSelection(retainedTypeIndex)
        binding.etGroupId.setText(retainedGroupId)
        binding.etRestAfterSet.setText(retainedRest)
        binding.etSubSetNumber.setText(retainedSubSetBase?.toString() ?: "")
    }

    // ── Diálogo de parámetros (MEJORADO) ──────────────────────────────────────

    private fun showAddParameterDialog(parameter: CustomParameterResponse) {
        val dialogBinding = DialogAddParameterBinding.inflate(layoutInflater)

        // Configurar header
        dialogBinding.tvParameterName.text = parameter.name
        dialogBinding.tvParameterType.text = parameter.parameterType ?: "Tipo desconocido"

        if (!parameter.unit.isNullOrEmpty()) {
            dialogBinding.tvParameterUnit.text = parameter.unit
            dialogBinding.tvParameterUnit.visibility = View.VISIBLE
        }

        // Mostrar campos según el tipo de parámetro
        when (parameter.parameterType) {
            "REPETITIONS" -> {
                // Solo repeticiones
                dialogBinding.layoutRepetitions.visibility = View.VISIBLE
                dialogBinding.layoutHelperInfo.visibility = View.VISIBLE
                dialogBinding.tvHelperText.text = "Ingresa solo el número de repeticiones"
            }
            "NUMBER", "DISTANCE", "PERCENTAGE" -> {
                // Valor numérico decimal
                dialogBinding.layoutNumericValue.visibility = View.VISIBLE
                dialogBinding.layoutHelperInfo.visibility = View.VISIBLE
                dialogBinding.tvHelperText.text = "Ingresa el valor en ${parameter.unit ?: "unidades"}"
            }
            "INTEGER" -> {
                // Valor entero
                dialogBinding.layoutIntegerValue.visibility = View.VISIBLE
                dialogBinding.layoutHelperInfo.visibility = View.VISIBLE
                dialogBinding.tvHelperText.text = "Ingresa un número entero"
            }
            "DURATION" -> {
                // Duración en segundos
                dialogBinding.layoutDurationValue.visibility = View.VISIBLE
                dialogBinding.layoutHelperInfo.visibility = View.VISIBLE
                dialogBinding.tvHelperText.text = "Ingresa duración en segundos"
            }
            else -> {
                // Por defecto: numérico
                dialogBinding.layoutNumericValue.visibility = View.VISIBLE
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Añadir ${parameter.name}")
            .setView(dialogBinding.root)
            .setPositiveButton("Añadir") { _, _ ->
                // Validar que al menos un valor está presente
                val repetitions = dialogBinding.etRepetitions.text.toString().toIntOrNull()
                val numericValue = dialogBinding.etNumericValue.text.toString().toDoubleOrNull()
                val integerValue = dialogBinding.etIntegerValue.text.toString().toIntOrNull()
                val durationValue = dialogBinding.etDurationValue.text.toString().toLongOrNull()

                if (repetitions == null && numericValue == null && integerValue == null && durationValue == null) {
                    showError("Por favor ingresa un valor")
                    return@setPositiveButton
                }

                // Crear nuevo parámetro
                val newParam = UpdateSetParameterRequest(
                    id            = null,
                    parameterId   = parameter.id,
                    repetitions   = repetitions,
                    numericValue  = numericValue,
                    integerValue  = integerValue,
                    durationValue = durationValue
                )

                // Verificar si ya existe un parámetro con este ID
                val exists = pendingParameters.any { it.parameterId == parameter.id }
                if (exists) {
                    showError("Este parámetro ya ha sido agregado al set")
                    return@setPositiveButton
                }

                pendingParameters.add(newParam)
                refreshParameterChips()
                binding.layoutParameterSection.visibility = View.GONE
                Snackbar.make(binding.root, "✓ ${parameter.name} añadido", Snackbar.LENGTH_SHORT).show()
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
                val displayText = buildParameterDisplayText(param)

                val chip = Chip(requireContext()).apply {
                    text = displayText
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

    private fun buildParameterDisplayText(param: UpdateSetParameterRequest): String {
        return when {
            param.repetitions != null -> "Reps: ${param.repetitions}"
            param.numericValue != null -> "Valor: ${param.numericValue}"
            param.integerValue != null -> "Valor: ${param.integerValue}"
            param.durationValue != null -> "Duración: ${param.durationValue}s"
            else -> "Parámetro"
        }
    }

    // ── Carga de parámetros ───────────────────────────────────────────────────

    private fun loadParameters(type: String? = null, onlyMine: Boolean = false) {
        parameterViewModel.searchAllParameters(
            CustomParameterFilterRequest(
                search        = binding.etParameterSearch.text.toString().takeIf { it.isNotEmpty() },
                page          = 0, size = 50, sortBy = "name", direction = "ASC",
                parameterType = type,
                onlyMine      = onlyMine
            )
        )
    }

    private fun loadMyParameters() = loadParameters(onlyMine = true)
    private fun loadParametersByType(type: String) = loadParameters(type = type)

    // ── UI helpers ────────────────────────────────────────────────────────────

    private fun refreshPositionUI() {
        binding.etSetPosition.setText(nextPosition.toString())
        binding.tvSetCounterBadge?.text = "SET #$nextPosition"
    }

    private fun showSavedFeedback() {
        val badge = binding.tvSetCounterBadge
        if (badge == null) {
            Snackbar.make(
                binding.root,
                "✅ Set #${nextPosition - 1} guardado",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        badge.animate().alpha(0f).setDuration(100).withEndAction {
            badge.text = "✓ SET #${nextPosition - 1} guardado"
            badge.animate().alpha(1f).setDuration(200).withEndAction {
                badge.postDelayed({ badge.text = "SET #$nextPosition" }, 1000)
            }.start()
        }.start()
    }

    private fun setFormEnabled(enabled: Boolean) {
        binding.progressBar.visibility    = if (enabled) View.GONE else View.VISIBLE
        binding.btnSaveSet.isEnabled       = enabled
        binding.btnSaveAndExit?.isEnabled  = enabled
    }

    private fun showError(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
    }
}