package com.fitapp.appfit.ui.workout

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.databinding.FragmentWorkoutBinding
import com.fitapp.appfit.model.RoutineViewModel
import com.fitapp.appfit.repository.WorkoutRepository
import com.fitapp.appfit.response.sets.response.RoutineSetParameterResponse
import com.fitapp.appfit.utils.Resource
import kotlinx.coroutines.launch

class WorkoutFragment : Fragment() {

    private var _binding: FragmentWorkoutBinding? = null
    private val binding get() = _binding!!

    private val args: WorkoutFragmentArgs by navArgs()
    private val viewModel: RoutineViewModel by viewModels()
    private lateinit var adapter: WorkoutDayAdapter

    private val setParamState =
        mutableMapOf<Long, MutableMap<Long, MutableMap<String, Any?>>>()

    private var workoutStartedAt: Long = System.currentTimeMillis()
    private var currentUserId: String = ""
    private lateinit var workoutRepository: WorkoutRepository

    // ── Cronómetro ────────────────────────────────────────────────────────────
    private val timerHandler = Handler(Looper.getMainLooper())
    private var elapsedSeconds = 0
    private val timerRunnable = object : Runnable {
        override fun run() {
            elapsedSeconds++
            updateTimerDisplay()
            timerHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        workoutRepository = WorkoutRepository(requireContext())
        workoutStartedAt = System.currentTimeMillis()
        currentUserId = "usuario_temporal"

        setupRecyclerView()
        setupRibbon()
        observeData()
        loadRoutine()
        startTimer()

        binding.fabSaveWorkout.setOnClickListener { saveWorkout() }
    }

    // ── Cronómetro ────────────────────────────────────────────────────────────

    private fun startTimer() {
        elapsedSeconds = 0
        timerHandler.removeCallbacks(timerRunnable)
        timerHandler.postDelayed(timerRunnable, 1000)
    }

    private fun updateTimerDisplay() {
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
        binding.btnExpandAll.setOnClickListener {
            adapter.expandAll()
        }
        binding.btnCollapseAll.setOnClickListener {
            adapter.collapseAll()
        }
        binding.btnNotes.setOnClickListener {
            Toast.makeText(requireContext(), "Notas — próximamente", Toast.LENGTH_SHORT).show()
        }
        binding.btnHistory.setOnClickListener {
            Toast.makeText(requireContext(), "Progreso — próximamente", Toast.LENGTH_SHORT).show()
        }
        binding.btnRestSettings.setOnClickListener {
            Toast.makeText(requireContext(), "Descanso global — próximamente", Toast.LENGTH_SHORT).show()
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
                        setParamState.clear()
                        workoutStartedAt = System.currentTimeMillis()
                        binding.fabSaveWorkout.isInvisible = true
                        binding.tvRoutineName.text = routine.name ?: "Entrenamiento"
                        adapter.submitRoutine(routine)
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

    override fun onDestroyView() {
        super.onDestroyView()
        timerHandler.removeCallbacks(timerRunnable)
        _binding = null
    }
}