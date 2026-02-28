package com.fitapp.appfit.ui.routines.create

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
import com.fitapp.appfit.constants.NavigationKeys
import com.fitapp.appfit.databinding.FragmentEditRoutineBinding
import com.fitapp.appfit.model.RoutineViewModel
import com.fitapp.appfit.model.SportViewModel
import com.fitapp.appfit.response.routine.request.UpdateRoutineRequest
import com.fitapp.appfit.response.routine.response.RoutineResponse
import com.fitapp.appfit.response.sport.response.SportResponse
import com.fitapp.appfit.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EditRoutineFragment : Fragment() {

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

    // ── Toolbar ─────────────────────────────────────────────────────────────

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    // ── Click listeners ───────────────────────────────────────────────────────

    private fun setupClickListeners() {
        binding.btnSaveRoutine.setOnClickListener {
            if (validateForm()) updateRoutine()
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

    // ── Observadores ─────────────────────────────────────────────────────────

    private fun setupObservers() {
        sportViewModel.allSportsState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { updateSportsSpinner(it) }
                    sportsReady = true
                    // Cargamos la rutina DESPUÉS de tener los deportes listos
                    if (currentRoutine == null) routineViewModel.getRoutine(args.routineId)
                }
                is Resource.Error -> {
                    // Intentamos cargar igual sin deportes
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
                    Toast.makeText(requireContext(), "Error cargando rutina", Toast.LENGTH_SHORT).show()
                }
            }
        }

        routineViewModel.updateRoutineState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> setFormEnabled(false)
                is Resource.Success -> {
                    setFormEnabled(true)
                    Toast.makeText(requireContext(), "Rutina actualizada", Toast.LENGTH_SHORT).show()
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        NavigationKeys.ROUTINE_UPDATED, true
                    )
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    setFormEnabled(true)
                    Toast.makeText(requireContext(), resource.message ?: "Error al guardar", Toast.LENGTH_LONG).show()
                }
            }
        }

        routineViewModel.deleteRoutineState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> setFormEnabled(false)
                is Resource.Success -> {
                    setFormEnabled(true)
                    Toast.makeText(requireContext(), "Rutina eliminada", Toast.LENGTH_SHORT).show()
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        NavigationKeys.ROUTINE_DELETED, true
                    )
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    setFormEnabled(true)
                    Toast.makeText(requireContext(), resource.message ?: "Error al eliminar", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }

        // Toggle activo/inactivo: recarga rutina para actualizar botón
        routineViewModel.toggleActiveState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                routineViewModel.getRoutine(args.routineId)
            }
        }
    }

    // ── Spinner de deportes ──────────────────────────────────────────────────

    private fun updateSportsSpinner(sports: List<SportResponse>) {
        sportsMap.clear()
        sportNames.clear()
        sportNames.add("Sin deporte específico")
        sports.forEach { sport ->
            sportNames.add(sport.name)
            sportsMap[sport.name] = sport.id
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sportNames)
        binding.spinnerSports.setAdapter(adapter)
    }

    private fun selectSport(sportId: Long?, sportName: String?) {
        if (sportId == null || sportName == null) {
            binding.spinnerSports.setText(sportNames.firstOrNull() ?: "", false)
            return
        }
        // Buscar por ID en el mapa (más fiable que por nombre)
        val label = sportsMap.entries.find { it.value == sportId }?.key
            ?: sportNames.find { it.equals(sportName, ignoreCase = true) }
            ?: sportNames.firstOrNull() ?: ""
        binding.spinnerSports.setText(label, false)
    }

    // ── Poblar formulario ─────────────────────────────────────────────────────

    private fun populateForm(routine: RoutineResponse) {
        binding.etRoutineName.setText(routine.name)
        binding.etGoal.setText(routine.goal ?: "")
        val sessions = routine.sessionsPerWeek?.takeIf { it > 0 } ?: 3
        binding.etSessionsPerWeek.setText(sessions.toString())

        // Días
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

        // Deporte
        selectSport(routine.sportId, routine.sportName)

        // Toggle activo/inactivo
        updateToggleButton(routine.isActive)

        // Info header
        val exerciseCount = routine.exercises?.size ?: 0
        binding.tvRoutineInfo.text = "$exerciseCount ejercicios"
    }

    private fun updateToggleButton(isActive: Boolean) {
        if (isActive) {
            binding.btnToggleActive.text = "DESACTIVAR RUTINA"
            binding.btnToggleActive.setBackgroundColor(android.graphics.Color.parseColor("#1AE41D1D"))
        } else {
            binding.btnToggleActive.text = "ACTIVAR RUTINA"
            binding.btnToggleActive.setBackgroundColor(android.graphics.Color.parseColor("#1A4CAF50"))
        }
    }

    // ── Validación ────────────────────────────────────────────────────────────

    private fun validateForm(): Boolean {
        if (binding.etRoutineName.text.toString().trim().isEmpty()) {
            binding.etRoutineName.error = "El nombre es obligatorio"
            return false
        }
        val sessions = binding.etSessionsPerWeek.text.toString().toIntOrNull()
        if (sessions == null || sessions < 1 || sessions > 7) {
            binding.etSessionsPerWeek.error = "Debe ser un número entre 1 y 7"
            return false
        }
        if (getSelectedDays().isEmpty()) {
            Toast.makeText(requireContext(), "Selecciona al menos un día", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    // ── Actualizar rutina ─────────────────────────────────────────────────────

    private fun updateRoutine() {
        val selectedText = binding.spinnerSports.text.toString()
        val sportId = sportsMap[selectedText]
        val goal = binding.etGoal.text.toString().trim().ifEmpty { null }

        val request = UpdateRoutineRequest(
            name = binding.etRoutineName.text.toString().trim(),
            description = null,
            sportId = sportId,
            trainingDays = getSelectedDays(),
            goal = goal,
            sessionsPerWeek = binding.etSessionsPerWeek.text.toString().toIntOrNull() ?: 3
        )
        routineViewModel.updateRoutine(args.routineId, request)
    }

    // ── Diálogo eliminar ──────────────────────────────────────────────────────

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar rutina")
            .setMessage("¿Eliminar \"${currentRoutine?.name}\"? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ -> routineViewModel.deleteRoutine(args.routineId) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

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
        binding.cbMonday.isChecked = false; binding.cbTuesday.isChecked = false
        binding.cbWednesday.isChecked = false; binding.cbThursday.isChecked = false
        binding.cbFriday.isChecked = false; binding.cbSaturday.isChecked = false
        binding.cbSunday.isChecked = false
    }

    private fun setLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setFormEnabled(enabled: Boolean) {
        binding.btnSaveRoutine.isEnabled = enabled
        binding.btnDeleteRoutine.isEnabled = enabled
        binding.progressBar.visibility = if (enabled) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}