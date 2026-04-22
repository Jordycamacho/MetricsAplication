package com.fitapp.appfit.feature.routine.ui.exercises

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.R
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentAddExercisesToRoutineBinding
import com.fitapp.appfit.feature.routine.model.rutinexercise.request.AddExerciseToRoutineRequest
import com.fitapp.appfit.feature.exercise.ExerciseViewModel
import com.fitapp.appfit.feature.routine.ui.RoutineExerciseViewModel
import com.fitapp.appfit.feature.routine.ui.RoutineViewModel
import com.fitapp.appfit.feature.exercise.model.exercise.request.ExerciseFilterRequest
import com.fitapp.appfit.feature.exercise.model.exercise.response.ExerciseResponse
import com.fitapp.appfit.feature.exercise.ui.list.ExerciseAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import java.util.UUID

class AddExercisesToRoutineFragment : Fragment() {

    private var _binding: FragmentAddExercisesToRoutineBinding? = null
    private val binding get() = _binding!!

    private val exerciseViewModel: ExerciseViewModel by viewModels()
    private val routineExerciseViewModel: RoutineExerciseViewModel by viewModels()
    private val routineViewModel: RoutineViewModel by viewModels()

    private lateinit var exerciseAdapter: ExerciseAdapter
    private var selectedExercise: ExerciseResponse? = null
    private var routineId: Long = 0

    // Auto-incremento de orden por día (igual que sessionOrder existente)
    private val orderCounterByDay = mutableMapOf<String, Int>()
    private var sortedTrainingDays: List<String> = emptyList()

    // Auto-incremento de groupId por tipo de agrupación:
    // circuitGroupId y superSetGroupId se auto-generan y son persistentes mientras
    // el usuario siga en el mismo "grupo". Se resetean al cambiar de tipo o manualmente.
    private var currentCircuitGroupId: String? = null
    private var currentSuperSetGroupId: String? = null

    private val chipToDayMap = mapOf(
        R.id.chip_monday    to "MONDAY",
        R.id.chip_tuesday   to "TUESDAY",
        R.id.chip_wednesday to "WEDNESDAY",
        R.id.chip_thursday  to "THURSDAY",
        R.id.chip_friday    to "FRIDAY",
        R.id.chip_saturday  to "SATURDAY",
        R.id.chip_sunday    to "SUNDAY"
    )

    // Modos especiales mutuamente excluyentes
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

        routineId = arguments?.getLong("routineId") ?: 0
        if (routineId == 0L) {
            findNavController().navigateUp()
            return
        }

        adjustFabForBottomNav()
        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupFilterChips()
        setupDayChips()
        setupGroupingSection()
        setupSpecialModeSection()
        setupAddButton()
        setupObservers()

