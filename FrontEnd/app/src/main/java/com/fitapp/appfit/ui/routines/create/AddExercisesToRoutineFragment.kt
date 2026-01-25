package com.fitapp.appfit.ui.routines.create

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.fitapp.appfit.R
import com.fitapp.appfit.databinding.FragmentAddExercisesToRoutineBinding
import com.fitapp.appfit.model.ExerciseViewModel
import com.fitapp.appfit.model.RoutineExerciseViewModel
import com.fitapp.appfit.model.RoutineViewModel
import com.fitapp.appfit.response.exercise.request.ExerciseFilterRequest
import com.fitapp.appfit.response.exercise.response.ExerciseResponse
import com.fitapp.appfit.response.routine.request.AddExerciseToRoutineRequest
import com.fitapp.appfit.response.routine.response.RoutineResponse
import com.fitapp.appfit.ui.exercises.adapter.ExerciseAdapter
import com.fitapp.appfit.utils.Resource

class AddExercisesToRoutineFragment : Fragment() {

    private var _binding: FragmentAddExercisesToRoutineBinding? = null
    private val binding get() = _binding!!

    private val exerciseViewModel: ExerciseViewModel by viewModels()
    private val routineExerciseViewModel: RoutineExerciseViewModel by viewModels()
    private val routineViewModel: RoutineViewModel by viewModels()

    private lateinit var exerciseAdapter: ExerciseAdapter
    private var selectedExercises = mutableListOf<ExerciseResponse>()
    private var routineId: Long = 0
    private var currentSession = 1
    private var currentDay: String? = null
    private var routineDetails: RoutineResponse? = null
    private var sortedTrainingDays: List<String> = emptyList() // Para almacenar días ordenados

    companion object {
        private const val TAG = "AddExercisesToRoutine"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExercisesToRoutineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        routineId = arguments?.getLong("routineId") ?: 0

        if (routineId == 0L) {
            Toast.makeText(requireContext(), "Error: No se recibió ID de rutina", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        loadRoutineDetails()
        setupListeners()
        loadExercises()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.toolbar.title = "Agregar Ejercicios"
    }

    private fun setupRecyclerView() {
        exerciseAdapter = ExerciseAdapter(
            onItemClick = { exercise ->
                toggleExerciseSelection(exercise)
            },
            onEditClick = {},
            onDeleteClick = { },
            onToggleStatusClick = { }
        )

        binding.recyclerExercises.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = exerciseAdapter
        }
    }

    private fun loadRoutineDetails() {
        routineViewModel.getRoutine(routineId)
    }

    private fun setupListeners() {
        binding.rgOrganization.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_by_session -> {
                    binding.layoutSession.visibility = View.VISIBLE
                    binding.layoutDay.visibility = View.GONE
                }
                R.id.rb_by_day -> {
                    binding.layoutSession.visibility = View.GONE
                    binding.layoutDay.visibility = View.VISIBLE
                }
            }
        }

