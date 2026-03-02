package com.fitapp.appfit.ui.routines.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
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
import com.fitapp.appfit.response.routine.response.RoutineResponse
import com.fitapp.appfit.ui.exercises.adapter.ExerciseAdapter
import com.fitapp.appfit.utils.Resource
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
    private var currentSession = 1
    private var currentDay: String? = null
    private var sortedTrainingDays: List<String> = emptyList()

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
        setupOrganizationSelectors()
        setupAddButton()
        setupObservers()

        routineViewModel.getRoutine(routineId)
        loadExercises()
    }

    /**
     * Ajusta el margen inferior del FAB para que quede por encima del BottomNavigationView.
     * Usa WindowInsets para obtener la altura real de la barra de navegación del sistema,
     * y suma la altura de la BottomNav de la app (56dp estándar Material).
     */
    private fun adjustFabForBottomNav() {
        val bottomNavHeightDp = 56 // altura estándar Material BottomNavigationView
        val density = resources.displayMetrics.density
        val bottomNavHeightPx = (bottomNavHeightDp * density).toInt()
        val extraMarginPx = (20 * density).toInt()

        ViewCompat.setOnApplyWindowInsetsListener(binding.btnAddExercise) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = v.layoutParams as ViewGroup.MarginLayoutParams
            // margen = altura BottomNav app + barra sistema + margen visual
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

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text?.clear()
        }
    }

    private fun setupFilterChips() {
        binding.chipAll.setOnClickListener { loadExercises() }
        binding.chipMy.setOnClickListener { loadMyExercises() }
        binding.chipAvailable.setOnClickListener { loadAvailableExercises() }
    }

    private fun setupOrganizationSelectors() {
        binding.rgOrganization.setOnCheckedChangeListener { _, checkedId ->
            binding.layoutSession.visibility = if (checkedId == R.id.rb_by_session) View.VISIBLE else View.GONE
            binding.layoutDay.visibility = if (checkedId == R.id.rb_by_day) View.VISIBLE else View.GONE
        }

        binding.spinnerSession.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                currentSession = pos + 1
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        binding.spinnerDay.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                currentDay = if (pos < sortedTrainingDays.size) sortedTrainingDays[pos] else null
            }
            override fun onNothingSelected(p: AdapterView<*>?) { currentDay = null }
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
            when (resource) {
                is Resource.Success -> resource.data?.let { setupRoutineConfiguration(it) }
                is Resource.Error -> showError(resource.message ?: "Error cargando rutina")
                else -> {}
            }
        }

        routineExerciseViewModel.addExerciseState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> setFormEnabled(false)
                is Resource.Success -> {
                    setFormEnabled(true)
                    val name = selectedExercise?.name ?: "Ejercicio"
                    Snackbar.make(binding.root, "✅ $name añadido a la rutina", Snackbar.LENGTH_LONG)
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

    // ── Configuración de la rutina ────────────────────────────────────────────

    private fun setupRoutineConfiguration(routine: RoutineResponse) {
        val sessionsPerWeek = routine.sessionsPerWeek ?: 3
        binding.spinnerSession.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            (1..sessionsPerWeek).map { "Sesión $it" }
        )

        val dayOrder = mapOf(
            "MONDAY" to 1, "TUESDAY" to 2, "WEDNESDAY" to 3, "THURSDAY" to 4,
            "FRIDAY" to 5, "SATURDAY" to 6, "SUNDAY" to 7
        )
        val dayNames = mapOf(
            "MONDAY" to "Lunes", "TUESDAY" to "Martes", "WEDNESDAY" to "Miércoles",
            "THURSDAY" to "Jueves", "FRIDAY" to "Viernes", "SATURDAY" to "Sábado", "SUNDAY" to "Domingo"
        )

        val days = routine.trainingDays?.sortedBy { dayOrder[it] ?: 8 } ?: emptyList()
        sortedTrainingDays = days

        if (days.isNotEmpty()) {
            binding.spinnerDay.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                days.map { dayNames[it] ?: it }
            )
            currentDay = days.firstOrNull()
            binding.rbByDay.isEnabled = true
            binding.rbByDay.isChecked = true
            binding.layoutSession.visibility = View.GONE
            binding.layoutDay.visibility = View.VISIBLE
        } else {
            binding.rbByDay.isEnabled = false
            binding.rbBySession.isChecked = true
        }

        binding.etSessionOrder.setText("1")
        binding.etRestAfter.setText("60")
    }

    // ── Selección y añadir ────────────────────────────────────────────────────

    private fun selectExercise(exercise: ExerciseResponse) {
        selectedExercise = exercise
        binding.btnAddExercise.show() // ExtendedFAB: usa show()/hide() en lugar de visibility
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

        val sessionOrder = binding.etSessionOrder.text.toString().toIntOrNull() ?: 1
        val restAfterExercise = binding.etRestAfter.text.toString().toIntOrNull() ?: 60
        val bySession = binding.rbBySession.isChecked
        val sessionNumber = if (bySession) currentSession else null
        val dayOfWeek = if (!bySession) currentDay else null

        if (sessionNumber == null && dayOfWeek == null) {
            showError("Selecciona sesión o día de entrenamiento")
            return
        }

        routineExerciseViewModel.addExerciseToRoutine(
            routineId,
            AddExerciseToRoutineRequest(
                exerciseId = exercise.id,
                sessionNumber = sessionNumber,
                dayOfWeek = dayOfWeek,
                sessionOrder = sessionOrder,
                restAfterExercise = restAfterExercise,
                targetParameters = null,
                sets = null
            )
        )
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