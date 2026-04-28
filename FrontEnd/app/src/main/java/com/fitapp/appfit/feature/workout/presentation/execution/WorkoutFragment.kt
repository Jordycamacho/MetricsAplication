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
import com.fitapp.appfit.feature.workout.presentation.execution.manager.SetParameterStateManager
import com.fitapp.appfit.feature.workout.service.RestTimerService
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class WorkoutFragment : Fragment() {

    private var _binding: FragmentWorkoutBinding? = null
    private val binding get() = _binding!!
    private val args: WorkoutFragmentArgs by navArgs()

    // ViewModels
    private lateinit var workoutViewModel: WorkoutExecutionViewModel
    private val routineViewModel: RoutineViewModel by viewModels()

    // Managers y Adapters
    private lateinit var adapter: WorkoutDayAdapter
    private lateinit var stateManager: SetParameterStateManager
    private val completionState = WorkoutCompletionState()

    // Timing
    private var workoutStartedAt: Long = System.currentTimeMillis()
    private var currentUserId: String = ""
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

    // Rest Timer Service
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

        // 1. Inicializar dependencias
        initializeDependencies()

        // 2. Setup UI
        setupRecyclerView()
        setupRibbon()
        setupBackPressHandler()

        // 3. Observar estado
        observeWorkoutState()
        observeSaveState()
        observeRoutineState()

        // 4. Cargar datos
        loadRoutineData()

        // 5. Timers
        resumeTimer()
        binding.fabSaveWorkout.setOnClickListener { saveWorkout() }
    }

    /**
     * NUEVO: Inicialización de dependencias centralizada
     */
    private fun initializeDependencies() {
        currentUserId = "usuario_temporal"
        WorkoutNotificationManager.createChannel(requireContext())

        // Crear managers y casos de uso
        val repository = WorkoutRepositoryImpl(requireContext())
        val saveUseCase = SaveWorkoutSessionUseCase(repository)
        val loadUseCase = LoadLastExerciseValuesUseCase(repository)
        val applier = LastWorkoutValuesApplier()

        // Crear ViewModel
        val factory = WorkoutExecutionViewModelFactory(saveUseCase, loadUseCase, applier)
        workoutViewModel = ViewModelProvider(this, factory)[WorkoutExecutionViewModel::class.java]

        // Crear state manager
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
        stateManager.clear()
        _binding = null
    }

    // ── Data Loading ───────────────────────────────────────────────────────

    private fun loadRoutineData() {
        Log.i(TAG, "LOAD_ROUTINE | routineId=${args.routineId}")
        routineViewModel.getRoutineForTraining(args.routineId)
        routineViewModel.markRoutineAsUsed(args.routineId)
        workoutStartedAt = System.currentTimeMillis()
        elapsedSeconds = 0
    }

    // ── Observers ──────────────────────────────────────────────────────────

    /**
     * Observa el estado de la rutina (carga de datos iniciales)
     */
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
                            loadLastValuesAndSubmit(routine)
                        }
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(
                        requireContext(),
                        "Error: ${resource.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    findNavController().navigateUp()
                }
            }
        }
    }

    /**
     * Observa el estado de últimos valores (carga y aplicación)
     */
    private fun observeWorkoutState() {
        workoutViewModel.routineWithValuesState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { routineWithValues ->
                        Log.i(TAG, "✅ ROUTINE_WITH_VALUES_RECEIVED")
                        adapter.submitRoutine(routineWithValues)
                        initializeCompletionStructure(routineWithValues)
                        Snackbar.make(
                            binding.root,
                            "✓ Valores de tu última sesión cargados",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
                is Resource.Error -> {
                    Log.w(TAG, "❌ LOAD_VALUES_FAILED | error=${resource.message}")
                    Toast.makeText(requireContext(), "No se pudieron cargar los valores previos", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    /**
     * Observa el estado del guardado
     */
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
                    Log.i(TAG, "✅ WORKOUT_SAVED | sessionId=$sessionId")

                    // Limpiar estado
                    stateManager.clear()
                    completionState.reset()
                    binding.fabSaveWorkout.isInvisible = true

                    Toast.makeText(
                        requireContext(),
                        "Entrenamiento guardado ✓",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Opcional: navegar o hacer algo más
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    binding.fabSaveWorkout.isEnabled = true

                    Log.e(TAG, "❌ SAVE_FAILED | error=${resource.message}")
                    Toast.makeText(
                        requireContext(),
                        "Error: ${resource.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // ── Load Last Values ───────────────────────────────────────────────────

    /**
     * SIMPLIFICADO: Una línea en lugar de toda la lógica anterior
     */
    private fun loadLastValuesAndSubmit(routine: RoutineResponse) {
        Log.i(TAG, "LOAD_LAST_VALUES_AND_SUBMIT | routineId=${routine.id}")
        workoutViewModel.loadAndApplyLastValues(routine)
    }

    // ── RecyclerView ───────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = WorkoutDayAdapter(
            onSetValueChanged = { exercise, set, valueType, newValue ->
                stateManager.initializeSet(set.id, exercise.exerciseId, set)
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

                binding.fabSaveWorkout.isInvisible = false
            },
            onSetCompletedToggled = { exercise, set, isCompleted ->
                completionState.markSetCompleted(set.id, exercise.exerciseId, isCompleted)
                if (isCompleted) stateManager.initializeSet(set.id, exercise.exerciseId, set)
                binding.fabSaveWorkout.isInvisible = !completionState.hasAnyCompletedSets()
            },
            completionState = completionState
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun initializeCompletionStructure(routine: RoutineResponse) {
        routine.exercises?.forEach { exercise ->
            completionState.registerExercise(exercise.exerciseId, exercise.dayOfWeek ?: "SIN_DIA")
            exercise.setsTemplate?.forEach { set ->
                completionState.registerSet(set.id, exercise.exerciseId)
            }
        }
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

    // ── Save Workout ───────────────────────────────────────────────────────

    private fun saveWorkout() {
        if (!completionState.hasAnyCompletedSets()) {
            Toast.makeText(
                requireContext(),
                "No hay sets completados para guardar",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        Log.i(TAG, "SAVE_WORKOUT_INITIATED")

        val completionMap = completionState.getAllCompletedSets().associateWith { true }

        workoutViewModel.saveWorkoutSession(
            routineId = args.routineId,
            userId = currentUserId,
            setParamState = stateManager.exportState(),  // ← Limpio
            setCompletionState = completionMap,
            startedAt = workoutStartedAt,
            finishedAt = System.currentTimeMillis(),
            performanceScore = null
        )
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
        val h = elapsedSeconds / 3600
        val m = (elapsedSeconds % 3600) / 60
        val s = elapsedSeconds % 60
        binding.tvTimer.text = if (h > 0) "%d:%02d:%02d".format(h, m, s)
        else "%02d:%02d".format(m, s)
    }

    // ── Back Press ─────────────────────────────────────────────────────────

    private fun setupBackPressHandler() {
        var backPressedOnce = false
        val handler = Handler(Looper.getMainLooper())

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (backPressedOnce) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Salir del entrenamiento")
                    .setMessage("Si sales ahora perderás el progreso. ¿Seguro?")
                    .setPositiveButton("Salir") { _, _ ->
                        if (completionState.hasAnyCompletedSets()) {
                            lifecycleScope.launch {
                                // Usar ViewModel para guardar
                                workoutViewModel.saveWorkoutSession(
                                    routineId = args.routineId,
                                    userId = currentUserId,
                                    setParamState = stateManager.exportState(),
                                    setCompletionState = completionState.getAllCompletedSets()
                                        .associateWith { true },
                                    startedAt = workoutStartedAt,
                                    finishedAt = System.currentTimeMillis()
                                )
                            }
                        }
                        findNavController().navigateUp()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } else {
                backPressedOnce = true
                Snackbar.make(binding.root, "Pulsa otra vez para salir", Snackbar.LENGTH_SHORT)
                    .show()
                handler.postDelayed({ backPressedOnce = false }, 2000)
            }
        }
    }
}