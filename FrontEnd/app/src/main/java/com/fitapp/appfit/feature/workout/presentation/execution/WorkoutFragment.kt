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
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.core.database.AppDatabase
import com.fitapp.appfit.core.notification.WorkoutNotificationManager
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentWorkoutBinding
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineResponse
import com.fitapp.appfit.feature.routine.ui.RoutineViewModel
import com.fitapp.appfit.feature.workout.data.repository.LocalLastExecutionValuesHelper
import com.fitapp.appfit.feature.workout.data.repository.SaveLastExecutionValuesHelper
import com.fitapp.appfit.feature.workout.domain.manager.LastWorkoutValuesApplier
import com.fitapp.appfit.feature.workout.domain.model.WorkoutCompletionState
import com.fitapp.appfit.feature.workout.domain.model.WorkoutLayoutResolver
import com.fitapp.appfit.feature.workout.data.repository.WorkoutRepositoryImpl
import com.fitapp.appfit.feature.workout.domain.usecase.LoadLocalLastExecutionValuesUseCase
import com.fitapp.appfit.feature.workout.domain.usecase.SaveWorkoutSessionUseCase
import com.fitapp.appfit.feature.workout.presentation.execution.manager.ActiveWorkoutCache
import com.fitapp.appfit.feature.workout.presentation.execution.manager.SetParameterStateManager
import com.fitapp.appfit.feature.workout.util.WorkoutParameterHelper
import com.fitapp.appfit.feature.workout.util.WorkoutPreferences
import com.fitapp.appfit.feature.workout.service.RestTimerService
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.time.LocalDate

class WorkoutFragment : Fragment(), WorkoutFilterBottomSheet.Listener {

    private var _binding: FragmentWorkoutBinding? = null
    private val binding get() = _binding!!
    private val args: WorkoutFragmentArgs by navArgs()

    private lateinit var workoutViewModel: WorkoutExecutionViewModel
    private val routineViewModel: RoutineViewModel by viewModels()

    private lateinit var adapter: WorkoutDayAdapter
    private lateinit var contentController: SetsWorkoutContentController
    private lateinit var stateManager: SetParameterStateManager
    private val completionState = WorkoutCompletionState()
    private val executionConfig = WorkoutExecutionConfig()

    private var workoutStartedAt: Long = System.currentTimeMillis()
    private var currentUserId: String = ""
    private val timerHandler = Handler(Looper.getMainLooper())
    private var timerRunning = false
    private var timerPaused = false
    private var pausedElapsedMs: Long = 0
    private var currentRoutine: RoutineResponse? = null
    private var usesDayGrouping = true

    private var sessionRestoredFromCache = false

    private val timerRunnable = object : Runnable {
        override fun run() {
            updateTimerDisplay()
            timerHandler.postDelayed(this, 1000)
        }
    }

    private var restTimerService: RestTimerService? = null
    private var serviceBound = false
    private var localRestTimer: android.os.CountDownTimer? = null

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

        loadExecutionPreferences()
        initializeDependencies()
        setupRecyclerView()
        setupRibbon()
        setupBackPressHandler()
        applyKeepScreenOn()

        observeRestoredCache()
        observeRoutineState()
        observeWorkoutState()
        observeSaveState()

        workoutViewModel.checkAndRestoreActiveSession(args.routineId)

