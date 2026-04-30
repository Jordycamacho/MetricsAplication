package com.fitapp.appfit.feature.workout.presentation.execution

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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.core.notification.WorkoutNotificationManager
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentWorkoutBinding
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineResponse
import com.fitapp.appfit.feature.routine.ui.RoutineViewModel
import com.fitapp.appfit.feature.workout.domain.manager.LastWorkoutValuesApplier
import com.fitapp.appfit.feature.workout.domain.usecase.LoadLastExerciseValuesUseCase
import com.fitapp.appfit.feature.workout.domain.usecase.SaveWorkoutSessionUseCase
import com.fitapp.appfit.feature.workout.domain.model.WorkoutCompletionState
import com.fitapp.appfit.feature.workout.data.repository.WorkoutRepositoryImpl
import com.fitapp.appfit.feature.workout.presentation.execution.manager.ActiveWorkoutCache
import com.fitapp.appfit.feature.workout.presentation.execution.manager.SetParameterStateManager
import com.fitapp.appfit.feature.workout.service.RestTimerService
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class WorkoutFragment : Fragment() {

    private var _binding: FragmentWorkoutBinding? = null
    private val binding get() = _binding!!
    private val args: WorkoutFragmentArgs by navArgs()

    private lateinit var workoutViewModel: WorkoutExecutionViewModel
    private val routineViewModel: RoutineViewModel by viewModels()

    private lateinit var adapter: WorkoutDayAdapter
    private lateinit var stateManager: SetParameterStateManager
    private val completionState = WorkoutCompletionState()

    private var workoutStartedAt: Long = System.currentTimeMillis()
    private var currentUserId: String = ""
    private val timerHandler = Handler(Looper.getMainLooper())
    private var timerRunning = false
    private var currentRoutine: RoutineResponse? = null

    // Evita iniciar la carga de últimos valores si el cache ya restauró el estado
    private var sessionRestoredFromCache = false

    private val timerRunnable = object : Runnable {
        override fun run() {
            updateTimerDisplay()
            timerHandler.postDelayed(this, 1000)
        }
    }

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

    companion object {
        private const val TAG = "WorkoutFragment"
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "WORKOUT_FRAGMENT_CREATED | routineId=${args.routineId}")

        initializeDependencies()
        setupRecyclerView()
        setupRibbon()
        setupBackPressHandler()

        observeRestoredCache()
        observeRoutineState()
        observeWorkoutState()
        observeSaveState()

        // Comprueba cache ANTES de cargar desde red
        workoutViewModel.checkAndRestoreActiveSession(args.routineId)

        resumeTimer()
        binding.fabSaveWorkout.setOnClickListener { saveWorkout() }
    }

    private fun initializeDependencies() {
        currentUserId = "usuario_temporal"
        WorkoutNotificationManager.createChannel(requireContext())

        val repository = WorkoutRepositoryImpl(requireContext())
        val saveUseCase = SaveWorkoutSessionUseCase(repository)
        val loadUseCase = LoadLastExerciseValuesUseCase(repository)
        val applier = LastWorkoutValuesApplier()
        val cache = ActiveWorkoutCache(requireContext())

        val factory = WorkoutExecutionViewModelFactory(saveUseCase, loadUseCase, applier, cache)
        workoutViewModel = ViewModelProvider(this, factory)[WorkoutExecutionViewModel::class.java]

        stateManager = SetParameterStateManager()
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
        // No limpiamos el cache aquí: puede que el usuario solo giró la pantalla.
        // El cache se limpia solo al guardar correctamente o al abandonar explícitamente.
        stateManager.clear()
        _binding = null
    }

    // ── Observers ──────────────────────────────────────────────────────────

    /**
     * Si hay sesión activa en cache, restaura estado local y arranca el timer
     * desde el timestamp guardado, sin tocar el servidor.
     */
    private fun observeRestoredCache() {
        workoutViewModel.restoredCacheState.observe(viewLifecycleOwner) { restored ->
            if (restored == null) {
                // No hay cache: carga rutina normalmente
                loadRoutineData()
                return@observe
            }

            Log.i(TAG, "APPLYING_CACHED_SESSION | sets=${restored.completedSetIds.size}")
            sessionRestoredFromCache = true
            workoutStartedAt = restored.startedAt
            workoutViewModel.activeWorkoutCache.saveRoutineId(args.routineId)

            // Restaura parámetros en el stateManager
            restored.paramState.forEach { (setId, setData) ->
                val exerciseId = setData["exerciseId"] as? Long ?: return@forEach
                @Suppress("UNCHECKED_CAST")
                val params = setData["parameters"] as? Map<Long, Map<String, Any?>>
                    ?: return@forEach
                // Usamos initializeSet con datos vacíos y luego sobreescribimos
                // No tenemos el RoutineSetTemplateResponse aquí, pero el stateManager
                // solo necesita el exerciseId y los parámetros ya estaban inicializados.
                // Aplicamos directamente al estado interno vía el mapa exportado.
                stateManager.restoreFromExport(setId, exerciseId, params)
            }

            // Carga la rutina para tener el esqueleto visual; no aplica valores históricos
            loadRoutineData()

            binding.fabSaveWorkout.isInvisible = restored.completedSetIds.isEmpty()
            Snackbar.make(binding.root, "Sesión anterior restaurada", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun observeRoutineState() {
        routineViewModel.workoutRoutineState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.recyclerView.isVisible = false
                }
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    binding.recyclerView.isVisible = true
                    resource.data?.let { routine ->
                        binding.tvRoutineName.text = routine.name ?: "Entrenamiento"
                        if (adapter.itemCount == 0) {
                            if (sessionRestoredFromCache) {
                                // Tenemos cache: mostramos la rutina y restauramos checks
                                currentRoutine = routine
                                adapter.submitRoutine(routine)
                                initializeCompletionStructure(routine)
                                restoreCompletedSetsIntoCompletionState()
                            } else {
                                // Flujo normal: carga últimos valores del backend
                                loadLastValuesAndSubmit(routine)
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(requireContext(), "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun observeWorkoutState() {
        workoutViewModel.routineWithValuesState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { routineWithValues ->
                        Log.i(TAG, "ROUTINE_WITH_VALUES_RECEIVED")
                        currentRoutine = routineWithValues
                        adapter.submitRoutine(routineWithValues)
                        initializeCompletionStructure(routineWithValues)

                        // Guarda el routineId y el startedAt en cache desde el primer momento
                        workoutViewModel.activeWorkoutCache.saveRoutineId(args.routineId)
                        workoutViewModel.activeWorkoutCache.saveStartedAt(workoutStartedAt)

                        Snackbar.make(
                            binding.root,
                            "Valores de tu última sesión cargados",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
                is Resource.Error -> {
                    Log.w(TAG, "LOAD_VALUES_FAILED | error=${resource.message}")
                    Toast.makeText(requireContext(), "No se pudieron cargar los valores previos", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun observeSaveState() {
        workoutViewModel.saveSessionState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.fabSaveWorkout.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    binding.fabSaveWorkout.isEnabled = true
                    val sessionId = resource.data ?: -1L
                    Log.i(TAG, "WORKOUT_SAVED | sessionId=$sessionId")

                    stateManager.clear()
                    completionState.reset()
                    sessionRestoredFromCache = false
                    binding.fabSaveWorkout.isInvisible = true

                    Toast.makeText(requireContext(), "Entrenamiento guardado ✓", Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    binding.fabSaveWorkout.isEnabled = true
                    Log.e(TAG, "SAVE_FAILED | error=${resource.message}")
                    Toast.makeText(requireContext(), "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // ── Cache restore helpers ──────────────────────────────────────────────

    /**
     * Aplica los setIds completados del cache al CompletionState una vez que
     * la estructura de la rutina ya está registrada.
     */
    private fun restoreCompletedSetsIntoCompletionState() {
        val cachedCompletedSets = workoutViewModel.activeWorkoutCache.loadCompletedSets()
        if (cachedCompletedSets.isEmpty()) return

        currentRoutine?.exercises?.forEach { exercise ->
            exercise.setsTemplate?.forEach { set ->
                if (set.id in cachedCompletedSets) {
                    completionState.markSetCompleted(set.id, exercise.id, true)
                }
            }
        }
        Log.i(TAG, "COMPLETION_STATE_RESTORED | completedSets=${cachedCompletedSets.size}")
    }

    // ── Data loading ───────────────────────────────────────────────────────

    private fun loadRoutineData() {
        Log.i(TAG, "LOAD_ROUTINE | routineId=${args.routineId}")
        routineViewModel.getRoutineForTraining(args.routineId)
        routineViewModel.markRoutineAsUsed(args.routineId)
        if (!sessionRestoredFromCache) {
            workoutStartedAt = System.currentTimeMillis()
        }
    }

    private fun loadLastValuesAndSubmit(routine: RoutineResponse) {
        Log.i(TAG, "LOAD_LAST_VALUES_AND_SUBMIT | routineId=${routine.id}")
        workoutViewModel.loadAndApplyLastValues(routine)
    }

    // ── RecyclerView ───────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = WorkoutDayAdapter(
            stateManager = stateManager,
            onSetValueChanged = { exercise, set, valueType, newValue ->
                stateManager.initializeSet(set.id, exercise.id, set)
                when (valueType) {
                    "reps" -> stateManager.updateReps(set.id, newValue.toInt())
                    "param" -> set.parameters?.firstOrNull()?.let { param ->
                        when (param.parameterType?.uppercase()) {
                            "NUMBER", "DISTANCE", "PERCENTAGE" ->
                                stateManager.updateNumericValue(set.id, param.parameterId, newValue)
                            "INTEGER" ->
                                stateManager.updateIntegerValue(set.id, param.parameterId, newValue.toInt())
                        }
                    }
                    "duration" -> set.parameters?.firstOrNull()?.let { param ->
                        if (param.parameterType?.uppercase() == "DURATION") {
                            stateManager.updateDurationValue(set.id, param.parameterId, newValue.toLong())
                        }
                    }
                }
                // Persiste en cache cada vez que cambia un valor
                workoutViewModel.persistParamState(stateManager.exportState())
                binding.fabSaveWorkout.isInvisible = false
            },
            onSetCompletedToggled = { exercise, set, isCompleted ->
                if (isCompleted) {
                    stateManager.initializeSet(set.id, exercise.id, set)
                }
                // Persiste en cache el estado de completado
                workoutViewModel.persistCompletedSets(completionState.getAllCompletedSets())
                workoutViewModel.persistParamState(stateManager.exportState())
                binding.fabSaveWorkout.isInvisible = !completionState.hasAnyCompletedSets()
            },
            completionState = completionState
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun initializeCompletionStructure(routine: RoutineResponse) {
        routine.exercises?.forEach { exercise ->
            completionState.registerExercise(exercise.id, exercise.dayOfWeek ?: "SIN_DIA")
            exercise.setsTemplate?.forEach { set ->
                completionState.registerSet(set.id, exercise.id)
            }
        }
    }

    // ── Save workout ───────────────────────────────────────────────────────

    private fun saveWorkout() {
        if (!completionState.hasAnyCompletedSets()) {
            Toast.makeText(requireContext(), "No hay sets completados para guardar", Toast.LENGTH_SHORT).show()
            return
        }

        Log.i(TAG, "SAVE_WORKOUT_INITIATED")
        val fullParamState = buildParamStateForSave()

        if (fullParamState.isEmpty()) {
            Toast.makeText(requireContext(), "Error: No se pudieron obtener los parámetros de los sets", Toast.LENGTH_LONG).show()
            return
        }

        workoutViewModel.saveWorkoutSession(
            routineId = args.routineId,
            userId = currentUserId,
            setParamState = fullParamState,
            setCompletionState = completionState.getAllCompletedSets().associateWith { true },
            startedAt = workoutStartedAt,
            finishedAt = System.currentTimeMillis(),
            performanceScore = null
        )
    }

    /**
     * Construye el mapa de parámetros solo para los sets completados.
     * Extraído como función para reutilizarlo en saveWorkout y en el back-press.
     */
    private fun buildParamStateForSave(): Map<Long, Map<String, Any?>> {
        val fullParamState = mutableMapOf<Long, Map<String, Any?>>()
        val addedSetIds = mutableSetOf<Long>()

        currentRoutine?.exercises?.forEach { exercise ->
            exercise.setsTemplate?.forEach { setTemplate ->
                val setId = setTemplate.id
                if (completionState.isSetCompleted(setId) && addedSetIds.add(setId)) {
                    val params = stateManager.getSetParameters(
                        setId = setId,
                        defaultExerciseId = exercise.id,
                        defaultParameters = setTemplate.parameters ?: emptyList()
                    )
                    if (params != null) fullParamState[setId] = params
                    else Log.w(TAG, "NO_PARAMS_FOR_SET | setId=$setId")
                }
            }
        }
        return fullParamState
    }

    // ── Ribbon ─────────────────────────────────────────────────────────────

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

    // ── Timer ──────────────────────────────────────────────────────────────

    private fun resumeTimer() {
        if (timerRunning) return
        timerRunning = true
        timerHandler.postDelayed(timerRunnable, 1000)
        updateTimerDisplay()
    }

    private fun updateTimerDisplay() {
        if (_binding == null) return
        val elapsedSeconds = (System.currentTimeMillis() - workoutStartedAt) / 1000
        val h = elapsedSeconds / 3600
        val m = (elapsedSeconds % 3600) / 60
        val s = elapsedSeconds % 60
        binding.tvTimer.text = if (h > 0) "%d:%02d:%02d".format(h, m, s)
        else "%02d:%02d".format(m, s)
    }

    // ── Back press ─────────────────────────────────────────────────────────

    private fun setupBackPressHandler() {
        var backPressedOnce = false
        val handler = Handler(Looper.getMainLooper())

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (backPressedOnce) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Salir del entrenamiento")
                    .setMessage("Si sales ahora, el progreso se guardará si hay sets completados. ¿Seguro?")
                    .setPositiveButton("Salir") { _, _ ->
                        if (completionState.hasAnyCompletedSets()) {
                            // CORREGIDO: usa buildParamStateForSave en lugar de exportState()
                            val paramState = buildParamStateForSave()
                            if (paramState.isNotEmpty()) {
                                lifecycleScope.launch {
                                    workoutViewModel.saveWorkoutSession(
                                        routineId = args.routineId,
                                        userId = currentUserId,
                                        setParamState = paramState,
                                        setCompletionState = completionState.getAllCompletedSets()
                                            .associateWith { true },
                                        startedAt = workoutStartedAt,
                                        finishedAt = System.currentTimeMillis()
                                    )
                                }
                            }
                        }
                        // Limpia el cache al salir explícitamente
                        workoutViewModel.activeWorkoutCache.clear()
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