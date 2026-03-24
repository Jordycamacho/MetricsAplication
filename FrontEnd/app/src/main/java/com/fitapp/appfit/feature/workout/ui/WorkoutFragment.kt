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
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentWorkoutBinding
import com.fitapp.appfit.feature.routine.model.setparameter.response.RoutineSetParameterResponse
import com.fitapp.appfit.feature.routine.ui.RoutineViewModel
import com.fitapp.appfit.feature.workout.data.WorkoutRepository
import com.fitapp.appfit.feature.workout.data.RestTimerService
import com.fitapp.appfit.feature.workout.ui.WorkoutDayAdapter
import com.fitapp.appfit.core.notification.WorkoutNotificationManager
import kotlinx.coroutines.launch

class WorkoutFragment : Fragment() {

    private var _binding: FragmentWorkoutBinding? = null
    private val binding get() = _binding!!

    private val args: WorkoutFragmentArgs by navArgs()
    private val viewModel: RoutineViewModel by viewModels()
    private lateinit var adapter: WorkoutDayAdapter
    private var backPressedOnce = false
    private val backHandler = Handler(Looper.getMainLooper())
    private val setParamState =
        mutableMapOf<Long, MutableMap<Long, MutableMap<String, Any?>>>()

    private var workoutStartedAt: Long = System.currentTimeMillis()
    private var currentUserId: String = ""
    private lateinit var workoutRepository: WorkoutRepository

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
                                workoutRepository.saveWorkout(
                                    routineId     = args.routineId,
                                    userId        = currentUserId,
                                    setParamState = setParamState,
                                    startedAt     = workoutStartedAt
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

                handler.postDelayed({
                    backPressedOnce = false
                }, 2000)
            }
        }
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
        adapter = WorkoutDayAdapter { exercise, set, valueType, newValue ->
            if (!setParamState.containsKey(set.id)) {
                setParamState[set.id] = initSetState(set.parameters ?: emptyList())
            }
            val paramMap = setParamState[set.id]!!
            when (valueType) {
                "reps" -> paramMap.values.forEach { it["repetitions"] = newValue.toInt() }
                "param" -> {
                    set.parameters?.forEach { param ->
                        val type = param.parameterType?.uppercase() ?: return@forEach
                        if (type in listOf("NUMBER", "DISTANCE", "PERCENTAGE")) {
                            paramMap[param.parameterId]?.set("numericValue", newValue)
                        } else if (type == "INTEGER") {
                            paramMap[param.parameterId]?.set("integerValue", newValue.toInt())
                        }
                    }
                }

                "duration" -> {
                    set.parameters?.forEach { param ->
                        if (param.parameterType?.uppercase() == "DURATION") {
                            paramMap[param.parameterId]?.set("durationValue", newValue.toLong())
                        }
                    }
                }
            }
            binding.fabSaveWorkout.isInvisible = false
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun initSetState(
        params: List<RoutineSetParameterResponse>
    ): MutableMap<Long, MutableMap<String, Any?>> {
        return params.associate { param ->
            param.parameterId to mutableMapOf<String, Any?>(
                "repetitions"   to param.repetitions,
                "numericValue"  to param.numericValue,
                "durationValue" to param.durationValue,
                "integerValue"  to param.integerValue
            )
        }.toMutableMap()
    }

    // ── Datos ─────────────────────────────────────────────────────────────────

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
                            adapter.submitRoutine(routine)
                        }
                        binding.fabSaveWorkout.isInvisible = setParamState.isEmpty()
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

    private fun loadRoutine() {
        viewModel.getRoutineForTraining(args.routineId)
        viewModel.markRoutineAsUsed(args.routineId)
    }

    // ── Guardar ───────────────────────────────────────────────────────────────

    private fun saveWorkout() {
        if (setParamState.isEmpty()) return
        binding.fabSaveWorkout.isEnabled = false
        binding.progressBar.isVisible = true

        lifecycleScope.launch {
            val result = workoutRepository.saveWorkout(
                routineId     = args.routineId,
                userId        = currentUserId,
                setParamState = setParamState,
                startedAt     = workoutStartedAt
            )
            binding.progressBar.isVisible = false
            binding.fabSaveWorkout.isEnabled = true
            result.fold(
                onSuccess = { sessionId ->
                    setParamState.clear()
                    binding.fabSaveWorkout.isInvisible = true
                    Log.i("WorkoutFragment", "Workout guardado, sessionId=$sessionId")
                    Toast.makeText(requireContext(), "Entrenamiento guardado ✓", Toast.LENGTH_SHORT).show()
                },
                onFailure = { error ->
                    Log.e("WorkoutFragment", "Error guardando workout: ${error.message}", error)
                    Toast.makeText(requireContext(), "Error al guardar. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}