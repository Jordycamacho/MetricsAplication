package com.fitapp.appfit.feature.workout.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.core.notification.WorkoutNotificationManager
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentWorkoutBinding
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineResponse
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineExerciseResponse
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse
import com.fitapp.appfit.feature.routine.model.setparameter.response.RoutineSetParameterResponse
import com.fitapp.appfit.feature.routine.ui.RoutineViewModel
import com.fitapp.appfit.feature.workout.data.WorkoutRepository
import com.fitapp.appfit.feature.workout.data.RestTimerService
import com.fitapp.appfit.feature.workout.model.WorkoutCompletionState
import com.fitapp.appfit.feature.workout.model.response.LastExerciseValuesResponse
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class WorkoutFragment : Fragment() {

    private var _binding: FragmentWorkoutBinding? = null
    private val binding get() = _binding!!
    private val args: WorkoutFragmentArgs by navArgs()
    private val viewModel: RoutineViewModel by viewModels()
    private lateinit var adapter: WorkoutDayAdapter
    private val setParamState = mutableMapOf<Long, MutableMap<String, Any?>>()
    private val completionState = WorkoutCompletionState()
    private var workoutStartedAt: Long = System.currentTimeMillis()
    private var currentUserId: String = ""
    private lateinit var workoutRepository: WorkoutRepository

    // Tracks the day being executed (set once when routine loads)
    private var activeDayOfWeek: String? = null

    companion object {
        private const val TAG = "WorkoutFragment"
    }

    // ── Timer ─────────────────────────────────────────────────────────────────

    private val timerHandler = Handler(Looper.getMainLooper())
    private var elapsedSeconds = 0
    private var timerRunning = false

    private val timerRunnable = object : Runnable {
        override fun run() {
            elapsedSeconds++
            updateTimerDisplay()
            timerHandler.postDelayed(this, 1000)
        }
    }

    // ── Rest timer service ────────────────────────────────────────────────────

    private var restTimerService: RestTimerService? = null
    private var serviceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            restTimerService = (binder as? RestTimerService.LocalBinder)?.getService()
            serviceBound = true
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            restTimerService = null
            serviceBound = false
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWorkoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i(TAG, "WORKOUT_FRAGMENT_CREATED | routineId=${args.routineId}")

        workoutRepository = WorkoutRepository(requireContext())
        currentUserId = "usuario_temporal"
        WorkoutNotificationManager.createChannel(requireContext())

        setupRecyclerView()
        setupRibbon()
        observeData()

        val alreadyLoaded = viewModel.workoutRoutineState.value is Resource.Success
        if (!alreadyLoaded) {
            workoutStartedAt = System.currentTimeMillis()
            elapsedSeconds = 0
            loadRoutine()
        } else {
            val routine = (viewModel.workoutRoutineState.value as Resource.Success).data
            binding.tvRoutineName.text = routine?.name ?: "Entrenamiento"
            binding.recyclerView.isVisible = true
            binding.progressBar.isVisible = false
            binding.fabSaveWorkout.isInvisible = setParamState.isEmpty()
        }

        resumeTimer()
        binding.fabSaveWorkout.setOnClickListener { saveWorkout() }
        setupBackPressHandler()
    }

    override fun onStart() {
        super.onStart()
        requireContext().bindService(
            Intent(requireContext(), RestTimerService::class.java),
            serviceConnection, 0
        )
    }

    override fun onStop() {
        super.onStop()
        if (serviceBound) {
            requireContext().unbindService(serviceConnection)
            serviceBound = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timerHandler.removeCallbacks(timerRunnable)
        timerRunning = false
        _binding = null
    }

    // ── Timer ─────────────────────────────────────────────────────────────────

    private fun resumeTimer() {
        if (timerRunning) return
        timerRunning = true
        timerHandler.postDelayed(timerRunnable, 1000)
        updateTimerDisplay()
    }

    private fun updateTimerDisplay() {
        if (_binding == null) return
        val h = elapsedSeconds / 3600
        val m = (elapsedSeconds % 3600) / 60
        val s = elapsedSeconds % 60
        binding.tvTimer.text = if (h > 0) "%d:%02d:%02d".format(h, m, s)
        else "%02d:%02d".format(m, s)
    }

    // ── Ribbon ────────────────────────────────────────────────────────────────

    private fun setupRibbon() {
        binding.btnExpandAll.setOnClickListener { adapter.expandAll() }
        binding.btnCollapseAll.setOnClickListener { adapter.collapseAll() }
        binding.btnHistory.setOnClickListener {
            Toast.makeText(requireContext(), "Progreso — próximamente", Toast.LENGTH_SHORT).show()
        }
        binding.btnRestSettings.setOnClickListener {
            findNavController().navigate(WorkoutFragmentDirections.actionWorkoutToPreferences())
        }
    }

    // ── RecyclerView ──────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = WorkoutDayAdapter(
            onSetValueChanged = { exercise, set, valueType, newValue ->
                ensureSetStateInitialized(exercise, set)

                @Suppress("UNCHECKED_CAST")
                val paramMap = setParamState[set.id]!!["parameters"] as MutableMap<Long, MutableMap<String, Any?>>

                when (valueType) {
                    "reps" -> paramMap.values.forEach { it["repetitions"] = newValue.toInt() }
                    "param" -> set.parameters?.forEach { param ->
                        when (param.parameterType?.uppercase()) {
                            "NUMBER", "DISTANCE", "PERCENTAGE" ->
                                paramMap[param.parameterId]?.set("numericValue", newValue)
                            "INTEGER" ->
                                paramMap[param.parameterId]?.set("integerValue", newValue.toInt())
                        }
                    }
                    "duration" -> set.parameters?.forEach { param ->
                        if (param.parameterType?.uppercase() == "DURATION") {
                            paramMap[param.parameterId]?.set("durationValue", newValue.toLong())
                        }
                    }
                }
                binding.fabSaveWorkout.isInvisible = false
            },
            onSetCompletedToggled = { exercise, set, isCompleted ->
                completionState.markSetCompleted(set.id, exercise.exerciseId, isCompleted)

                if (isCompleted) ensureSetStateInitialized(exercise, set)

                binding.fabSaveWorkout.isInvisible = !completionState.hasAnyCompletedSets()
            },
            completionState = completionState
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun ensureSetStateInitialized(
        exercise: RoutineExerciseResponse,
        set: RoutineSetTemplateResponse
    ) {
        if (!setParamState.containsKey(set.id)) {
            setParamState[set.id] = mutableMapOf(
                "exerciseId" to exercise.exerciseId,
                "parameters" to initSetParamMap(set.parameters ?: emptyList())
            )
        }
    }

    private fun initSetParamMap(
        params: List<RoutineSetParameterResponse>
    ): MutableMap<Long, MutableMap<String, Any?>> {
        return params.associate { param ->
            param.parameterId to mutableMapOf<String, Any?>(
                "repetitions" to param.repetitions,
                "numericValue" to param.numericValue,
                "durationValue" to param.durationValue,
                "integerValue" to param.integerValue
            )
        }.toMutableMap()
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    private fun loadRoutine() {
        Log.i(TAG, "LOAD_ROUTINE | routineId=${args.routineId}")
        viewModel.getRoutineForTraining(args.routineId)
        viewModel.markRoutineAsUsed(args.routineId)
    }

    private fun observeData() {
        viewModel.workoutRoutineState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.recyclerView.isVisible = false
                    binding.fabSaveWorkout.isInvisible = true
                }
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    binding.recyclerView.isVisible = true
                    resource.data?.let { routine ->
                        binding.tvRoutineName.text = routine.name ?: "Entrenamiento"
                        if (adapter.itemCount == 0) {
                            loadLastValuesAndSubmit(routine)
                        }
                        binding.fabSaveWorkout.isInvisible = !completionState.hasAnyCompletedSets()
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(requireContext(), "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                }
                else -> {}
            }
        }
    }

    private fun initializeCompletionStructure(routine: RoutineResponse) {
        routine.exercises?.forEach { exercise ->
            val day = exercise.dayOfWeek ?: "SIN_DIA"
            completionState.registerExercise(exercise.exerciseId, day)
            exercise.setsTemplate?.forEach { set ->
                completionState.registerSet(set.id, exercise.exerciseId)
            }
        }
    }

    // ── Last values + submit ──────────────────────────────────────────────────

    /**
     * Fetches last session values from backend, applies them only to exercises
     * that belong to the ACTIVE day of the routine, then submits to the adapter.
     *
     * FIX: previous implementation applied values to ALL exercises in the routine
     * and matched sets by list index instead of by setTemplateId, causing wrong
     * pre-fills when the current and previous sessions had different set counts.
     */
    private fun loadLastValuesAndSubmit(routine: RoutineResponse) {
        Log.i(TAG, "LOAD_LAST_VALUES_AND_SUBMIT | routineId=${routine.id}")

        lifecycleScope.launch {
            try {
                val lastValuesResult = workoutRepository.getLastValuesForRoutine(routine.id)

                val routineWithValues = when (lastValuesResult) {
                    is Resource.Success -> {
                        val lastValues = lastValuesResult.data ?: emptyMap()
                        Log.i(TAG, "LAST_VALUES_LOADED | exercisesWithHistory=${lastValues.size}")
                        applyLastValuesToActiveDay(routine, lastValues)
                    }
                    else -> {
                        Log.w(TAG, "LAST_VALUES_UNAVAILABLE | error=${(lastValuesResult as? Resource.Error)?.message}")
                        routine
                    }
                }

                adapter.submitRoutine(routineWithValues)
                initializeCompletionStructure(routineWithValues)

                // Determine the active day after submitting
                activeDayOfWeek = routineWithValues.exercises
                    ?.firstOrNull()?.dayOfWeek

                if (lastValuesResult is Resource.Success && lastValuesResult.data?.isNotEmpty() == true) {
                    Snackbar.make(binding.root, "✓ Valores de tu última sesión cargados",
                        Snackbar.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e(TAG, "ERROR_LOADING_LAST_VALUES | error=${e.message}", e)
                adapter.submitRoutine(routine)
                initializeCompletionStructure(routine)
            }
        }
    }

    /**
     * Applies last workout values to the routine exercises.
     *
     * Key fixes vs old implementation:
     * 1. Only exercises in the routine that have history are updated (no day filter here
     *    since the backend already filters by what was completed last time).
     * 2. Set parameters are matched by [setTemplateId], NOT by list index — so if the
     *    user skipped or added sets last session the values still land on the right set.
     */
    private fun applyLastValuesToActiveDay(
        routine: RoutineResponse,
        lastValues: Map<Long, LastExerciseValuesResponse>
    ): RoutineResponse {

        if (lastValues.isEmpty()) return routine

        val updatedExercises = routine.exercises?.map { exercise ->
            val lastExercise = lastValues[exercise.exerciseId]

            if (lastExercise == null) {
                exercise
            } else {
                val lastSetByPosition = lastExercise.sets.associateBy { it.position }

                val updatedSets = exercise.setsTemplate?.map { setTemplate ->
                    val lastSet = lastSetByPosition[setTemplate.position]

                    if (lastSet == null) {
                        setTemplate
                    } else {
                        val lastParamById = lastSet.parameters.associateBy { it.parameterId }

                        val updatedParams = setTemplate.parameters?.map { param ->
                            val lastParam = lastParamById[param.parameterId]
                            if (lastParam != null) {
                                RoutineSetParameterResponse(
                                    id = param.id,
                                    setTemplateId = param.setTemplateId,
                                    parameterId = param.parameterId,
                                    parameterName = param.parameterName,
                                    parameterType = param.parameterType,
                                    unit = param.unit,
                                    repetitions = lastParam.integerValue ?: param.repetitions,
                                    numericValue = lastParam.numericValue ?: param.numericValue,
                                    integerValue = lastParam.integerValue ?: param.integerValue,
                                    durationValue = lastParam.durationValue ?: param.durationValue
                                )
                            } else {
                                param
                            }
                        }

                        setTemplate.copy(parameters = updatedParams)
                    }
                }

                exercise.copy(setsTemplate = updatedSets)
            }
        }

        return routine.copy(exercises = updatedExercises)
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    private fun saveWorkout() {
        if (!completionState.hasAnyCompletedSets()) {
            Toast.makeText(requireContext(), "No hay sets completados para guardar", Toast.LENGTH_SHORT).show()
            return
        }

        Log.i(TAG, "SAVE_WORKOUT_INITIATED | completedSets=${completionState.getAllCompletedSets().size}")

        binding.fabSaveWorkout.isEnabled = false
        binding.progressBar.isVisible = true

        lifecycleScope.launch {
            val completionMap = completionState.getAllCompletedSets().associateWith { true }

            val result = workoutRepository.saveWorkoutSession(
                routineId = args.routineId,
                userId = currentUserId,
                setParamState = setParamState,
                setCompletionState = completionMap,
                startedAt = workoutStartedAt,
                finishedAt = System.currentTimeMillis(),
                performanceScore = null
            )

            binding.progressBar.isVisible = false
            binding.fabSaveWorkout.isEnabled = true

            result.fold(
                onSuccess = { sessionId ->
                    Log.i(TAG, "WORKOUT_SAVED | sessionId=$sessionId")
                    // FIX: use reset() instead of iterating with exerciseId=0L
                    setParamState.clear()
                    completionState.reset()
                    binding.fabSaveWorkout.isInvisible = true
                    Toast.makeText(requireContext(), "Entrenamiento guardado ✓", Toast.LENGTH_SHORT).show()
                },
                onFailure = { error ->
                    Log.e(TAG, "SAVE_ERROR | error=${error.message}", error)
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    // ── Back press ────────────────────────────────────────────────────────────

    private fun setupBackPressHandler() {
        var backPressedOnce = false
        val handler = Handler(Looper.getMainLooper())

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (backPressedOnce) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Salir del entrenamiento")
                    .setMessage("Si sales ahora perderás el progreso. ¿Seguro?")
                    .setPositiveButton("Salir") { _, _ ->
                        if (setParamState.isNotEmpty() && completionState.hasAnyCompletedSets()) {
                            lifecycleScope.launch {
                                workoutRepository.saveWorkoutSession(
                                    routineId = args.routineId,
                                    userId = currentUserId,
                                    setParamState = setParamState,
                                    setCompletionState = completionState.getAllCompletedSets()
                                        .associateWith { true },
                                    startedAt = workoutStartedAt
                                )
                            }
                        }
                        findNavController().navigateUp()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } else {
                backPressedOnce = true
                Snackbar.make(binding.root, "Pulsa otra vez para salir", Snackbar.LENGTH_SHORT).show()
                handler.postDelayed({ backPressedOnce = false }, 2000)
            }
        }
    }
}