        loadExercises()
        routineViewModel.getRoutine(routineId)
    }

    private fun adjustFabForBottomNav() {
        val density = resources.displayMetrics.density
        val bottomNavHeightPx = (56 * density).toInt()
        val extraMarginPx = (20 * density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(binding.btnAddExercise) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = v.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = bottomNavHeightPx + systemBars.bottom + extraMarginPx
            v.layoutParams = params
            insets
        }
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private fun setupToolbar() {
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
            binding.btnClearSearch.visibility = if (it.isNullOrEmpty()) View.GONE else View.VISIBLE
            loadExercises()
        }
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { loadExercises(); true } else false
        }
        binding.btnClearSearch.setOnClickListener { binding.etSearch.text?.clear() }
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
            updateOrderFieldForDay(day)
        }
    }

    // ── Agrupación (Circuito / Superset) ─────────────────────────────────────

    private fun setupGroupingSection() {
        // Toggle visibilidad del panel
        binding.btnToggleGrouping.setOnClickListener {
            val isVisible = binding.layoutGroupingContent.isVisible
            binding.layoutGroupingContent.isVisible = !isVisible
            binding.btnToggleGrouping.text = if (isVisible) "▶ Agrupación" else "▼ Agrupación"
        }

        // Tipo de agrupación: ninguno / circuito / superset
        binding.chipGroupNone.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                setGroupingType(GroupingType.NONE)
            }
        }
        binding.chipGroupCircuit.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                setGroupingType(GroupingType.CIRCUIT)
            }
        }
        binding.chipGroupSuperset.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                setGroupingType(GroupingType.SUPERSET)
            }
        }

        // Botón "nuevo grupo": genera un UUID nuevo para el tipo activo,
        // igual que el auto-incremento de sessionOrder al cambiar de día.
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
                GroupingType.NONE -> { /* noop */ }
            }
        }

        // Rondas de circuito: sólo visible cuando es circuito
        binding.layoutCircuitRounds.isVisible = false
        binding.tvGroupIdHint.isVisible = false
    }

    // ── Modos especiales (AMRAP / EMOM / TABATA) ─────────────────────────────

    private fun setupSpecialModeSection() {
        binding.btnToggleSpecialMode.setOnClickListener {
            val isVisible = binding.layoutSpecialModeContent.isVisible
            binding.layoutSpecialModeContent.isVisible = !isVisible
            binding.btnToggleSpecialMode.text =
                if (isVisible) "▶ Modo especial" else "▼ Modo especial"
        }

        // Chips de modo especial son mutuamente excluyentes
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

        // Estado inicial
        applySpecialMode(SpecialMode.NONE)
    }

    private fun applySpecialMode(mode: SpecialMode) {
        currentSpecialMode = mode
        binding.layoutAmrap.isVisible = mode == SpecialMode.AMRAP
        binding.layoutEmom.isVisible = mode == SpecialMode.EMOM
        binding.layoutTabata.isVisible = mode == SpecialMode.TABATA
    }

    private fun setupAddButton() {
        binding.btnAddExercise.setOnClickListener { addExerciseToRoutine() }
    }

    // ── Observers ─────────────────────────────────────────────────────────────

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
                else -> {}
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
                    setupDayChipsForRoutine(sortedTrainingDays)
                }
            }
        }

        routineExerciseViewModel.addExerciseState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> setFormEnabled(false)
                is Resource.Success -> {
                    setFormEnabled(true)
                    val name = selectedExercise?.name ?: "Ejercicio"

                    // Auto-incremento de orden por día (igual que antes)
                    val currentDay = getSelectedDay()
                    if (currentDay != null) {
                        val current = orderCounterByDay[currentDay] ?: 1
                        orderCounterByDay[currentDay] = current + 1
                        updateOrderFieldForDay(currentDay)
                    }

                    Snackbar.make(binding.root, "✅ $name añadido", Snackbar.LENGTH_LONG)
                        .setAction("Volver") { findNavController().navigateUp() }
                        .show()

                    clearSelection()
                    routineExerciseViewModel.clearAddState()
                }
                is Resource.Error -> {
                    setFormEnabled(true)
                    showError(resource.message ?: "Error al añadir ejercicio")
                    routineExerciseViewModel.clearAddState()
                }
                null -> {}
                else -> {}
            }
        }
    }

    // ── Carga de ejercicios ───────────────────────────────────────────────────

    private fun loadExercises() { exerciseViewModel.searchExercises(buildFilter()) }
    private fun loadMyExercises() { exerciseViewModel.searchMyExercises(buildFilter()) }
    private fun loadAvailableExercises() { exerciseViewModel.searchAvailableExercises(buildFilter()) }

    private fun buildFilter() = ExerciseFilterRequest(
        search = binding.etSearch.text.toString().takeIf { it.isNotEmpty() },
        page = 0, size = 50, sortBy = "name",
        direction = ExerciseFilterRequest.SortDirection.ASC
    )

    // ── Selección y añadir ────────────────────────────────────────────────────

    private fun selectExercise(exercise: ExerciseResponse) {
        selectedExercise = exercise
        binding.btnAddExercise.show()
        binding.btnAddExercise.text = "Añadir: ${exercise.name}"
    }

    private fun clearSelection() {
        selectedExercise = null
        binding.btnAddExercise.hide()
        binding.btnAddExercise.text = "Añadir ejercicio"
    }

    private fun addExerciseToRoutine() {
        val exercise = selectedExercise ?: run {
            showError("Selecciona un ejercicio primero")
            return
        }

        val selectedDay = getSelectedDay() ?: run {
            showError("Selecciona un día de entrenamiento")
            return
        }

        val sessionOrder = binding.etSessionOrder.text.toString().toIntOrNull()
        if (sessionOrder == null || sessionOrder < 1) {
            binding.etSessionOrder.error = "Orden inválido"
            binding.etSessionOrder.requestFocus()
            return
        }

        val restAfter = binding.etRestAfter.text.toString().toIntOrNull() ?: 60

        // ── Agrupación ────────────────────────────────────────────────────────
        val groupingType = getGroupingType()
        val circuitGroupId = if (groupingType == GroupingType.CIRCUIT) {
            // Si no hay grupo activo todavía, auto-genera uno
            currentCircuitGroupId ?: generateGroupId().also { currentCircuitGroupId = it }
        } else null

        val circuitRoundCount = if (groupingType == GroupingType.CIRCUIT) {
            binding.etCircuitRounds.text.toString().toIntOrNull()?.takeIf { it > 0 } ?: run {
                binding.etCircuitRounds.error = "Rondas inválidas"
                binding.etCircuitRounds.requestFocus()
                return
            }
        } else null

        val superSetGroupId = if (groupingType == GroupingType.SUPERSET) {
            currentSuperSetGroupId ?: generateGroupId().also { currentSuperSetGroupId = it }
        } else null

        // ── Modos especiales ──────────────────────────────────────────────────
        val amrapDuration = if (currentSpecialMode == SpecialMode.AMRAP) {
            binding.etAmrapDuration.text.toString().toIntOrNull()?.takeIf { it > 0 } ?: run {
                binding.etAmrapDuration.error = "Duración inválida"
                binding.etAmrapDuration.requestFocus()
                return
            }
        } else null

        val emomInterval = if (currentSpecialMode == SpecialMode.EMOM) {
            binding.etEmomInterval.text.toString().toIntOrNull()?.takeIf { it > 0 } ?: run {
                binding.etEmomInterval.error = "Intervalo inválido"
                binding.etEmomInterval.requestFocus()
                return
            }
        } else null

        val emomRounds = if (currentSpecialMode == SpecialMode.EMOM) {
            binding.etEmomRounds.text.toString().toIntOrNull()?.takeIf { it > 0 } ?: run {
                binding.etEmomRounds.error = "Rondas inválidas"
                binding.etEmomRounds.requestFocus()
                return
            }
        } else null

        val tabataWork = if (currentSpecialMode == SpecialMode.TABATA) {
            binding.etTabataWork.text.toString().toIntOrNull()?.takeIf { it > 0 } ?: run {
                binding.etTabataWork.error = "Tiempo trabajo inválido"
                binding.etTabataWork.requestFocus()
                return
            }
        } else null

        val tabataRest = if (currentSpecialMode == SpecialMode.TABATA) {
            binding.etTabataRest.text.toString().toIntOrNull()?.takeIf { it > 0 } ?: run {
                binding.etTabataRest.error = "Tiempo descanso inválido"
                binding.etTabataRest.requestFocus()
                return
            }
        } else null

        val tabataRounds = if (currentSpecialMode == SpecialMode.TABATA) {
            binding.etTabataRounds.text.toString().toIntOrNull()?.takeIf { it > 0 } ?: run {
                binding.etTabataRounds.error = "Rondas inválidas"
                binding.etTabataRounds.requestFocus()
                return
            }
        } else null

        val notes = binding.etNotes.text.toString().takeIf { it.isNotBlank() }

        routineExerciseViewModel.addExerciseToRoutine(
            routineId,
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

    // ── Días ──────────────────────────────────────────────────────────────────

    private fun setupDayChipsForRoutine(days: List<String>) {
        val daySet = days.toSet()
        val showAll = daySet.isEmpty()

        chipToDayMap.forEach { (chipId, day) ->
            val chip = binding.chipGroupDays.findViewById<Chip>(chipId)
            chip?.visibility = if (showAll || day in daySet) View.VISIBLE else View.GONE
        }

        val firstAvailableChipId = chipToDayMap.entries
            .firstOrNull { (_, day) -> showAll || day in daySet }
            ?.key

        if (firstAvailableChipId != null) {
            binding.chipGroupDays.check(firstAvailableChipId)
        }
    }

    private fun getSelectedDay(): String? {
        val checkedId = binding.chipGroupDays.checkedChipId
        return chipToDayMap[checkedId]
    }

    private fun updateOrderFieldForDay(day: String) {
        val nextOrder = orderCounterByDay.getOrDefault(day, 1)
        binding.etSessionOrder.setText(nextOrder.toString())
    }

    // ── Agrupación helpers ────────────────────────────────────────────────────

    private enum class GroupingType { NONE, CIRCUIT, SUPERSET }

    private fun setGroupingType(type: GroupingType) {
        binding.layoutCircuitRounds.isVisible = type == GroupingType.CIRCUIT
        binding.tvGroupIdHint.isVisible = type != GroupingType.NONE

        when (type) {
            GroupingType.CIRCUIT -> showGroupIdHint(currentCircuitGroupId)
            GroupingType.SUPERSET -> showGroupIdHint(currentSuperSetGroupId)
            GroupingType.NONE -> {
                // Al deseleccionar agrupación, los IDs guardados se mantienen
                // para que el usuario pueda volver sin perder el grupo activo.
            }
        }
    }

    private fun getGroupingType(): GroupingType = when {
        binding.chipGroupCircuit.isChecked -> GroupingType.CIRCUIT
        binding.chipGroupSuperset.isChecked -> GroupingType.SUPERSET
        else -> GroupingType.NONE
    }

    private fun showGroupIdHint(groupId: String?) {
        binding.tvGroupIdHint.text = if (groupId != null) {
            "Grupo activo: …${groupId.takeLast(6)}"
        } else {
            "Se creará un nuevo grupo al añadir"
        }
        binding.tvGroupIdHint.isVisible = true
    }

    /** Genera un identificador corto legible (no UUID completo para no saturar la DB). */
    private fun generateGroupId(): String = UUID.randomUUID().toString()

    // ── UI helpers ────────────────────────────────────────────────────────────

    private fun showLoading() { binding.progressBar.visibility = View.VISIBLE }
    private fun hideLoading() { binding.progressBar.visibility = View.GONE }

    private fun setFormEnabled(enabled: Boolean) {
        binding.progressBar.visibility = if (enabled) View.GONE else View.VISIBLE
        binding.btnAddExercise.isEnabled = enabled
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}