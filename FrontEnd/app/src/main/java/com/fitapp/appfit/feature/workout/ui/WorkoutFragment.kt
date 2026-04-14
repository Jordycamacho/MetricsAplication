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
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.core.database.AppDatabase
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentWorkoutBinding
import com.fitapp.appfit.feature.routine.model.setparameter.response.RoutineSetParameterResponse
import com.fitapp.appfit.feature.routine.ui.RoutineViewModel
import com.fitapp.appfit.feature.workout.data.WorkoutRepository
import com.fitapp.appfit.feature.workout.data.RestTimerService
import com.fitapp.appfit.core.notification.WorkoutNotificationManager
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineResponse
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineExerciseResponse
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse
import com.fitapp.appfit.feature.workout.model.WorkoutCompletionState
import com.fitapp.appfit.feature.workout.model.response.LastExerciseValuesResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkoutFragment : Fragment() {

    private var _binding: FragmentWorkoutBinding? = null
    private val binding get() = _binding!!
    private val args: WorkoutFragmentArgs by navArgs()
    private val viewModel: RoutineViewModel by viewModels()
    private lateinit var adapter: WorkoutDayAdapter
    private var backPressedOnce = false
    private val backHandler = Handler(Looper.getMainLooper())
    private val setParamState = mutableMapOf<Long, MutableMap<String, Any?>>()
    private val completionState = WorkoutCompletionState()
    private var workoutStartedAt: Long = System.currentTimeMillis()
    private var currentUserId: String = ""
    private lateinit var workoutRepository: WorkoutRepository

    companion object {
        private const val TAG = "WorkoutFragment"
    }

    // ── Cronómetro ────────────────────────────────────────────────────────────
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

    // ── Conexión con RestTimerService ─────────────────────────────────────────
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

    // ── Ciclo de vida ─────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i(TAG, "═══════════════════════════════════════════════════════════")
        Log.i(TAG, "WORKOUT_FRAGMENT_CREATED | routineId=${args.routineId}")
        Log.i(TAG, "═══════════════════════════════════════════════════════════")

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
        val intent = Intent(requireContext(), RestTimerService::class.java)
        requireContext().bindService(intent, serviceConnection, 0)
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

    // ── Cronómetro ────────────────────────────────────────────────────────────

    private fun resumeTimer() {
        if (timerRunning) return
        timerRunning = true
        timerHandler.removeCallbacks(timerRunnable)
        timerHandler.postDelayed(timerRunnable, 1000)
        updateTimerDisplay()
    }

    private fun updateTimerDisplay() {
        if (_binding == null) return
        val h = elapsedSeconds / 3600
        val m = (elapsedSeconds % 3600) / 60
        val s = elapsedSeconds % 60
        binding.tvTimer.text = if (h > 0) {
            "%d:%02d:%02d".format(h, m, s)
        } else {
            "%02d:%02d".format(m, s)
        }
    }

    // ── Ribbon ────────────────────────────────────────────────────────────────

    private fun setupRibbon() {
        binding.btnExpandAll.setOnClickListener { adapter.expandAll() }
        binding.btnCollapseAll.setOnClickListener { adapter.collapseAll() }
        binding.btnHistory.setOnClickListener {
            Toast.makeText(requireContext(), "Progreso — próximamente", Toast.LENGTH_SHORT).show()
        }
        binding.btnRestSettings.setOnClickListener {
            findNavController().navigate(
                WorkoutFragmentDirections.actionWorkoutToPreferences()
            )
        }
    }

    // ── RecyclerView ──────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = WorkoutDayAdapter(
            onSetValueChanged = { exercise, set, valueType, newValue ->
                Log.d(TAG, "SET_VALUE_CHANGED | exerciseId=${exercise.exerciseId} | setId=${set.id} | type=$valueType | value=$newValue")

                if (!setParamState.containsKey(set.id)) {
                    setParamState[set.id] = mutableMapOf(
                        "exerciseId" to exercise.exerciseId,
                        "parameters" to initSetState(set.parameters ?: emptyList())
                    )
                    Log.d(TAG, "  INITIALIZED_SET_STATE | setId=${set.id}")
                }

                @Suppress("UNCHECKED_CAST")
                val paramMap = setParamState[set.id]!!["parameters"] as MutableMap<Long, MutableMap<String, Any?>>

                when (valueType) {
                    "reps" -> {
                        paramMap.values.forEach { it["repetitions"] = newValue.toInt() }
                        Log.d(TAG, "  UPDATED_REPS | setId=${set.id} | reps=${newValue.toInt()}")
                    }
                    "param" -> {
                        set.parameters?.forEach { param ->
                            val type = param.parameterType?.uppercase() ?: return@forEach
                            if (type in listOf("NUMBER", "DISTANCE", "PERCENTAGE")) {
                                paramMap[param.parameterId]?.set("numericValue", newValue)
                                Log.d(TAG, "  UPDATED_NUMERIC | parameterId=${param.parameterId} | value=$newValue")
                            } else if (type == "INTEGER") {
                                paramMap[param.parameterId]?.set("integerValue", newValue.toInt())
                                Log.d(TAG, "  UPDATED_INTEGER | parameterId=${param.parameterId} | value=${newValue.toInt()}")
                            }
                        }
                    }
                    "duration" -> {
                        set.parameters?.forEach { param ->
                            if (param.parameterType?.uppercase() == "DURATION") {
                                paramMap[param.parameterId]?.set("durationValue", newValue.toLong())
                                Log.d(TAG, "  UPDATED_DURATION | parameterId=${param.parameterId} | value=${newValue.toLong()}")
                            }
                        }
                    }
                }
                binding.fabSaveWorkout.isInvisible = false
            },
            onSetCompletedToggled = { exercise, set, isCompleted ->
                Log.d(TAG, "SET_COMPLETED_TOGGLED | exerciseId=${exercise.exerciseId} | setId=${set.id} | completed=$isCompleted")

                completionState.markSetCompleted(set.id, exercise.exerciseId, isCompleted)

                if (isCompleted && !setParamState.containsKey(set.id)) {
                    setParamState[set.id] = mutableMapOf(
                        "exerciseId" to exercise.exerciseId,
                        "parameters" to initSetState(set.parameters ?: emptyList())
                    )
                    Log.d(TAG, "  INITIALIZED_SET_STATE_ON_COMPLETE | setId=${set.id}")
                }

                binding.fabSaveWorkout.isInvisible = !completionState.hasAnyCompletedSets()

                Log.i(TAG, "COMPLETION_STATE_UPDATED | completedSets=${completionState.getAllCompletedSets().size} | hasAny=${completionState.hasAnyCompletedSets()}")
            },
            completionState = completionState
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun initSetState(
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

    // ── Datos ─────────────────────────────────────────────────────────────────

    private fun observeData() {
        viewModel.workoutRoutineState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    Log.d(TAG, "ROUTINE_LOADING")
                    binding.progressBar.isVisible = true
                    binding.recyclerView.isVisible = false
                    binding.fabSaveWorkout.isInvisible = true
                }

                is Resource.Success -> {
                    Log.i(TAG, "ROUTINE_LOADED | name=${resource.data?.name}")
                    binding.progressBar.isVisible = false
                    binding.recyclerView.isVisible = true
                    resource.data?.let { routine ->
                        binding.tvRoutineName.text = routine.name ?: "Entrenamiento"
                        if (adapter.itemCount == 0) {
                            applyLastValuesAndSubmit(routine)
                        }
                        binding.fabSaveWorkout.isInvisible = !completionState.hasAnyCompletedSets()
                    }
                }

                is Resource.Error -> {
                    Log.e(TAG, "ROUTINE_ERROR | error=${resource.message}")
                    binding.progressBar.isVisible = false
                    Toast.makeText(
                        requireContext(),
                        "Error: ${resource.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    findNavController().navigateUp()
                }

                else -> {}
            }
        }
    }

    private fun initializeCompletionStructure(routine: RoutineResponse) {
        Log.i(TAG, "───────────────────────────────────────────────────────────")
        Log.i(TAG, "INITIALIZE_COMPLETION_STRUCTURE")

        routine.exercises?.forEach { exercise ->
            val dayOfWeek = exercise.dayOfWeek ?: "SIN_DIA"
            completionState.registerExercise(exercise.exerciseId, dayOfWeek)
            Log.d(TAG, "  REGISTERED_EXERCISE | exerciseId=${exercise.exerciseId} | day=$dayOfWeek")

            exercise.setsTemplate?.forEach { set ->
                completionState.registerSet(set.id, exercise.exerciseId)
                Log.d(TAG, "    REGISTERED_SET | setId=${set.id} | exerciseId=${exercise.exerciseId}")
            }
        }

        Log.i(TAG, "COMPLETION_STRUCTURE_INITIALIZED")
        Log.i(TAG, "───────────────────────────────────────────────────────────")
    }

    private fun loadRoutine() {
        Log.i(TAG, "LOAD_ROUTINE | routineId=${args.routineId}")
        viewModel.getRoutineForTraining(args.routineId)
        viewModel.markRoutineAsUsed(args.routineId)
    }

    // ── Guardar ───────────────────────────────────────────────────────────────

    private fun saveWorkout() {
        if (!completionState.hasAnyCompletedSets()) {
            Toast.makeText(requireContext(), "No hay sets completados para guardar", Toast.LENGTH_SHORT).show()
            return
        }

        Log.i(TAG, "═══════════════════════════════════════════════════════════")
        Log.i(TAG, "SAVE_WORKOUT_INITIATED")
        Log.i(TAG, "completedSets: ${completionState.getAllCompletedSets().size}")
        Log.i(TAG, "modifiedSets: ${setParamState.size}")
        Log.i(TAG, "═══════════════════════════════════════════════════════════")

        binding.fabSaveWorkout.isEnabled = false
        binding.progressBar.isVisible = true

        lifecycleScope.launch {
            val completionMap = completionState.getAllCompletedSets().associateWith { true }

            Log.d(TAG, "COMPLETION_MAP | entries=${completionMap.size}")

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
                    Log.i(TAG, "✅ WORKOUT_SAVED | sessionId=$sessionId")
                    setParamState.clear()
                    completionState.getAllCompletedSets().forEach { setId ->
                        completionState.markSetCompleted(setId, 0L, false)
                    }
                    binding.fabSaveWorkout.isInvisible = true
                    Toast.makeText(requireContext(), "Entrenamiento guardado ✓", Toast.LENGTH_SHORT).show()
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ SAVE_ERROR | error=${error.message}", error)
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun applyLastValuesAndSubmit(routine: RoutineResponse) {
        Log.i(TAG, "═══════════════════════════════════════════════════════════")
        Log.i(TAG, "APPLY_LAST_VALUES_AND_SUBMIT | routineId=${routine.id}")
        Log.i(TAG, "═══════════════════════════════════════════════════════════")

        lifecycleScope.launch {
            try {
                // Obtener últimos valores del backend
                val lastValuesResult = workoutRepository.getLastValuesForRoutine(routine.id)

                val routineWithLastValues = when (lastValuesResult) {
                    is Resource.Success -> {
                        val lastValues = lastValuesResult.data ?: emptyMap()
                        Log.i(TAG, "✅ LAST_VALUES_LOADED | exercisesWithHistory=${lastValues.size}")

                        applyLastValuesToRoutine(routine, lastValues)
                    }
                    is Resource.Error -> {
                        Log.w(TAG, "⚠️ LAST_VALUES_ERROR | error=${lastValuesResult.message}")
                        routine
                    }
                    else -> routine
                }

                adapter.submitRoutine(routineWithLastValues)

                initializeCompletionStructure(routineWithLastValues)

                if (lastValuesResult is Resource.Success &&
                    lastValuesResult.data?.isNotEmpty() == true) {
                    com.google.android.material.snackbar.Snackbar.make(
                        binding.root,
                        "✓ Valores de tu última sesión cargados",
                        com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR_APPLYING_LAST_VALUES | error=${e.message}", e)
                adapter.submitRoutine(routine)
                initializeCompletionStructure(routine)
            }
        }
    }

    private fun applyLastValuesToRoutine(
        routine: RoutineResponse,
        lastValues: Map<Long, LastExerciseValuesResponse>
    ): RoutineResponse {

        Log.i(TAG, "───────────────────────────────────────────────────────────")
        Log.i(TAG, "APPLY_LAST_VALUES_TO_ROUTINE")
        Log.i(TAG, "lastValuesMap size: ${lastValues.size}")

        val updatedExercises = routine.exercises?.map { exercise ->
            val lastValue = lastValues[exercise.exerciseId]

            if (lastValue != null) {
                Log.d(TAG, "  APPLYING_TO_EXERCISE | exerciseId=${exercise.exerciseId} | lastSets=${lastValue.sets.size}")

                val updatedSets = exercise.setsTemplate?.mapIndexed { index, setTemplate ->
                    val lastSet = lastValue.sets.getOrNull(index)

                    if (lastSet != null) {
                        Log.d(TAG, "    APPLYING_TO_SET[$index] | setId=${setTemplate.id} | lastParams=${lastSet.parameters.size}")

                        val updatedParams = setTemplate.parameters?.map { param ->
                            val lastParam = lastSet.parameters.find { it.parameterId == param.parameterId }

                            if (lastParam != null) {
                                Log.d(TAG, "      PARAM_APPLIED | parameterId=${param.parameterId} | " +
                                        "numeric=${lastParam.numericValue} | integer=${lastParam.integerValue}")

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

                        RoutineSetTemplateResponse(
                            id = setTemplate.id,
                            position = setTemplate.position,
                            subSetNumber = setTemplate.subSetNumber,
                            groupId = setTemplate.groupId,
                            setType = setTemplate.setType,
                            restAfterSet = setTemplate.restAfterSet,
                            parameters = updatedParams
                        )
                    } else {
                        setTemplate
                    }
                }

                RoutineExerciseResponse(
                    id = exercise.id,
                    exerciseId = exercise.exerciseId,
                    routineId = exercise.routineId,
                    exerciseName = exercise.exerciseName,
                    position = exercise.position,
                    sessionNumber = exercise.sessionNumber,
                    dayOfWeek = exercise.dayOfWeek,
                    sessionOrder = exercise.sessionOrder,
                    restAfterExercise = exercise.restAfterExercise,
                    sets = exercise.sets,
                    targetParameters = exercise.targetParameters,
                    setsTemplate = updatedSets
                )
            } else {
                exercise
            }
        }

        Log.i(TAG, "LAST_VALUES_APPLIED")
        Log.i(TAG, "───────────────────────────────────────────────────────────")

        return RoutineResponse(
            id = routine.id,
            name = routine.name,
            description = routine.description,
            sportId = routine.sportId,
            sportName = routine.sportName,
            trainingDays = routine.trainingDays,
            goal = routine.goal,
            sessionsPerWeek = routine.sessionsPerWeek,
            isActive = routine.isActive,
            createdAt = routine.createdAt,
            updatedAt = routine.updatedAt,
            lastUsedAt = routine.lastUsedAt,
            exercises = updatedExercises
        )
    }

    // ── Back Press Handler ────────────────────────────────────────────────────

    private fun setupBackPressHandler() {
        var backPressedOnce = false
        val handler = Handler(Looper.getMainLooper())

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (backPressedOnce) {
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Salir del entrenamiento")
                    .setMessage("Si sales ahora perderás el progreso. ¿Seguro?")
                    .setPositiveButton("Salir") { _, _ ->
                        if (setParamState.isNotEmpty()) {
                            lifecycleScope.launch {
                                val completionMap = completionState.getAllCompletedSets().associateWith { true }
                                workoutRepository.saveWorkoutSession(
                                    routineId = args.routineId,
                                    userId = currentUserId,
                                    setParamState = setParamState,
                                    setCompletionState = completionMap,
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
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root,
                    "Pulsa otra vez para salir",
                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                ).show()
                handler.postDelayed({ backPressedOnce = false }, 2000)
            }
        }
    }
}