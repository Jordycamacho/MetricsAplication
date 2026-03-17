package com.fitapp.appfit.ui.routines.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fitapp.appfit.R
import com.fitapp.appfit.databinding.FragmentAddExercisesToRoutineBinding
import com.fitapp.appfit.model.ExerciseViewModel
import com.fitapp.appfit.model.RoutineExerciseViewModel
import com.fitapp.appfit.model.RoutineViewModel
import com.fitapp.appfit.response.exercise.request.ExerciseFilterRequest
import com.fitapp.appfit.response.exercise.response.ExerciseResponse
import com.fitapp.appfit.response.routine.request.AddExerciseToRoutineRequest
import com.fitapp.appfit.ui.exercises.adapter.ExerciseAdapter
import com.fitapp.appfit.utils.Resource
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar

class AddExercisesToRoutineFragment : Fragment() {

    private var _binding: FragmentAddExercisesToRoutineBinding? = null
    private val binding get() = _binding!!

    private val exerciseViewModel: ExerciseViewModel by viewModels()
    private val routineExerciseViewModel: RoutineExerciseViewModel by viewModels()
    private val routineViewModel: RoutineViewModel by viewModels()

    private lateinit var exerciseAdapter: ExerciseAdapter
    private var selectedExercise: ExerciseResponse? = null
    private var routineId: Long = 0
    private val orderCounterByDay = mutableMapOf<String, Int>()
    private var sortedTrainingDays: List<String> = emptyList()

    private val chipToDayMap = mapOf(
        R.id.chip_monday    to "MONDAY",
        R.id.chip_tuesday   to "TUESDAY",
        R.id.chip_wednesday to "WEDNESDAY",
        R.id.chip_thursday  to "THURSDAY",
        R.id.chip_friday    to "FRIDAY",
        R.id.chip_saturday  to "SATURDAY",
        R.id.chip_sunday    to "SUNDAY"
    )

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
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
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

    /**
     * Configura el listener de cambio de día.
     * La visibilidad de chips se aplica después cuando llega la rutina.
     * Por ahora ocultamos todos y esperamos a setupDayChipsForRoutine().
     */
    private fun setupDayChips() {
        binding.chipGroupDays.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val day = chipToDayMap[checkedId] ?: return@setOnCheckedStateChangeListener
            updateOrderFieldForDay(day)
        }
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

        routineExerciseViewModel.addExerciseToRoutine(
            routineId,
            AddExerciseToRoutineRequest(
                exerciseId        = exercise.id,
                sessionNumber     = null,
                dayOfWeek         = selectedDay,
                sessionOrder      = sessionOrder,
                restAfterExercise = restAfter,
                targetParameters  = null,
                sets              = null
            )
        )
    }

    /**
     * Muestra solo los chips de días configurados en la rutina.
     * Selecciona automáticamente el primero disponible.
     */
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

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun getSelectedDay(): String? {
        val checkedId = binding.chipGroupDays.checkedChipId
        return chipToDayMap[checkedId]
    }

    private fun updateOrderFieldForDay(day: String) {
        val nextOrder = orderCounterByDay.getOrDefault(day, 1)
        binding.etSessionOrder.setText(nextOrder.toString())
    }

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