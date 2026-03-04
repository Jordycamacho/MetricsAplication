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
import com.fitapp.appfit.response.sets.request.BulkUpdateSetParametersRequest
import com.fitapp.appfit.response.sets.response.RoutineSetParameterResponse
import com.fitapp.appfit.service.RoutineSetTemplateService
import com.fitapp.appfit.utils.Resource
import kotlinx.coroutines.launch

class WorkoutFragment : Fragment() {

    private var _binding: FragmentWorkoutBinding? = null
    private val binding get() = _binding!!
    private val args: WorkoutFragmentArgs by navArgs()
    private val viewModel: RoutineViewModel by viewModels()
    private lateinit var adapter: WorkoutDayAdapter
    private val setTemplateService = RoutineSetTemplateService.instance
    private val setParamState =
        mutableMapOf<Long, MutableMap<Long, MutableMap<String, Any?>>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

    private fun saveWorkout() {
        if (setParamState.isEmpty()) return

        val setResults = setParamState.map { (setTemplateId, paramMap) ->
            BulkUpdateSetParametersRequest.SetResultRequest(
                setTemplateId = setTemplateId,
                parameters = paramMap.map { (parameterId, values) ->
                    BulkUpdateSetParametersRequest.ParameterResultRequest(
                        parameterId   = parameterId,
                        repetitions   = values["repetitions"] as? Int,
                        numericValue  = values["numericValue"] as? Double,
                        durationValue = values["durationValue"] as? Long,
                        integerValue  = values["integerValue"] as? Int
                    )
                }
            )
        }

        binding.fabSaveWorkout.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = setTemplateService.bulkSaveSetParameters(
                    BulkUpdateSetParametersRequest(setResults)
                )
                if (response.isSuccessful) {
                    setParamState.clear()
                    binding.fabSaveWorkout.isInvisible = true
                    Toast.makeText(requireContext(), "Entrenamiento guardado ✓", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Error al guardar. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("Workout", "bulkSave error: ${e.message}", e)
                Toast.makeText(requireContext(), "Sin conexión. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
            } finally {
                binding.fabSaveWorkout.isEnabled = true
            }
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