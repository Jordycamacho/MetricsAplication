package com.fitapp.appfit.ui.routines.create

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
import com.fitapp.appfit.databinding.FragmentEditRoutineBinding
import com.fitapp.appfit.model.RoutineViewModel
import com.fitapp.appfit.model.SportViewModel
import com.fitapp.appfit.response.routine.request.UpdateRoutineRequest
import com.fitapp.appfit.response.routine.response.RoutineResponse
import com.fitapp.appfit.response.sport.response.SportResponse
import com.fitapp.appfit.utils.Resource

class EditRoutineFragment : Fragment() {
    private var _binding: FragmentEditRoutineBinding? = null
    private val binding get() = _binding!!
    private val routineViewModel: RoutineViewModel by viewModels()
    private val sportViewModel: SportViewModel by viewModels()
    private val args: EditRoutineFragmentArgs by navArgs()

    private var sportsMap = mutableMapOf<String, Long>()
    private var currentRoutineId: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditRoutineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener el ID de la rutina de los argumentos
        currentRoutineId = args.routineId

        setupSportsSpinner()
        setupClickListeners()
        setupObservers()
        loadRoutineData()

        sportViewModel.getPredefinedSports()
    }

    private fun loadRoutineData() {
        // Cargar los datos de la rutina desde el ViewModel
        routineViewModel.getRoutine(currentRoutineId)
    }

    private fun setupSportsSpinner() {
        val placeholder = arrayOf("Seleccionar deporte")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            placeholder
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSports.adapter = adapter
    }

    private fun setupObservers() {
        // Observar los deportes
        sportViewModel.sportsState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { sports ->
                        updateSportsSpinner(sports)
                    }
                }
                is Resource.Error -> {
                    showToast("Error cargando deportes: ${resource.message}")
                }
                else -> {}
            }
        })

        // Observar los detalles de la rutina
        routineViewModel.routineDetailState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { routine ->
                        populateFormWithRoutineData(routine)
                    }
                }
                is Resource.Error -> {
                    showToast("Error cargando rutina: ${resource.message}")
                }
                is Resource.Loading -> {
                    // Mostrar loading si quieres
                }
            }
        })

        // Observar el estado de actualización
        routineViewModel.updateRoutineState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    binding.btnSaveRoutine.isEnabled = true
                    showToast("✅ Rutina actualizada exitosamente")
                    // Navegar hacia atrás después de un segundo
                    binding.root.postDelayed({
                        findNavController().navigateUp()
                    }, 1000)
                }
                is Resource.Error -> {
                    binding.btnSaveRoutine.isEnabled = true
                    showError(resource.message ?: "Error desconocido")
                }
                is Resource.Loading -> {
                    binding.btnSaveRoutine.isEnabled = false
                }
            }
        })

        // Observar el estado de eliminación
        routineViewModel.deleteRoutineState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    showToast("✅ Rutina eliminada exitosamente")
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    showError("Error eliminando rutina: ${resource.message}")
                }
                else -> {}
            }
        })
    }

    private fun updateSportsSpinner(sports: List<SportResponse>) {
        sportsMap.clear()
        val sportNames = mutableListOf("Seleccionar deporte")

        sports.forEach { sport ->
            sportNames.add(sport.name)
            sportsMap[sport.name] = sport.id
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            sportNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSports.adapter = adapter
    }

    private fun populateFormWithRoutineData(routine: RoutineResponse) {
        // Nombre
        binding.etRoutineName.setText(routine.name)

        // Deporte (si tiene)
        if (routine.sportName != null) {
            val sportIndex = (binding.spinnerSports.adapter as ArrayAdapter<String>).getPosition(routine.sportName)
            if (sportIndex >= 0) {
                binding.spinnerSports.setSelection(sportIndex)
            }
        }

        // Descripción
        binding.etRoutineDescription.setText(routine.description ?: "")

        // Objetivo
        binding.etGoal.setText(routine.goal)

        // Sesiones por semana
        binding.etSessionsPerWeek.setText(routine.sessionsPerWeek.toString())
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSaveRoutine.setOnClickListener {
            if (validateAllFields()) {
                updateRoutine()
            }
        }

        binding.btnDeleteRoutine.setOnClickListener {
            // Mostrar diálogo de confirmación
            showDeleteConfirmationDialog()
        }

        binding.btnToggleActive.setOnClickListener {
            // Cambiar estado activo/inactivo
            // Necesitarías obtener el estado actual primero
        }
    }

    private fun showDeleteConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Rutina")
            .setMessage("¿Estás seguro de que quieres eliminar esta rutina? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                routineViewModel.deleteRoutine(currentRoutineId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun validateAllFields(): Boolean {
        var isValid = true

        // Validar nombre
        val name = binding.etRoutineName.text.toString()
        if (name.isEmpty()) {
            binding.etRoutineName.error = "El nombre es obligatorio"
            isValid = false
        }

        // Validar objetivo
        val goal = binding.etGoal.text.toString()
        if (goal.isEmpty()) {
            binding.etGoal.error = "El objetivo es obligatorio"
            isValid = false
        }

        // Validar sesiones por semana
        val sessions = binding.etSessionsPerWeek.text.toString()
        if (sessions.isEmpty()) {
            binding.etSessionsPerWeek.error = "Las sesiones por semana son obligatorias"
            isValid = false
        } else {
            val sessionsInt = sessions.toIntOrNull()
            if (sessionsInt == null || sessionsInt < 1 || sessionsInt > 7) {
                binding.etSessionsPerWeek.error = "Debe ser un número entre 1 y 7"
                isValid = false
            }
        }

        return isValid
    }

    private fun updateRoutine() {
        val name = binding.etRoutineName.text.toString()
        val selectedSport = binding.spinnerSports.selectedItem.toString()
        val description = binding.etRoutineDescription.text.toString()
        val trainingDays = getSelectedTrainingDaysAsStrings()
        val goal = binding.etGoal.text.toString()
        val sessionsPerWeek = binding.etSessionsPerWeek.text.toString().toIntOrNull() ?: 3

        // Obtener sportId del mapa
        val sportId = if (selectedSport != "Seleccionar deporte") {
            sportsMap[selectedSport]
        } else {
            null
        }

        // Crear el request de actualización
        val request = UpdateRoutineRequest(
            name = name,
            description = if (description.isNotBlank()) description else null,
            sportId = sportId,
            trainingDays = trainingDays,
            goal = goal,
            sessionsPerWeek = sessionsPerWeek
        )

        routineViewModel.updateRoutine(currentRoutineId, request)
    }

    private fun getSelectedTrainingDaysAsStrings(): List<String> {
        val days = mutableListOf<String>()
        if (binding.cbMonday.isChecked) days.add("MONDAY")
        if (binding.cbTuesday.isChecked) days.add("TUESDAY")
        if (binding.cbWednesday.isChecked) days.add("WEDNESDAY")
        if (binding.cbThursday.isChecked) days.add("THURSDAY")
        if (binding.cbFriday.isChecked) days.add("FRIDAY")
        if (binding.cbSaturday.isChecked) days.add("SATURDAY")
        if (binding.cbSunday.isChecked) days.add("SUNDAY")
        return days
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), "❌ $message", Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}