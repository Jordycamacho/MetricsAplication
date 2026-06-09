package com.fitapp.appfit.feature.routine.ui.exercises

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.R
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentAddExercisesToRoutineBinding
import com.fitapp.appfit.feature.exercise.ExerciseViewModel
import com.fitapp.appfit.feature.exercise.model.exercise.request.ExerciseFilterRequest
import com.fitapp.appfit.feature.exercise.model.exercise.response.ExerciseResponse
import com.fitapp.appfit.feature.exercise.ui.list.ExerciseAdapter
import com.fitapp.appfit.feature.routine.model.rutinexercise.request.AddExerciseToRoutineRequest
import com.fitapp.appfit.feature.routine.ui.RoutineExerciseViewModel
import com.fitapp.appfit.feature.routine.ui.RoutineViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import java.util.UUID

class AddExercisesToRoutineFragment : Fragment() {

    private var _binding: FragmentAddExercisesToRoutineBinding? = null
    private val binding get() = _binding!!

    private val args: AddExercisesToRoutineFragmentArgs by navArgs()
    private val exerciseViewModel: ExerciseViewModel by viewModels()
    private val routineExerciseViewModel: RoutineExerciseViewModel by viewModels()
    private val routineViewModel: RoutineViewModel by viewModels()

    private lateinit var exerciseAdapter: ExerciseAdapter
    private var selectedExercise: ExerciseResponse? = null
    private var editingRoutineExerciseId: Long? = null

    private val orderCounterByDay = mutableMapOf<String, Int>()
    private var orderInitialized = false
    private var sortedTrainingDays: List<String> = emptyList()
    private var pendingSaveNavigateBack: Boolean? = null

    private var currentCircuitGroupId: String? = null
    private var currentSuperSetGroupId: String? = null

    private val chipToDayMap = mapOf(
        R.id.chip_monday to "MONDAY",
        R.id.chip_tuesday to "TUESDAY",
        R.id.chip_wednesday to "WEDNESDAY",
        R.id.chip_thursday to "THURSDAY",
        R.id.chip_friday to "FRIDAY",
        R.id.chip_saturday to "SATURDAY",
        R.id.chip_sunday to "SUNDAY"
    )

    private val dayToChipMap = chipToDayMap.entries.associate { (chipId, day) -> day to chipId }

    private enum class SpecialMode { NONE, AMRAP, EMOM, TABATA }
    private var currentSpecialMode = SpecialMode.NONE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExercisesToRoutineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editingRoutineExerciseId = if (args.routineExerciseId == -1L) null else args.routineExerciseId

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupFilterChips()
        setupDayChips()
        setupGroupingSection()
        setupSpecialModeSection()
        setupExercisePicker()
        setupSaveButtons()
        setupObservers()
        prefillEditData()

