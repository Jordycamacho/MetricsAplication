package com.fitapp.appfit.ui.workout

import android.os.Bundle
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

    // Valores que el usuario modifica durante el workout
    // setTemplateId → (parameterId → { reps, numericValue, ... })
    private val setParamState =
        mutableMapOf<Long, MutableMap<Long, MutableMap<String, Any?>>>()

    // Timestamp de cuando el usuario empieza a entrenar
    private var workoutStartedAt: Long = System.currentTimeMillis()

    // ID del usuario autenticado — reemplazar con tu fuente real (prefs, sesión, etc.)
    private var currentUserId: String = ""

    // Repositorio offline-first para guardar y sincronizar el workout
    private lateinit var workoutRepository: WorkoutRepository

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

        // TODO: obtén el userId real de tu capa de sesión/preferencias
        // Ejemplo: currentUserId = sessionManager.getUserId()
        currentUserId = "usuario_temporal"

        setupRecyclerView()
        observeData()
        loadRoutine()

        binding.fabSaveWorkout.setOnClickListener { saveWorkout() }
    }

    private fun setupRecyclerView() {
        adapter = WorkoutDayAdapter { exercise, set, valueType, newValue ->

            if (!setParamState.containsKey(set.id)) {
                setParamState[set.id] = initSetState(set.parameters ?: emptyList())
            }

            val paramMap = setParamState[set.id]!!

            when (valueType) {
                "reps" -> {
                    paramMap.values.forEach { it["repetitions"] = newValue.toInt() }
                }
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

    /**
     * Guarda el workout usando WorkoutRepository:
     *  - Siempre persiste localmente (historial offline)
     *  - Si hay conexión, sincroniza con el back de inmediato
     *  - Si no hay conexión, queda pendiente para WorkoutSyncManager
     */
    private fun saveWorkout() {
        if (setParamState.isEmpty()) return

        binding.fabSaveWorkout.isEnabled = false
        binding.progressBar.isVisible = true

        lifecycleScope.launch {
            val result = workoutRepository.saveWorkout(
                routineId  = args.routineId,
                userId     = currentUserId,
                setParamState = setParamState,
                startedAt  = workoutStartedAt
            )

            binding.progressBar.isVisible = false
            binding.fabSaveWorkout.isEnabled = true

            result.fold(
                onSuccess = { sessionId ->
                    setParamState.clear()
                    binding.fabSaveWorkout.isInvisible = true
                    Log.i("WorkoutFragment", "Workout guardado, sessionId=$sessionId")
                    Toast.makeText(
                        requireContext(),
                        "Entrenamiento guardado ✓",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onFailure = { error ->
                    Log.e("WorkoutFragment", "Error guardando workout: ${error.message}", error)
                    Toast.makeText(
                        requireContext(),
                        "Error al guardar. Intenta de nuevo.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
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
                        setParamState.clear()
                        workoutStartedAt = System.currentTimeMillis()
                        binding.fabSaveWorkout.isInvisible = true
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}