// Archivo: com/fitapp/appfit/ui/routines/create/EditRoutineFragment.kt
package com.fitapp.appfit.ui.routines.create

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fitapp.appfit.R
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
    private var currentRoutine: RoutineResponse? = null

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

        Log.d("EditRoutine", "Cargando rutina ID: $currentRoutineId") // Para debug

        setupSportsSpinner()
        setupClickListeners()
        setupObservers()

        // Cargar datos
        sportViewModel.getPredefinedSports()
        loadRoutineData()
    }

    private fun loadRoutineData() {
        // Mostrar loading
        binding.progressBar.isVisible = true
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

                        // Una vez cargados deportes, si ya tenemos la rutina, seleccionar el correcto
                        currentRoutine?.let { routine ->
                            selectSportInSpinner(routine.sportName)
                        }
                    }
                }
                is Resource.Error -> {
                    showToast("Error cargando deportes: ${resource.message}")
                }
                else -> {}
            }
        })

        // Observar los detalles de la rutina - ¡ESTO ES LO MÁS IMPORTANTE!
        routineViewModel.routineDetailState.observe(viewLifecycleOwner, Observer { resource ->
            binding.progressBar.isVisible = false

            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { routine ->
                        Log.d("EditRoutine", "Rutina recibida: $routine")
                        currentRoutine = routine
                        populateFormWithRoutineData(routine)
                    } ?: run {
                        showError("La rutina no contiene datos")
                    }
                }
                is Resource.Error -> {
                    showError("Error cargando rutina: ${resource.message}")
                }
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                }
            }
        })

        // Observar el estado de actualización
        routineViewModel.updateRoutineState.observe(viewLifecycleOwner, Observer { resource ->
            binding.btnSaveRoutine.isEnabled = true
            binding.progressBar.isVisible = false

            when (resource) {
                is Resource.Success -> {
                    showToast("✅ Rutina actualizada exitosamente")
                    // Navegar hacia atrás después de un segundo
                    binding.root.postDelayed({
                        findNavController().navigateUp()
                    }, 1000)
                }
                is Resource.Error -> {
                    showError("Error: ${resource.message ?: "Error desconocido"}")
                }
                is Resource.Loading -> {
                    binding.btnSaveRoutine.isEnabled = false
                    binding.progressBar.isVisible = true
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

    private fun selectSportInSpinner(sportName: String?) {
        if (sportName != null) {
            val adapter = binding.spinnerSports.adapter as ArrayAdapter<String>
            val position = adapter.getPosition(sportName)
            if (position >= 0) {
                binding.spinnerSports.setSelection(position)
            }
        }
    }

    private fun populateFormWithRoutineData(routine: RoutineResponse) {
        Log.d("EditRoutine", "Llenando formulario con: ${routine.name}")

        // 1. Nombre
        binding.etRoutineName.setText(routine.name)

        // 2. Deporte (se seleccionará automáticamente cuando cargue el spinner)
        if (routine.sportName != null && sportsMap.isNotEmpty()) {
            selectSportInSpinner(routine.sportName)
        }

        // 3. Descripción
        binding.etRoutineDescription.setText(routine.description ?: "")

        // 4. Objetivo (VERIFICA que este campo existe en RoutineResponse)
        binding.etGoal.setText(routine.goal ?: "")

        // 5. Sesiones por semana (VERIFICA que este campo existe)
        binding.etSessionsPerWeek.setText(routine.sessionsPerWeek.toString())

        // 6. ¡DÍAS DE ENTRENAMIENTO! - Esto es lo que más te interesa
        setTrainingDaysCheckboxes(routine.trainingDays ?: emptySet())

        // 7. Estado activo (opcional - para el botón toggle)
        updateToggleButton(routine.isActive)

        Log.d("EditRoutine", "Formulario llenado correctamente")
    }

    private fun setTrainingDaysCheckboxes(days: Set<String>) {
        // Limpiar todos primero
        clearAllDayCheckboxes()

        // Marcar solo los días que están en el conjunto
        days.forEach { day ->
            when (day.uppercase()) {
                "MONDAY" -> binding.cbMonday.isChecked = true
                "TUESDAY" -> binding.cbTuesday.isChecked = true
                "WEDNESDAY" -> binding.cbWednesday.isChecked = true
                "THURSDAY" -> binding.cbThursday.isChecked = true
                "FRIDAY" -> binding.cbFriday.isChecked = true
                "SATURDAY" -> binding.cbSaturday.isChecked = true
                "SUNDAY" -> binding.cbSunday.isChecked = true
            }
        }

        Log.d("EditRoutine", "Checkboxes configurados: $days")
    }

    private fun clearAllDayCheckboxes() {
        binding.cbMonday.isChecked = false
        binding.cbTuesday.isChecked = false
        binding.cbWednesday.isChecked = false
        binding.cbThursday.isChecked = false
        binding.cbFriday.isChecked = false
        binding.cbSaturday.isChecked = false
        binding.cbSunday.isChecked = false
    }

    private fun updateToggleButton(isActive: Boolean) {
        val text = if (isActive) "Desactivar Rutina" else "Activar Rutina"
        binding.btnToggleActive.text = text
        if (isActive) {
            binding.btnToggleActive.setBackgroundResource(android.R.color.holo_red_dark)
        } else {
            binding.btnToggleActive.setBackgroundResource(R.color.gold_primary)
        }
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
            showDeleteConfirmationDialog()
        }

        binding.btnToggleActive.setOnClickListener {
            currentRoutine?.let { routine ->
                val newActiveState = !routine.isActive
                routineViewModel.updateRoutine(currentRoutineId, UpdateRoutineRequest(isActive = newActiveState))
            }
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

        // Validar que al menos un día esté seleccionado
        val hasDaysSelected = binding.cbMonday.isChecked ||
                binding.cbTuesday.isChecked ||
                binding.cbWednesday.isChecked ||
                binding.cbThursday.isChecked ||
                binding.cbFriday.isChecked ||
                binding.cbSaturday.isChecked ||
                binding.cbSunday.isChecked

        if (!hasDaysSelected) {
            showToast("Selecciona al menos un día de entrenamiento")
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

        Log.d("EditRoutine", "Actualizando rutina:")
        Log.d("EditRoutine", "  - Nombre: $name")
        Log.d("EditRoutine", "  - Días: $trainingDays")
        Log.d("EditRoutine", "  - Deporte ID: $sportId")

        // Crear el request de actualización
        val request = UpdateRoutineRequest(
            name = name,
            description = if (description.isNotBlank()) description else null,
            sportId = sportId,
            trainingDays = trainingDays,
            goal = if (goal.isNotBlank()) goal else null,
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

    private fun safeSetTrainingDays(days: Set<String>?) {
        if (days == null) {
            Log.w("EditRoutine", "⚠️ trainingDays es null, no se marcarán checkboxes")
            clearAllDayCheckboxes()
            return
        }
        setTrainingDaysCheckboxes(days)
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