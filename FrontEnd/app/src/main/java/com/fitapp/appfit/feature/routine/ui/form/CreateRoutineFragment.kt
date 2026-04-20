package com.fitapp.appfit.feature.routine.ui.form

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentCreateRoutineBinding
import com.fitapp.appfit.feature.routine.ui.RoutineViewModel
import com.fitapp.appfit.feature.sport.SportViewModel
import com.fitapp.appfit.feature.sport.model.response.SportResponse

class CreateRoutineFragment : Fragment() {

    private var _binding: FragmentCreateRoutineBinding? = null
    private val binding get() = _binding!!
    private val routineViewModel: RoutineViewModel by viewModels()
    private val sportViewModel: SportViewModel by viewModels()
    private val sportsMap = mutableMapOf<String, Long>()
    private val sportNames = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateRoutineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupObservers()
        setupFrequencyMode()
        binding.btnCreateRoutine.setOnClickListener {
            if (validateForm()) createRoutine()
        }
        sportViewModel.getAllSports()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
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
        binding.spinnerSports.setText(sportNames[0], false)
    }

    private fun setupObservers() {
        sportViewModel.allSportsState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                resource.data?.let { updateSportsSpinner(it) }
            }
        }

        routineViewModel.createRoutineState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> setFormEnabled(false)
                is Resource.Success -> {
                    setFormEnabled(true)
                    Toast.makeText(requireContext(), "✓ Rutina creada", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    setFormEnabled(true)
                    Toast.makeText(
                        requireContext(),
                        resource.message ?: "Error al crear",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        routineViewModel.validationErrors.observe(viewLifecycleOwner) { errors ->
            if (errors.isNotEmpty()) {
                showValidationErrors(errors)
            }
        }
    }

    // ── Validación (ahora basada en visibilidad) ──────────────────────────────

    private fun validateForm(): Boolean {
        binding.etRoutineName.error = null
        binding.etGoal.error = null
        if (binding.layoutSessionsContainer.visibility == View.VISIBLE) {
            binding.etSessionsPerWeek.error = null
        }

        val name = binding.etRoutineName.text.toString().trim()
        val goal = binding.etGoal.text.toString().trim()

        if (name.isEmpty()) {
            binding.etRoutineName.error = "El nombre es obligatorio"
            return false
        }
        if (goal.isEmpty()) {
            binding.etGoal.error = "El objetivo es obligatorio"
            return false
        }

        val useDays = binding.layoutDaysContainer.visibility == View.VISIBLE

        if (useDays) {
            val selectedDays = getSelectedDays()
            if (selectedDays.isEmpty()) {
                Toast.makeText(requireContext(), "Selecciona al menos un día", Toast.LENGTH_SHORT).show()
                return false
            }
        } else {
            val sessionsText = binding.etSessionsPerWeek.text.toString()
            val sessions = sessionsText.toIntOrNull()
            if (sessions == null || sessions !in 1..7) {
                binding.etSessionsPerWeek.error = "Debe ser un número entre 1 y 7"
                return false
            }
        }
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

    private fun setupFrequencyMode() {
        binding.switchMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.layoutDaysContainer.visibility = View.VISIBLE
                binding.layoutSessionsContainer.visibility = View.GONE
            } else {
                binding.layoutDaysContainer.visibility = View.GONE
                binding.layoutSessionsContainer.visibility = View.VISIBLE
            }
        }
    }

    // ── Crear rutina (también basada en visibilidad) ──────────────────────────

    private fun createRoutine() {
        val name = binding.etRoutineName.text.toString().trim()
        val goal = binding.etGoal.text.toString().trim()
        val version = binding.etVersion.text.toString().trim().takeIf { it.isNotEmpty() }
        val selectedSportText = binding.spinnerSports.text.toString()
        val sportId = sportsMap[selectedSportText]

        val useDays = binding.layoutDaysContainer.visibility == View.VISIBLE

        val trainingDays: List<String>
        val sessionsPerWeek: Int

        if (useDays) {
            trainingDays = getSelectedDays()
            sessionsPerWeek = trainingDays.size  // o podrías usar 0 si el backend no lo necesita
        } else {
            trainingDays = emptyList()
            sessionsPerWeek = binding.etSessionsPerWeek.text.toString().toIntOrNull() ?: 3
        }

        routineViewModel.createRoutine(
            name = name,
            description = null,
            sportId = sportId,
            trainingDays = trainingDays,
            goal = goal,
            sessionsPerWeek = sessionsPerWeek,
            version = version,
            originalRoutineId = null,
            packageId = null
        )
    }

    private fun getSelectedDays(): List<String> = buildList {
        if (binding.cbMonday.isChecked) add("MONDAY")
        if (binding.cbTuesday.isChecked) add("TUESDAY")
        if (binding.cbWednesday.isChecked) add("WEDNESDAY")
        if (binding.cbThursday.isChecked) add("THURSDAY")
        if (binding.cbFriday.isChecked) add("FRIDAY")
        if (binding.cbSaturday.isChecked) add("SATURDAY")
        if (binding.cbSunday.isChecked) add("SUNDAY")
    }

    private fun setFormEnabled(enabled: Boolean) {
        binding.btnCreateRoutine.isEnabled = enabled
        binding.progressBar.visibility = if (enabled) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}