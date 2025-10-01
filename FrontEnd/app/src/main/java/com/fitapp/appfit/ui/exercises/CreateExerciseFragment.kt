package com.fitapp.appfit.ui.exercises

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fitapp.appfit.model.SportViewModel
import com.fitapp.appfit.response.exercise.ExerciseRequest
import com.fitapp.appfit.response.exercise.ExerciseResponse
import com.fitapp.appfit.utils.Resource
import com.fitapp.appfit.viewmodel.ExerciseViewModel

class CreateExerciseFragment : Fragment() {
    private var _binding: FragmentExerciseSelectionBinding? = null
    private val binding get() = _binding!!
    private val args: ExerciseSelectionFragmentArgs by navArgs()
    private val exerciseViewModel: ExerciseViewModel by viewModels()
    private val sportViewModel: SportViewModel by viewModels()

    private val selectedExercises = mutableListOf<ExerciseRequest>()
    private var currentExerciseId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupObservers()
        setupClickListeners()

        // Cargar datos iniciales
        exerciseViewModel.getExercisesBySport(args.sportId)
        sportViewModel.getSportById(args.sportId)
    }

    private fun setupUI() {
        binding.tvRoutineName.text = "Rutina: ${args.routineName}"
        binding.tvRoutineId.text = "ID: ${args.routineId}"
    }

    private fun setupObservers() {
        // Observar deporte
        sportViewModel.sportDetailState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { sport ->
                        binding.tvSportName.text = "Deporte: ${sport.name}"
                    }
                }
                is Resource.Error -> {
                    binding.tvSportName.text = "Deporte ID: ${args.sportId}"
                }
                else -> {}
            }
        })

        // Observar ejercicios existentes
        exerciseViewModel.exercisesState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { exercises ->
                        setupExerciseDropdown(exercises)
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Error cargando ejercicios", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        })

        // Observar creación de ejercicio
        exerciseViewModel.createExerciseState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    binding.btnCreateExercise.isEnabled = true
                    Toast.makeText(requireContext(), "Ejercicio creado", Toast.LENGTH_SHORT).show()

                    // Recargar ejercicios y seleccionar el nuevo
                    exerciseViewModel.getExercisesBySport(args.sportId)
                    clearNewExerciseForm()
                }
                is Resource.Error -> {
                    binding.btnCreateExercise.isEnabled = true
                    Toast.makeText(requireContext(), "Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    binding.btnCreateExercise.isEnabled = false
                }
            }
        })
    }

    private fun setupExerciseDropdown(exercises: List<ExerciseResponse>) {
        val exerciseNames = exercises.map { it.name }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            exerciseNames
        )
        binding.actvExerciseSearch.setAdapter(adapter)

        binding.actvExerciseSearch.setOnItemClickListener { parent, view, position, id ->
            val selectedExercise = exercises[position]
            currentExerciseId = selectedExercise.id
            // Pre-cargar parámetros si existen
            selectedExercise.parameterTemplates?.let { templates ->
                showParameterFields(templates)
            }
        }
    }

    private fun showParameterFields(parameterTemplates: Map<String, String>) {
        binding.llCustomParameters.removeAllViews()

        parameterTemplates.forEach { (paramName, paramType) ->
            // Por simplicidad, solo mostramos un mensaje
            Toast.makeText(requireContext(), "Parámetro disponible: $paramName ($paramType)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnAddExercise.setOnClickListener {
            addExerciseToRoutine()
        }

        binding.btnCreateExercise.setOnClickListener {
            createNewExercise()
        }

        binding.actvExerciseSearch.setOnClickListener {
            binding.actvExerciseSearch.showDropDown()
        }
    }

    private fun addExerciseToRoutine() {
        if (currentExerciseId == null) {
            Toast.makeText(requireContext(), "Selecciona un ejercicio existente", Toast.LENGTH_SHORT).show()
            return
        }

        val sets = binding.etSets.text.toString().toIntOrNull() ?: 0
        val targetReps = binding.etTargetReps.text.toString()
        val targetWeight = binding.etTargetWeight.text.toString().toDoubleOrNull()
        val restInterval = binding.etRestInterval.text.toString().toIntOrNull()
        val position = binding.etPosition.text.toString().toIntOrNull() ?: (selectedExercises.size + 1)
        val notes = binding.etNotes.text.toString()

        if (sets <= 0) {
            binding.etSets.error = "Series requeridas"
            return
        }

        if (targetReps.isBlank()) {
            binding.etTargetReps.error = "Repeticiones requeridas"
            return
        }

        val exerciseRequest = ExerciseRequest(
            exerciseId = currentExerciseId!!,
            sets = sets,
            targetReps = targetReps,
            targetWeight = targetWeight,
            restIntervalSeconds = restInterval,
            position = position,
            notes = notes.ifBlank { null }
        )

        selectedExercises.add(exerciseRequest)
        clearExerciseSelectionForm()
        updateSelectedExercisesCount()

        Toast.makeText(requireContext(), "Ejercicio agregado a rutina", Toast.LENGTH_SHORT).show()
    }

    private fun createNewExercise() {
        val name = binding.etNewExerciseName.text.toString()
        val description = binding.etNewExerciseDescription.text.toString()

        if (name.isBlank()) {
            binding.etNewExerciseName.error = "Nombre requerido"
            return
        }

        // Crear el ejercicio - parámetros básicos por defecto
        val parameterTemplates = mapOf(
            "repeticiones" to "number",
            "peso" to "number"
        )

        exerciseViewModel.createExercise(
            name = name,
            description = description.ifBlank { null },
            sportId = args.sportId,
            parameterTemplates = parameterTemplates
        )
    }

    private fun clearExerciseSelectionForm() {
        binding.actvExerciseSearch.text.clear()
        binding.etSets.text?.clear()
        binding.etTargetReps.text?.clear()
        binding.etTargetWeight.text?.clear()
        binding.etRestInterval.text?.clear()
        binding.etPosition.text?.clear()
        binding.etNotes.text?.clear()
        currentExerciseId = null
        binding.llCustomParameters.removeAllViews()
    }

    private fun clearNewExerciseForm() {
        binding.etNewExerciseName.text?.clear()
        binding.etNewExerciseDescription.text?.clear()
    }

    private fun updateSelectedExercisesCount() {
        binding.tvSelectedCount.text = "Ejercicios seleccionados: ${selectedExercises.size}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}