        binding.spinnerSession.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSession = position + 1
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Spinner de día - CORREGIDO
        binding.spinnerDay.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position < sortedTrainingDays.size) {
                    currentDay = sortedTrainingDays[position]
                    Log.d(TAG, "Día seleccionado: $currentDay")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                currentDay = null
            }
        }

        binding.layoutAdvancedHeader.setOnClickListener {
            val isVisible = binding.layoutAdvancedContent.visibility == View.VISIBLE
            binding.layoutAdvancedContent.visibility = if (isVisible) View.GONE else View.VISIBLE
            val rotation = if (isVisible) 0f else 180f
            binding.ivAdvancedArrow.animate().rotation(rotation).setDuration(200).start()
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == androidx.appcompat.view.menu.ActionMenuItemView.VISIBLE) {
                loadExercises()
                true
            } else {
                false
            }
        }

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text.clear()
            loadExercises()
        }

        binding.chipAll.setOnClickListener {
            loadExercises()
        }
        binding.chipMy.setOnClickListener {
            loadMyExercises()
        }
        binding.chipAvailable.setOnClickListener {
            loadAvailableExercises()
        }

        binding.btnSelectAll.setOnClickListener {
            val allExercises = exerciseAdapter.getExercises()
            selectedExercises.clear()
            selectedExercises.addAll(allExercises)
            updateSelectionCount()
            exerciseAdapter.notifyDataSetChanged()
        }

        binding.btnClearSelection.setOnClickListener {
            selectedExercises.clear()
            updateSelectionCount()
            exerciseAdapter.notifyDataSetChanged()
        }

        binding.btnAddExercises.setOnClickListener {
            addSelectedExercises()
        }
    }

    private fun setupObservers() {
        exerciseViewModel.allExercisesState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    resource.data?.let { pageResponse ->
                        pageResponse.content?.let { exercises ->
                            exerciseAdapter.setExercises(exercises)
                        }
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    showLoading()
                }
                else -> {}
            }
        })

        routineViewModel.routineDetailState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    resource.data?.let { routine ->
                        routineDetails = routine
                        setupRoutineConfiguration(routine)
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Error cargando rutina: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    showLoading()
                }
                else -> {}
            }
        })

        routineExerciseViewModel.addExerciseState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Ejercicios agregados exitosamente", Toast.LENGTH_SHORT).show()
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        findNavController().navigateUp()
                    }, 500)
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    showLoading()
                }
                else -> {}
            }
        })
    }

    private fun setupRoutineConfiguration(routine: RoutineResponse) {
        Log.d(TAG, "Configurando rutina: ${routine.name}")

        val sessionsPerWeek = routine.sessionsPerWeek ?: 3
        val sessions = (1..sessionsPerWeek).map { "Sesión $it" }
        val sessionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sessions)
        binding.spinnerSession.adapter = sessionAdapter
        binding.spinnerSession.setSelection(0)

        routine.trainingDays?.let { days ->
            if (days.isNotEmpty()) {
                // Ordenar días de la semana
                sortedTrainingDays = days.sortedBy { day ->
                    when (day) {
                        "MONDAY" -> 1
                        "TUESDAY" -> 2
                        "WEDNESDAY" -> 3
                        "THURSDAY" -> 4
                        "FRIDAY" -> 5
                        "SATURDAY" -> 6
                        "SUNDAY" -> 7
                        else -> 8
                    }
                }

                val dayMap = mapOf(
                    "MONDAY" to "Lunes",
                    "TUESDAY" to "Martes",
                    "WEDNESDAY" to "Miércoles",
                    "THURSDAY" to "Jueves",
                    "FRIDAY" to "Viernes",
                    "SATURDAY" to "Sábado",
                    "SUNDAY" to "Domingo"
                )

                val spanishDays = sortedTrainingDays.map { day -> dayMap[day] ?: day }
                val dayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spanishDays)
                binding.spinnerDay.adapter = dayAdapter

                binding.rbByDay.isEnabled = true

                if (days.size > 0) {
                    binding.rbByDay.isChecked = true
                    binding.layoutSession.visibility = View.GONE
                    binding.layoutDay.visibility = View.VISIBLE
                }
            } else {
                binding.rbByDay.isEnabled = false
                binding.rbBySession.isChecked = true
                binding.layoutSession.visibility = View.VISIBLE
                binding.layoutDay.visibility = View.GONE
            }
        } ?: run {
            binding.rbByDay.isEnabled = false
            binding.rbBySession.isChecked = true
            binding.layoutSession.visibility = View.VISIBLE
            binding.layoutDay.visibility = View.GONE
        }

        binding.etSessionOrder.setText("1")
        binding.etRestAfter.setText("60")

        Log.d(TAG, "Rutina configurada: $sessionsPerWeek sesiones, días: ${routine.trainingDays?.size ?: 0}")
    }

    private fun loadExercises() {
        val searchText = binding.etSearch.text.toString()
        val filterRequest = ExerciseFilterRequest(
            search = if (searchText.isNotEmpty()) searchText else null,
            page = 0,
            size = 50,
            sortBy = "name",
            direction = ExerciseFilterRequest.SortDirection.ASC
        )
        exerciseViewModel.searchExercises(filterRequest)
        binding.btnClearSearch.visibility = if (searchText.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun loadMyExercises() {
        val searchText = binding.etSearch.text.toString()
        val filterRequest = ExerciseFilterRequest(
            search = if (searchText.isNotEmpty()) searchText else null,
            page = 0,
            size = 50,
            sortBy = "name",
            direction = ExerciseFilterRequest.SortDirection.ASC
        )
        exerciseViewModel.searchMyExercises(filterRequest)
    }

    private fun loadAvailableExercises() {
        val searchText = binding.etSearch.text.toString()
        val filterRequest = ExerciseFilterRequest(
            search = if (searchText.isNotEmpty()) searchText else null,
            page = 0,
            size = 50,
            sortBy = "name",
            direction = ExerciseFilterRequest.SortDirection.ASC
        )
        exerciseViewModel.searchAvailableExercises(filterRequest)
    }

    private fun toggleExerciseSelection(exercise: ExerciseResponse) {
        if (selectedExercises.contains(exercise)) {
            selectedExercises.remove(exercise)
        } else {
            selectedExercises.add(exercise)
        }
        updateSelectionCount()
    }

    private fun updateSelectionCount() {
        binding.tvSelectedCount.text = selectedExercises.size.toString()
        binding.btnAddExercises.text = "Agregar (${selectedExercises.size})"
        binding.btnAddExercises.isEnabled = selectedExercises.isNotEmpty()
    }

    private fun addSelectedExercises() {
        if (selectedExercises.isEmpty()) {
            Toast.makeText(requireContext(), "Selecciona al menos un ejercicio", Toast.LENGTH_SHORT).show()
            return
        }

        val sessionOrder = binding.etSessionOrder.text.toString().toIntOrNull() ?: 1
        val restAfterExercise = binding.etRestAfter.text.toString().toIntOrNull() ?: 60

        val sessionNumber = if (binding.rbBySession.isChecked) currentSession else null
        val dayOfWeek = if (binding.rbByDay.isChecked) currentDay else null

        if (sessionNumber == null && dayOfWeek == null) {
            Toast.makeText(requireContext(), "Debes seleccionar sesión o día", Toast.LENGTH_SHORT).show()
            return
        }

        val exercisesWithRequests = selectedExercises.mapIndexed { index, exercise ->
            val request = AddExerciseToRoutineRequest(
                exerciseId = exercise.id,
                sessionNumber = sessionNumber,
                dayOfWeek = dayOfWeek,
                sessionOrder = sessionOrder + index,
                restAfterExercise = restAfterExercise,
                targetParameters = null,
                sets = null
            )
            exercise to request
        }

        showLoading()
        Toast.makeText(requireContext(), "Agregando ${selectedExercises.size} ejercicios...", Toast.LENGTH_SHORT).show()

        routineExerciseViewModel.addMultipleExercisesToRoutine(routineId, exercisesWithRequests)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnAddExercises.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnAddExercises.isEnabled = selectedExercises.isNotEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}