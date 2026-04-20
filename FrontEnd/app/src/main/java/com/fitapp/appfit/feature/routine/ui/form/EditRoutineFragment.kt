package com.fitapp.appfit.feature.routine.ui.form

import android.R
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fitapp.appfit.shared.constants.NavigationKeys
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentEditRoutineBinding
import com.fitapp.appfit.feature.routine.model.rutine.request.UpdateRoutineRequest
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineResponse
import com.fitapp.appfit.feature.routine.ui.RoutineViewModel
import com.fitapp.appfit.feature.routine.util.RoutineValidation
import com.fitapp.appfit.feature.sport.SportViewModel
import com.fitapp.appfit.feature.sport.model.response.SportResponse
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EditRoutineFragment : Fragment() {

    private val VERSION_REGEX = Regex("^\\d+(\\.\\d+){0,2}$")

    private var _binding: FragmentEditRoutineBinding? = null
    private val binding get() = _binding!!

    private val routineViewModel: RoutineViewModel by viewModels()
    private val sportViewModel: SportViewModel by viewModels()
    private val args: EditRoutineFragmentArgs by navArgs()

    private val sportsMap = mutableMapOf<String, Long>()
    private val sportNames = mutableListOf<String>()
    private var currentRoutine: RoutineResponse? = null
    private var sportsReady = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditRoutineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupClickListeners()
        setupObservers()
        sportViewModel.getAllSports()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupClickListeners() {
        binding.btnSaveRoutine.setOnClickListener {
            // Primero validamos todo
            if (validateForm()) {
                updateRoutine()
            } else {
                // Si la validación falla, mostramos un toast genérico (ya hay errores específicos en los campos)
                Toast.makeText(requireContext(), "Revisa los campos en rojo", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDeleteRoutine.setOnClickListener {
            showDeleteConfirmation()
        }

        binding.btnToggleActive.setOnClickListener {
            currentRoutine?.let { routine ->
                routineViewModel.toggleRoutineActiveStatus(args.routineId, !routine.isActive)
            }
        }
    }

    private fun setupObservers() {
        sportViewModel.allSportsState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { updateSportsSpinner(it) }
                    sportsReady = true
                    if (currentRoutine == null) routineViewModel.getRoutine(args.routineId)
                }
                is Resource.Error -> {
                    sportsReady = true
                    if (currentRoutine == null) routineViewModel.getRoutine(args.routineId)
                }
                else -> {}
            }
        }

        routineViewModel.routineDetailState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> setLoading(true)
                is Resource.Success -> {
                    setLoading(false)
                    resource.data?.let { routine ->
                        currentRoutine = routine
                        populateForm(routine)
                    }
                }
                is Resource.Error -> {
                    setLoading(false)
                    Toast.makeText(
                        requireContext(),
                        resource.message ?: "Error cargando rutina",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        routineViewModel.updateRoutineState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> setFormEnabled(false)
                is Resource.Success -> {
                    setFormEnabled(true)
                    Toast.makeText(requireContext(), "✓ Rutina actualizada", Toast.LENGTH_SHORT).show()
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        NavigationKeys.ROUTINE_UPDATED, true
                    )
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    setFormEnabled(true)
                    Toast.makeText(
                        requireContext(),
                        "Error: ${resource.message ?: "No se pudo guardar"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        routineViewModel.deleteRoutineState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> setFormEnabled(false)
                is Resource.Success -> {
                    setFormEnabled(true)
                    Toast.makeText(requireContext(), "✓ Rutina eliminada", Toast.LENGTH_SHORT).show()
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        NavigationKeys.ROUTINE_DELETED, true
                    )
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    setFormEnabled(true)
                    Toast.makeText(
                        requireContext(),
                        resource.message ?: "Error al eliminar",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        routineViewModel.toggleActiveState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    routineViewModel.getRoutine(args.routineId)
                }
                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        resource.message ?: "Error al cambiar estado",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is Resource.Loading<*> -> TODO()
            }
        }

        routineViewModel.validationErrors.observe(viewLifecycleOwner) { errors ->
            if (errors.isNotEmpty()) {
                showValidationErrors(errors)
            }
        }
    }

    private fun updateSportsSpinner(sports: List<SportResponse>) {
        sportsMap.clear()
        sportNames.clear()
        sportNames.add("Sin deporte específico")
        sports.forEach { sport ->
            sportNames.add(sport.name)
            sportsMap[sport.name] = sport.id
        }
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, sportNames)
        binding.spinnerSports.setAdapter(adapter)
    }

    private fun selectSport(sportId: Long?, sportName: String?) {
        if (sportId == null || sportName == null) {
            binding.spinnerSports.setText(sportNames.firstOrNull() ?: "", false)
            return
        }
        val label = sportsMap.entries.find { it.value == sportId }?.key
            ?: sportNames.find { it.equals(sportName, ignoreCase = true) }
            ?: sportNames.firstOrNull() ?: ""
        binding.spinnerSports.setText(label, false)
    }

    // ==================== MOSTRAR MODO (días o sesiones) ====================

    private fun setupFrequencyMode(routine: RoutineResponse) {
        val hasTrainingDays = !routine.trainingDays.isNullOrEmpty()
        if (hasTrainingDays) {
            // Modo días específicos
            binding.layoutDaysContainer.visibility = View.VISIBLE
            binding.layoutSessionsContainer.visibility = View.GONE
            clearDays()
            routine.trainingDays?.forEach { day ->
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
        } else {
            // Modo sesiones por semana
            binding.layoutDaysContainer.visibility = View.GONE
            binding.layoutSessionsContainer.visibility = View.VISIBLE
            val sessions = routine.sessionsPerWeek?.takeIf { it in 1..7 } ?: 3
            binding.etSessionsPerWeek.setText(sessions.toString())
        }
    }

    private fun populateForm(routine: RoutineResponse) {
        binding.etRoutineName.setText(routine.name)
        binding.etGoal.setText(routine.goal ?: "")
        binding.etVersion.setText(routine.version ?: "")

        // Mostrar info V2
        if (!routine.exportKey.isNullOrBlank()) {
            binding.tvExportKey.text = "🔑 Clave exportación: ${routine.exportKey}"
            binding.tvExportKey.visibility = View.VISIBLE
        }
        if (routine.timesPurchased != null && routine.timesPurchased > 0) {
            binding.tvTimesPurchased.text = "🛒 Comprada ${routine.timesPurchased} veces"
            binding.tvTimesPurchased.visibility = View.VISIBLE
        }
        if (routine.originalRoutineId != null) {
            binding.tvOriginalId.text = "📎 Importada desde ID: ${routine.originalRoutineId}"
            binding.tvOriginalId.visibility = View.VISIBLE
        }

        setupFrequencyMode(routine)

        selectSport(routine.sportId, routine.sportName)
        updateToggleButton(routine.isActive)

        val exerciseCount = routine.exercises?.size ?: 0
        val infoText = buildString {
            append("$exerciseCount ejercicios")
            routine.originalRoutineId?.let { append(" • Importada") }
            routine.version?.let { append(" • v$it") }
        }
        binding.tvRoutineInfo.text = infoText
    }

    private fun updateToggleButton(isActive: Boolean) {
        if (isActive) {
            binding.btnToggleActive.text = "DESACTIVAR RUTINA"
            binding.btnToggleActive.setBackgroundColor(Color.parseColor("#1AE41D1D"))
        } else {
            binding.btnToggleActive.text = "ACTIVAR RUTINA"
            binding.btnToggleActive.setBackgroundColor(Color.parseColor("#1A4CAF50"))
        }
    }

    // ==================== VALIDACIÓN CORREGIDA ====================

    private fun validateForm(): Boolean {
        // Limpiar errores previos
        binding.etRoutineName.error = null
        binding.etGoal.error = null
        if (binding.layoutSessionsContainer.visibility == View.VISIBLE) {
            binding.etSessionsPerWeek.error = null
        }

        val name = binding.etRoutineName.text.toString().trim()
        if (name.isEmpty()) {
            binding.etRoutineName.error = "El nombre es obligatorio"
            return false
        }

        val goal = binding.etGoal.text.toString().trim()
        if (goal.isEmpty()) {
            binding.etGoal.error = "El objetivo es obligatorio"
            return false
        }

        // Validar según el modo visible
        val useDays = binding.layoutDaysContainer.visibility == View.VISIBLE

        if (useDays) {
            // Modo días: al menos un día seleccionado
            val selectedDays = getSelectedDays()
            if (selectedDays.isEmpty()) {
                Toast.makeText(requireContext(), "Selecciona al menos un día de entrenamiento", Toast.LENGTH_SHORT).show()
                return false
            }
        } else {
            // Modo sesiones: número entre 1 y 7
            val sessionsText = binding.etSessionsPerWeek.text.toString()
            val sessions = sessionsText.toIntOrNull()
            if (sessions == null || sessions !in 1..7) {
                binding.etSessionsPerWeek.error = "Debe ser un número entre 1 y 7"
                return false
            }
        }

        return true
    }

    private fun validateVersion(): Boolean {
        val version = binding.etVersion.text.toString().trim()
        if (version.isNotEmpty() && !VERSION_REGEX.matches(version)) {
            binding.tilVersion.error = "Formato inválido. Usa números y puntos (ej: 1.0.0, 2.1, 3)"
            return false
        }
        binding.tilVersion.error = null
        return true
    }

    private fun showValidationErrors(errors: Map<String, String>) {
        errors["name"]?.let { binding.etRoutineName.error = it }
        errors["goal"]?.let { binding.etGoal.error = it }
        errors["sessionsPerWeek"]?.let { binding.etSessionsPerWeek.error = it }
        errors["trainingDays"]?.let {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
    }

    // ==================== ACTUALIZAR RUTINA ====================

    private fun updateRoutine() {
        if (!validateVersion()) return

        val selectedText = binding.spinnerSports.text.toString()
        val sportId = sportsMap[selectedText]
        val goal = binding.etGoal.text.toString().trim().ifEmpty { null }
        val version = binding.etVersion.text.toString().trim().takeIf { it.isNotEmpty() }

        val useDays = binding.layoutDaysContainer.visibility == View.VISIBLE

        val trainingDays: List<String>? = if (useDays) getSelectedDays() else null
        val sessionsPerWeek: Int? = if (!useDays) {
            binding.etSessionsPerWeek.text.toString().toIntOrNull()
        } else null

        val request = UpdateRoutineRequest(
            name = binding.etRoutineName.text.toString().trim(),
            description = null,
            sportId = sportId,
            trainingDays = trainingDays,
            goal = goal,
            sessionsPerWeek = sessionsPerWeek,
            version = version
        )

        // Llamar al ViewModel
        routineViewModel.updateRoutine(args.routineId, request)
    }

    // ==================== DIÁLOGO ELIMINAR ====================

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar rutina")
            .setMessage("¿Eliminar \"${currentRoutine?.name}\"? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                routineViewModel.deleteRoutine(args.routineId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ==================== UTILIDADES ====================

    private fun getSelectedDays(): List<String> = buildList {
        if (binding.cbMonday.isChecked) add("MONDAY")
        if (binding.cbTuesday.isChecked) add("TUESDAY")
        if (binding.cbWednesday.isChecked) add("WEDNESDAY")
        if (binding.cbThursday.isChecked) add("THURSDAY")
        if (binding.cbFriday.isChecked) add("FRIDAY")
        if (binding.cbSaturday.isChecked) add("SATURDAY")
        if (binding.cbSunday.isChecked) add("SUNDAY")
    }

    private fun clearDays() {
        binding.cbMonday.isChecked = false
        binding.cbTuesday.isChecked = false
        binding.cbWednesday.isChecked = false
        binding.cbThursday.isChecked = false
        binding.cbFriday.isChecked = false
        binding.cbSaturday.isChecked = false
        binding.cbSunday.isChecked = false
    }

    private fun setLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setFormEnabled(enabled: Boolean) {
        binding.btnSaveRoutine.isEnabled = enabled
        binding.btnDeleteRoutine.isEnabled = enabled
        binding.btnToggleActive.isEnabled = enabled
        binding.progressBar.visibility = if (enabled) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}