        resumeTimer()
        binding.fabSaveWorkout.setOnClickListener { saveWorkout() }
    }

    private fun loadExecutionPreferences() {
        val ctx = requireContext()
        executionConfig.autoRestEnabled = WorkoutPreferences.isAutoRestEnabled(ctx)
        executionConfig.defaultRestSeconds = WorkoutPreferences.getDefaultRestSeconds(ctx)
        executionConfig.expandActiveOnly = WorkoutPreferences.isExpandActiveOnly(ctx)
    }

    private fun initializeDependencies() {
        currentUserId = "usuario_temporal"
        WorkoutNotificationManager.createChannel(requireContext())

        val appDatabase = AppDatabase.getInstance(requireContext())
        val lastSetExecutionDao = appDatabase.lastSetExecutionDao()

        val localLastExecutionHelper = LocalLastExecutionValuesHelper(lastSetExecutionDao)
        val saveLastExecutionHelper = SaveLastExecutionValuesHelper(lastSetExecutionDao)

        val repository = WorkoutRepositoryImpl(requireContext())
        val saveUseCase = SaveWorkoutSessionUseCase(repository, saveLastExecutionHelper)
        val loadLocalUseCase = LoadLocalLastExecutionValuesUseCase(localLastExecutionHelper)

        val applier = LastWorkoutValuesApplier()
        val cache = ActiveWorkoutCache(requireContext())

        val factory = WorkoutExecutionViewModelFactory(
            saveUseCase,
            loadLocalUseCase,
            applier,
            saveLastExecutionHelper,
            cache
        )
        workoutViewModel = ViewModelProvider(this, factory)[WorkoutExecutionViewModel::class.java]

        stateManager = SetParameterStateManager()

        executionConfig.onAutoRestRequested = { seconds, label, _ ->
            startRestCountdown(seconds, label)
        }
        executionConfig.onSetCompleted = { updateProgressBadge() }
    }

    override fun onStart() {
        super.onStart()
        requireContext().bindService(
            Intent(requireContext(), RestTimerService::class.java),
            serviceConnection,
            0
        )
        applyKeepScreenOn()
    }

    override fun onStop() {
        super.onStop()
        if (serviceBound) {
            requireContext().unbindService(serviceConnection)
            serviceBound = false
        }
        clearKeepScreenOn()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timerHandler.removeCallbacks(timerRunnable)
        timerRunning = false
        clearKeepScreenOn()
        stopRestCountdown()
        stateManager.clear()
        _binding = null
    }

    // ── Observers ──────────────────────────────────────────────────────────

    private fun observeRestoredCache() {
        workoutViewModel.restoredCacheState.observe(viewLifecycleOwner) { restored ->
            if (restored == null) {
                loadRoutineData()
                return@observe
            }

            Log.i(TAG, "APPLYING_CACHED_SESSION | sets=${restored.completedSetIds.size}")
            sessionRestoredFromCache = true
            workoutStartedAt = restored.startedAt
            workoutViewModel.activeWorkoutCache.saveRoutineId(args.routineId)

            restored.paramState.forEach { (setId, setData) ->
                val exerciseId = setData["exerciseId"] as? Long ?: return@forEach
                @Suppress("UNCHECKED_CAST")
                val params = setData["parameters"] as? Map<Long, Map<String, Any?>>
                    ?: return@forEach
                stateManager.restoreFromExport(setId, exerciseId, params)
            }

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
                        onRoutineLoaded(routine)
                        if (adapter.itemCount == 0) {
                            if (sessionRestoredFromCache) {
                                currentRoutine = routine
                                applyRoutineToAdapter(routine)
                                initializeCompletionStructure(routine)
                                restoreCompletedSetsIntoCompletionState()
                            } else {
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
                        onRoutineLoaded(routineWithValues)
                        applyRoutineToAdapter(routineWithValues)
                        initializeCompletionStructure(routineWithValues)

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
                    Log.i(TAG, "WORKOUT_SAVED | sessionId=${resource.data}")

                    stateManager.clear()
                    completionState.reset()
                    sessionRestoredFromCache = false
                    binding.fabSaveWorkout.isInvisible = true

                    Toast.makeText(requireContext(), "Entrenamiento guardado ✓", Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    binding.fabSaveWorkout.isEnabled = true
                    if (!resource.message.isNullOrBlank()) {
                        Log.e(TAG, "SAVE_FAILED | error=${resource.message}")
                        Toast.makeText(requireContext(), "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun onRoutineLoaded(routine: RoutineResponse) {
        currentRoutine = routine
        usesDayGrouping = routine.exercises.orEmpty().any { it.dayOfWeek != null }

        val sportPrefix = routine.sportName?.takeIf { it.isNotBlank() }?.let { "$it · " }.orEmpty()
        binding.tvRoutineName.text = sportPrefix + (routine.name ?: "Entrenamiento")

        val profile = WorkoutLayoutResolver.resolve(routine)
        Log.i(TAG, "EXECUTION_PROFILE | profile=$profile | sport=${routine.sportName}")
    }

    private fun applyRoutineToAdapter(routine: RoutineResponse) {
        contentController.bindRoutine(routine)
        applyInitialFilter(routine)
        updateProgressBadge()
        updateFilterUi()
    }

    private fun applyInitialFilter(routine: RoutineResponse) {
        val ctx = requireContext()
        val savedMode = WorkoutPreferences.getLastFilterMode(ctx)
        val savedSession = WorkoutPreferences.getLastFilterSession(ctx)
        val savedDay = WorkoutPreferences.getLastFilterDay(ctx)

        val mode = when {
            savedMode != WorkoutPreferences.WorkoutFilterMode.ALL -> savedMode
            WorkoutPreferences.isFilterTodayOnStart(ctx) &&
                usesDayGrouping &&
                routine.trainingDays?.contains(LocalDate.now().dayOfWeek.name) == true ->
                WorkoutPreferences.WorkoutFilterMode.TODAY
            !usesDayGrouping -> WorkoutPreferences.WorkoutFilterMode.SESSION
            else -> WorkoutPreferences.WorkoutFilterMode.ALL
        }

        val session = if (mode == WorkoutPreferences.WorkoutFilterMode.SESSION) {
            adapter.suggestSessionNumber(routine.sessionsPerWeek).coerceAtLeast(1)
        } else savedSession

        val day = if (mode == WorkoutPreferences.WorkoutFilterMode.DAY) {
            savedDay ?: adapter.availableDays().firstOrNull()?.first
        } else null

        adapter.setFilter(mode, session, day)
    }

    // ── Cache restore ──────────────────────────────────────────────────────

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
        updateProgressBadge()
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
        Log.i(TAG, "LOAD_LAST_VALUES_FROM_LOCAL | routineId=${routine.id}")
        workoutViewModel.loadAndApplyLastValuesLocal(routine)
    }

    // ── RecyclerView ───────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = WorkoutDayAdapter(
            onShowNumericInput = { param, current, onConfirm ->
                if (!isAdded) return@WorkoutDayAdapter
                WorkoutNumericValueBottomSheet.show(
                    fragmentManager = parentFragmentManager,
                    param = param,
                    currentValue = current,
                    onConfirm = onConfirm
                )
            },
            stateManager = stateManager,
            onSetValueChanged = { exercise, set, valueType, newValue ->
                stateManager.initializeSet(
                    setId = set.id,
                    routineExerciseId = exercise.id,
                    exerciseId = exercise.exerciseId,
                    setTemplate = set
                )
                when (valueType) {
                    "reps" -> WorkoutParameterHelper.findRepsParameter(set.parameters)?.let { repsParam ->
                        stateManager.updateRepsValue(set.id, repsParam.parameterId, newValue.toInt())
                    }
                    "param" -> WorkoutParameterHelper.findNumericParameter(set.parameters)?.let { param ->
                        if (WorkoutParameterHelper.isIntegerInput(param)) {
                            stateManager.updateIntegerValue(set.id, param.parameterId, newValue.toInt())
                        } else {
                            stateManager.updateNumericValue(set.id, param.parameterId, newValue)
                        }
                    }
                    "duration" -> set.parameters?.firstOrNull { param ->
                        param.parameterType?.uppercase() == "DURATION"
                    }?.let { param ->
                        stateManager.updateDurationValue(set.id, param.parameterId, newValue.toLong())
                    }
                }
                workoutViewModel.persistParamState(stateManager.exportState())
                binding.fabSaveWorkout.isInvisible = false
            },
            onSetCompletedToggled = { exercise, set, isCompleted ->
                if (isCompleted) {
                    stateManager.initializeSet(
                        setId = set.id,
                        routineExerciseId = exercise.id,
                        exerciseId = exercise.exerciseId,
                        setTemplate = set
                    )
                }
                workoutViewModel.persistCompletedSets(completionState.getAllCompletedSets())
                workoutViewModel.persistParamState(stateManager.exportState())
                binding.fabSaveWorkout.isInvisible = !completionState.hasAnyCompletedSets()
                updateProgressBadge()
            },
            completionState = completionState,
            executionConfig = executionConfig
        )

        adapter.onFilterSubtitleChanged = { subtitle ->
            if (_binding != null) {
                binding.tvFilterSubtitle.text = subtitle
                binding.tvFilterSubtitle.isVisible = !subtitle.isNullOrBlank()
            }
        }

        contentController = SetsWorkoutContentController(adapter)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun initializeCompletionStructure(routine: RoutineResponse) {
        routine.exercises?.forEach { exercise ->
            val dayKey = exercise.dayOfWeek ?: "SESSION_${exercise.sessionNumber ?: 0}"
            completionState.registerExercise(exercise.id, dayKey)
            exercise.setsTemplate?.forEach { set ->
                completionState.registerSet(set.id, exercise.id)
            }
        }
        updateProgressBadge()
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

    private fun buildParamStateForSave(): Map<Long, Map<String, Any?>> {
        val fullParamState = mutableMapOf<Long, Map<String, Any?>>()
        val addedSetIds = mutableSetOf<Long>()

        currentRoutine?.exercises?.forEach { exercise ->
            exercise.setsTemplate?.forEach { setTemplate ->
                val setId = setTemplate.id
                if (completionState.isSetCompleted(setId) && addedSetIds.add(setId)) {
                    val params = stateManager.getSetParameters(
                        setId = setId,
                        defaultExerciseId = exercise.exerciseId,
                        defaultParameters = setTemplate.parameters ?: emptyList()
                    )
                    if (params != null) {
                        // Siempre ID del catálogo (exerciseId), nunca routine_exercise.id
                        fullParamState[setId] = params + ("exerciseId" to exercise.exerciseId)
                    }
                    else Log.w(TAG, "NO_PARAMS_FOR_SET | setId=$setId")
                }
            }
        }
        return fullParamState
    }

    // ── Ribbon ─────────────────────────────────────────────────────────────

    private fun setupRibbon() {
        binding.chipFilter.setOnClickListener { showFilterSheet() }
        binding.chipAutoMode.setOnCheckedChangeListener { _, checked ->
            executionConfig.autoRestEnabled = checked
            WorkoutPreferences.setAutoRestEnabled(requireContext(), checked)
            if (checked) {
                Snackbar.make(binding.root, "Modo automático activado", Snackbar.LENGTH_SHORT).show()
            }
        }
        binding.chipNext.setOnClickListener { goToNextIncomplete() }
        binding.chipSettings.setOnClickListener {
            findNavController().navigate(WorkoutFragmentDirections.actionWorkoutToPreferences())
        }
        binding.btnSkipRest.setOnClickListener { stopRestCountdown() }
        binding.layoutTimer.setOnClickListener { toggleSessionTimerPause() }

        updateAutoModeUi()
        updateFilterUi()
    }

    override fun onFilterApplied(
        mode: WorkoutPreferences.WorkoutFilterMode,
        sessionNumber: Int,
        dayOfWeek: String?
    ) {
        adapter.setFilter(mode, sessionNumber, dayOfWeek)
        WorkoutPreferences.setLastFilterMode(requireContext(), mode)
        WorkoutPreferences.setLastFilterSession(requireContext(), sessionNumber)
        WorkoutPreferences.setLastFilterDay(requireContext(), dayOfWeek)
        updateFilterUi()
    }

    override fun onExpandAll() = adapter.expandAll()
    override fun onCollapseAll() = adapter.collapseAll()

    private fun showFilterSheet() {
        val routine = currentRoutine ?: return
        WorkoutFilterBottomSheet.show(
            fragment = this,
            usesDayGrouping = usesDayGrouping,
            currentMode = adapter.filterMode,
            currentSession = adapter.filterSessionNumber,
            currentDayOfWeek = adapter.filterDayOfWeek,
            availableDays = adapter.availableDays(),
            availableSessions = adapter.availableSessionNumbers().ifEmpty {
                listOf(routine.sessionsPerWeek?.coerceAtLeast(1) ?: 1)
            }
        )
    }

    private fun updateAutoModeUi() {
        binding.chipAutoMode.isChecked = executionConfig.autoRestEnabled
    }

    private fun updateFilterUi() {
        val label = when (adapter.filterMode) {
            WorkoutPreferences.WorkoutFilterMode.ALL -> "Filtro"
            WorkoutPreferences.WorkoutFilterMode.TODAY -> "Hoy"
            WorkoutPreferences.WorkoutFilterMode.SESSION -> "Ses. ${adapter.filterSessionNumber}"
            WorkoutPreferences.WorkoutFilterMode.DAY -> {
                adapter.availableDays()
                    .firstOrNull { it.first == adapter.filterDayOfWeek }
                    ?.second
                    ?: "Día"
            }
        }
        binding.chipFilter.text = label
        val gold = ContextCompat.getColor(requireContext(), com.fitapp.appfit.R.color.gold_primary)
        val dim = ContextCompat.getColor(requireContext(), com.fitapp.appfit.R.color.text_secondary_dark)
        binding.chipFilter.chipStrokeColor = android.content.res.ColorStateList.valueOf(
            if (adapter.filterMode != WorkoutPreferences.WorkoutFilterMode.ALL) gold else dim
        )
    }

    private fun shouldAutoFocusNext(): Boolean =
        executionConfig.autoRestEnabled || executionConfig.expandActiveOnly

    private fun goToNextIncomplete() {
        val target = adapter.findNextIncomplete(completionState)
        if (target == null) {
            Toast.makeText(requireContext(), "¡Rutina completada!", Toast.LENGTH_SHORT).show()
            return
        }
        adapter.focusTarget(target, collapseOthers = shouldAutoFocusNext())
        binding.recyclerView.post {
            (binding.recyclerView.layoutManager as? LinearLayoutManager)?.let { lm ->
                lm.scrollToPositionWithOffset(target.dayIndex, 0)
            } ?: binding.recyclerView.smoothScrollToPosition(target.dayIndex)
        }
    }

    private fun updateProgressBadge() {
        if (_binding == null) return
        val (completed, total) = adapter.getProgress(completionState)
        val pct = if (total > 0) (completed * 100 / total) else 0
        binding.tvProgressBadge.text = if (total > 0) "$completed/$total sets · $pct%" else "0 sets"
    }

    // ── Rest countdown (RestTimerService) ───────────────────────────────────

    private fun startRestCountdown(seconds: Int, label: String) {
        stopRestCountdown(notifyService = false)

        RestTimerService.startTimer(
            requireContext(),
            seconds,
            label,
            WorkoutPreferences.TimerSoundType.SET_REST
        )

        restTimerService?.onTick = { remaining ->
            if (_binding != null) {
                binding.layoutRestCountdown.isVisible = true
                binding.tvRestCountdown.text = "Descanso ${remaining}s"
            }
        }
        restTimerService?.onFinish = {
            onRestCountdownFinished()
        }

        localRestTimer = object : android.os.CountDownTimer(seconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                if (_binding != null) {
                    val remaining = (millisUntilFinished / 1000).toInt() + 1
                    binding.layoutRestCountdown.isVisible = true
                    binding.tvRestCountdown.text = "Descanso ${remaining}s"
                }
            }
            override fun onFinish() {
                onRestCountdownFinished()
            }
        }.start()

        binding.layoutRestCountdown.isVisible = true
        binding.tvRestCountdown.text = "Descanso ${seconds}s"
    }

    private fun onRestCountdownFinished() {
        if (_binding == null) return
        binding.layoutRestCountdown.isVisible = false
        localRestTimer = null
        restTimerService?.onTick = null
        restTimerService?.onFinish = null
        if (shouldAutoFocusNext()) {
            goToNextIncomplete()
        }
    }

    private fun stopRestCountdown(notifyService: Boolean = true) {
        localRestTimer?.cancel()
        localRestTimer = null
        if (notifyService) {
            RestTimerService.stopTimer(requireContext())
        }
        restTimerService?.onTick = null
        restTimerService?.onFinish = null
        if (_binding != null) {
            binding.layoutRestCountdown.isVisible = false
        }
    }

    // ── Session timer ──────────────────────────────────────────────────────

    private fun resumeTimer() {
        if (timerRunning) return
        timerRunning = true
        timerHandler.postDelayed(timerRunnable, 1000)
        updateTimerDisplay()
    }

    private fun toggleSessionTimerPause() {
        if (timerPaused) {
            workoutStartedAt = System.currentTimeMillis() - pausedElapsedMs
            timerPaused = false
            Snackbar.make(binding.root, "Cronómetro reanudado", Snackbar.LENGTH_SHORT).show()
        } else {
            pausedElapsedMs = System.currentTimeMillis() - workoutStartedAt
            timerPaused = true
            Snackbar.make(binding.root, "Cronómetro pausado", Snackbar.LENGTH_SHORT).show()
        }
        updateTimerDisplay()
    }

    private fun updateTimerDisplay() {
        if (_binding == null) return
        val elapsedMs = if (timerPaused) {
            pausedElapsedMs
        } else {
            System.currentTimeMillis() - workoutStartedAt
        }
        val elapsedSeconds = elapsedMs / 1000
        val h = elapsedSeconds / 3600
        val m = (elapsedSeconds % 3600) / 60
        val s = elapsedSeconds % 60
        binding.tvTimer.text = if (h > 0) "%d:%02d:%02d".format(h, m, s)
        else "%02d:%02d".format(m, s)
        binding.tvTimer.alpha = if (timerPaused) 0.6f else 1f
    }

    private fun applyKeepScreenOn() {
        if (WorkoutPreferences.isKeepScreenOn(requireContext())) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun clearKeepScreenOn() {
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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

    override fun onResume() {
        super.onResume()
        loadExecutionPreferences()
        updateAutoModeUi()
        applyKeepScreenOn()
    }
}