        routineViewModel.getRoutine(args.routineId)
        routineExerciseViewModel.loadOrderBaseline(args.routineId)
        loadExercises()
    }

    private fun setupToolbar() {
        binding.toolbar.title = if (editingRoutineExerciseId == null) "Nuevo Ejercicio" else "Editar Ejercicio"
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupRecyclerView() {
        exerciseAdapter = ExerciseAdapter(onItemClick = { exercise -> selectExercise(exercise) })
        binding.recyclerExercises.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = exerciseAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener {
            val clearBtn = binding.root.findViewById<ImageButton>(R.id.btn_clear_search)
            clearBtn?.visibility = if (it.isNullOrEmpty()) View.GONE else View.VISIBLE
            loadExercises()
        }
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                loadExercises()
                true
            } else false
        }
        binding.root.findViewById<ImageButton>(R.id.btn_clear_search)?.setOnClickListener {
            binding.etSearch.text?.clear()
        }
    }

    private fun setupFilterChips() {
        binding.chipAll.setOnClickListener { loadExercises() }
        binding.chipMy.setOnClickListener { loadMyExercises() }
        binding.chipAvailable.setOnClickListener { loadAvailableExercises() }
    }

    private fun setupDayChips() {
        binding.chipGroupDays.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val day = chipToDayMap[checkedId] ?: return@setOnCheckedStateChangeListener
            updateOrderUiForDay(day)
        }
    }

    private fun setupExercisePicker() {
        binding.btnToggleExercisePicker.setOnClickListener { toggleExercisePicker() }
        if (editingRoutineExerciseId == null) {
            binding.cardExercisePicker.isVisible = true
            binding.btnToggleExercisePicker.text = "Elegir ejercicio"
        }
    }

    private fun toggleExercisePicker() {
        val show = !binding.cardExercisePicker.isVisible
        binding.cardExercisePicker.isVisible = show
        binding.btnToggleExercisePicker.text = if (show) {
            "Ocultar lista"
        } else if (selectedExercise != null) {
            "Cambiar ejercicio"
        } else {
            "Elegir ejercicio"
        }
    }

    private fun setupSaveButtons() {
        binding.btnSaveContinue.setOnClickListener { saveExercise(navigateBack = false) }
        binding.btnSaveAndExit.setOnClickListener { saveExercise(navigateBack = true) }

        if (editingRoutineExerciseId != null) {
            binding.btnSaveContinue.text = "GUARDAR CAMBIOS"
            binding.btnSaveAndExit.text = "Guardar y salir"
        }
    }

    private fun prefillEditData() {
        if (editingRoutineExerciseId == null) return

        if (args.exerciseId != -1L && args.exerciseName.isNotBlank()) {
            selectedExercise = ExerciseResponse(
                id = args.exerciseId,
                name = args.exerciseName,
                description = null,
                exerciseType = null,
                createdById = null,
                isActive = null,
                isPublic = null,
                usageCount = null,
                rating = null,
                ratingCount = null,
                createdAt = null,
                updatedAt = null,
                lastUsedAt = null
            )
            updateSelectedExerciseUi()
        }

        val day = args.dayOfWeek
        if (day.isNotBlank()) {
            dayToChipMap[day]?.let { binding.chipGroupDays.check(it) }
            binding.etSessionOrder.text = args.sessionOrder.takeIf { it > 0 }?.toString() ?: "1"
        }

        binding.etRestAfter.setText(args.restAfterExercise.takeIf { it > 0 }?.toString() ?: "60")
        binding.tvExerciseCounterBadge.text =
            "EJERCICIO #${args.sessionOrder.takeIf { it > 0 } ?: 1}"

        val allowedDays = args.trainingDays
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (allowedDays.isNotEmpty()) {
            setupDayChipsForRoutine(allowedDays)
        }
    }

    private fun setupGroupingSection() {
        binding.btnToggleGrouping.setOnClickListener {
            val visible = binding.layoutGroupingContent.isVisible
            binding.layoutGroupingContent.isVisible = !visible
            binding.btnToggleGrouping.text =
                if (visible) "▶ Agrupación (opcional)" else "▼ Agrupación (opcional)"
        }

        binding.chipGroupNone.setOnCheckedChangeListener { _, checked ->
            if (checked) setGroupingType(GroupingType.NONE)
        }
        binding.chipGroupCircuit.setOnCheckedChangeListener { _, checked ->
            if (checked) setGroupingType(GroupingType.CIRCUIT)
        }
        binding.chipGroupSuperset.setOnCheckedChangeListener { _, checked ->
            if (checked) setGroupingType(GroupingType.SUPERSET)
        }

        binding.btnNewGroup.setOnClickListener {
            when (getGroupingType()) {
                GroupingType.CIRCUIT -> {
                    currentCircuitGroupId = generateGroupId()
                    showGroupIdHint(currentCircuitGroupId)
                }
                GroupingType.SUPERSET -> {
                    currentSuperSetGroupId = generateGroupId()
                    showGroupIdHint(currentSuperSetGroupId)
                }
                GroupingType.NONE -> Unit
            }
        }
    }

    private fun setupSpecialModeSection() {
        binding.btnToggleSpecialMode.setOnClickListener {
            val visible = binding.layoutSpecialModeContent.isVisible
            binding.layoutSpecialModeContent.isVisible = !visible
            binding.btnToggleSpecialMode.text =
                if (visible) "▶ Modo especial (opcional)" else "▼ Modo especial (opcional)"
        }

        binding.chipModeNone.setOnCheckedChangeListener { _, checked ->
            if (checked) applySpecialMode(SpecialMode.NONE)
        }
        binding.chipModeAmrap.setOnCheckedChangeListener { _, checked ->
            if (checked) applySpecialMode(SpecialMode.AMRAP)
        }
        binding.chipModeEmom.setOnCheckedChangeListener { _, checked ->
            if (checked) applySpecialMode(SpecialMode.EMOM)
        }
        binding.chipModeTabata.setOnCheckedChangeListener { _, checked ->
            if (checked) applySpecialMode(SpecialMode.TABATA)
        }
        applySpecialMode(SpecialMode.NONE)
    }

    private fun setupObservers() {
        exerciseViewModel.allExercisesState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading()
                is Resource.Success -> {
                    hideLoading()
                    exerciseAdapter.setExercises(resource.data?.content ?: emptyList())
                }
                is Resource.Error -> {
                    hideLoading()
                    showError(resource.message ?: "Error cargando ejercicios")
                }
                else -> Unit
            }
        }

        routineViewModel.routineDetailState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                resource.data?.let { routine ->
                    val dayOrder = mapOf(
                        "MONDAY" to 1, "TUESDAY" to 2, "WEDNESDAY" to 3,
                        "THURSDAY" to 4, "FRIDAY" to 5, "SATURDAY" to 6, "SUNDAY" to 7
                    )
                    sortedTrainingDays = (routine.trainingDays ?: emptyList())
                        .sortedBy { dayOrder[it] ?: 8 }
                    if (editingRoutineExerciseId == null) {
                        setupDayChipsForRoutine(sortedTrainingDays)
                    }
                }
            }
        }

        routineExerciseViewModel.orderBaselineState.observe(viewLifecycleOwner) { baseline ->
            if (editingRoutineExerciseId != null || orderInitialized) return@observe
            orderCounterByDay.clear()
            orderCounterByDay.putAll(baseline)
            val day = getSelectedDay()
            if (day != null) updateOrderUiForDay(day)
            orderInitialized = true
        }

        routineExerciseViewModel.saveState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> setFormEnabled(false)
                is Resource.Success -> {
                    setFormEnabled(true)
                    routineExerciseViewModel.clearSaveState()

                    if (editingRoutineExerciseId != null || pendingSaveNavigateBack == true) {
                        Snackbar.make(
                            binding.root,
                            if (editingRoutineExerciseId != null) "✅ Ejercicio actualizado" else "✅ Ejercicio guardado",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        findNavController().navigateUp()
                    } else {
                        val day = getSelectedDay()
                        if (day != null) {
                            val current = orderCounterByDay[day] ?: 1
                            orderCounterByDay[day] = current + 1
                            updateOrderUiForDay(day)
                        }
                        clearExerciseSelection()
                        showSavedFeedback()
                    }
                    pendingSaveNavigateBack = null
                }
                is Resource.Error -> {
                    setFormEnabled(true)
                    showError(resource.message ?: "Error al guardar")
                    routineExerciseViewModel.clearSaveState()
                    pendingSaveNavigateBack = null
                }
                null -> Unit
                else -> Unit
            }
        }
    }

    private fun saveExercise(navigateBack: Boolean) {
        pendingSaveNavigateBack = navigateBack

        val exercise = selectedExercise ?: run {
            showError("Selecciona un ejercicio primero")
            binding.cardExercisePicker.isVisible = true
            return
        }

        val selectedDay = getSelectedDay() ?: run {
            showError("Selecciona un día de entrenamiento")
            return
        }

        val sessionOrder = binding.etSessionOrder.text.toString().toIntOrNull()
        if (sessionOrder == null || sessionOrder < 1) {
            showError("Orden inválido")
            return
        }

        val restAfter = binding.etRestAfter.text.toString().toIntOrNull() ?: 60
        val groupingType = getGroupingType()

        val circuitGroupId = if (groupingType == GroupingType.CIRCUIT) {
            currentCircuitGroupId ?: generateGroupId().also { currentCircuitGroupId = it }
        } else null

        val circuitRoundCount = if (groupingType == GroupingType.CIRCUIT) {
            binding.etCircuitRounds.text.toString().toIntOrNull()?.takeIf { it > 0 } ?: run {
                showError("Rondas de circuito inválidas")
                return
            }
        } else null

        val superSetGroupId = if (groupingType == GroupingType.SUPERSET) {
            currentSuperSetGroupId ?: generateGroupId().also { currentSuperSetGroupId = it }
        } else null

        val amrapDuration = if (currentSpecialMode == SpecialMode.AMRAP) {
            binding.etAmrapDuration.text.toString().toIntOrNull()?.takeIf { it > 0 } ?: run {
                showError("Duración AMRAP inválida")
                return
            }
        } else null

        val emomInterval = if (currentSpecialMode == SpecialMode.EMOM) {
            binding.etEmomInterval.text.toString().toIntOrNull()?.takeIf { it > 0 } ?: run {
                showError("Intervalo EMOM inválido")
                return
            }
        } else null

        val emomRounds = if (currentSpecialMode == SpecialMode.EMOM) {
            binding.etEmomRounds.text.toString().toIntOrNull()?.takeIf { it > 0 } ?: run {
                showError("Rondas EMOM inválidas")
                return
            }
        } else null

        val tabataWork = if (currentSpecialMode == SpecialMode.TABATA) {
            binding.etTabataWork.text.toString().toIntOrNull()?.takeIf { it > 0 } ?: run {
                showError("Tiempo trabajo Tabata inválido")
                return
            }
        } else null

        val tabataRest = if (currentSpecialMode == SpecialMode.TABATA) {
            binding.etTabataRest.text.toString().toIntOrNull()?.takeIf { it > 0 } ?: run {
                showError("Tiempo descanso Tabata inválido")
                return
            }
        } else null

        val tabataRounds = if (currentSpecialMode == SpecialMode.TABATA) {
            binding.etTabataRounds.text.toString().toIntOrNull()?.takeIf { it > 0 } ?: run {
                showError("Rondas Tabata inválidas")
                return
            }
        } else null

        val notes = binding.etNotes.text.toString().takeIf { it.isNotBlank() }

        routineExerciseViewModel.saveExercise(
            args.routineId,
            editingRoutineExerciseId,
            AddExerciseToRoutineRequest(
                exerciseId = exercise.id,
                sessionNumber = null,
                dayOfWeek = selectedDay,
                sessionOrder = sessionOrder,
                restAfterExercise = restAfter,
                targetParameters = null,
                sets = null,
                circuitGroupId = circuitGroupId,
                circuitRoundCount = circuitRoundCount,
                superSetGroupId = superSetGroupId,
                amrapDurationSeconds = amrapDuration,
                emomIntervalSeconds = emomInterval,
                emomTotalRounds = emomRounds,
                tabataWorkSeconds = tabataWork,
                tabataRestSeconds = tabataRest,
                tabataRounds = tabataRounds,
                notes = notes
            )
        )
    }

    private fun selectExercise(exercise: ExerciseResponse) {
        selectedExercise = exercise
        updateSelectedExerciseUi()
        binding.cardExercisePicker.isVisible = false
        binding.btnToggleExercisePicker.text = "Cambiar ejercicio"
        Snackbar.make(binding.root, "✓ ${exercise.name}", Snackbar.LENGTH_SHORT).show()
    }

    private fun updateSelectedExerciseUi() {
        binding.tvSelectedExerciseName.text = selectedExercise?.name ?: "Toca abajo para elegir un ejercicio"
    }

    private fun clearExerciseSelection() {
        selectedExercise = null
        updateSelectedExerciseUi()
        binding.cardExercisePicker.isVisible = true
        binding.btnToggleExercisePicker.text = "Elegir ejercicio"
    }

    private fun setupDayChipsForRoutine(days: List<String>) {
        val daySet = days.toSet()
        val showAll = daySet.isEmpty()

        chipToDayMap.forEach { (chipId, day) ->
            val chip = binding.chipGroupDays.findViewById<Chip>(chipId)
            chip?.visibility = if (showAll || day in daySet) View.VISIBLE else View.GONE
        }

        if (binding.chipGroupDays.checkedChipId == View.NO_ID) {
            val firstChipId = chipToDayMap.entries
                .firstOrNull { (_, day) -> showAll || day in daySet }
                ?.key
            if (firstChipId != null) binding.chipGroupDays.check(firstChipId)
        }
    }

    private fun getSelectedDay(): String? = chipToDayMap[binding.chipGroupDays.checkedChipId]

    private fun updateOrderUiForDay(day: String) {
        val nextOrder = if (editingRoutineExerciseId != null && args.dayOfWeek == day) {
            args.sessionOrder.takeIf { it > 0 } ?: orderCounterByDay.getOrDefault(day, 1)
        } else {
            orderCounterByDay.getOrDefault(day, 1)
        }
        binding.etSessionOrder.text = nextOrder.toString()
        binding.tvExerciseCounterBadge.text = "EJERCICIO #$nextOrder"
    }

    private fun showSavedFeedback() {
        val badge = binding.tvExerciseCounterBadge
        val savedOrder = (binding.etSessionOrder.text.toString().toIntOrNull() ?: 1) - 1
        badge.animate().alpha(0f).setDuration(100).withEndAction {
            badge.text = "✓ EJERCICIO #$savedOrder guardado"
            badge.animate().alpha(1f).setDuration(200).withEndAction {
                badge.postDelayed({
                    val next = binding.etSessionOrder.text.toString().toIntOrNull() ?: 1
                    badge.text = "EJERCICIO #$next"
                }, 1000)
            }.start()
        }.start()
    }

    private fun loadExercises() = exerciseViewModel.searchExercises(buildFilter())
    private fun loadMyExercises() = exerciseViewModel.searchMyExercises(buildFilter())
    private fun loadAvailableExercises() = exerciseViewModel.searchAvailableExercises(buildFilter())

    private fun buildFilter() = ExerciseFilterRequest(
        search = binding.etSearch.text.toString().takeIf { it.isNotEmpty() },
        page = 0,
        size = 50,
        sortBy = "name",
        direction = ExerciseFilterRequest.SortDirection.ASC
    )

    private enum class GroupingType { NONE, CIRCUIT, SUPERSET }

    private fun setGroupingType(type: GroupingType) {
        binding.layoutCircuitRounds.isVisible = type == GroupingType.CIRCUIT
        binding.tvGroupIdHint.isVisible = type != GroupingType.NONE
        when (type) {
            GroupingType.CIRCUIT -> showGroupIdHint(currentCircuitGroupId)
            GroupingType.SUPERSET -> showGroupIdHint(currentSuperSetGroupId)
            GroupingType.NONE -> Unit
        }
    }

    private fun getGroupingType(): GroupingType = when {
        binding.chipGroupCircuit.isChecked -> GroupingType.CIRCUIT
        binding.chipGroupSuperset.isChecked -> GroupingType.SUPERSET
        else -> GroupingType.NONE
    }

    private fun applySpecialMode(mode: SpecialMode) {
        currentSpecialMode = mode
        binding.layoutAmrap.isVisible = mode == SpecialMode.AMRAP
        binding.layoutEmom.isVisible = mode == SpecialMode.EMOM
        binding.layoutTabata.isVisible = mode == SpecialMode.TABATA
    }

    private fun showGroupIdHint(groupId: String?) {
        binding.tvGroupIdHint.text = if (groupId != null) {
            "Grupo activo: …${groupId.takeLast(6)}"
        } else {
            "Se creará un nuevo grupo al guardar"
        }
        binding.tvGroupIdHint.isVisible = true
    }

    private fun generateGroupId(): String = UUID.randomUUID().toString()

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun setFormEnabled(enabled: Boolean) {
        binding.progressBar.visibility = if (enabled) View.GONE else View.VISIBLE
        binding.btnSaveContinue.isEnabled = enabled
        binding.btnSaveAndExit.isEnabled = enabled
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
