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
import com.fitapp.appfit.databinding.FragmentCreateRoutineBinding
import com.fitapp.appfit.model.RoutineViewModel
import com.fitapp.appfit.model.SportViewModel
import com.fitapp.appfit.response.sport.response.SportResponse
import com.fitapp.appfit.utils.FormValidator
import com.fitapp.appfit.utils.Resource

class CreateRoutineFragment : Fragment() {
    private var _binding: FragmentCreateRoutineBinding? = null
    private val binding get() = _binding!!
    private val routineViewModel: RoutineViewModel by viewModels()
    private val sportViewModel: SportViewModel by viewModels()
    private var sportsMap = mutableMapOf<String, Long>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateRoutineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSportsSpinner()
        setupClickListeners()
        setupObservers()

        // CAMBIO: Obtener TODOS los deportes, no solo predefinidos
        sportViewModel.getAllSports()

        setupTextWatchers()
    }

    private fun setupTextWatchers() {
        binding.etRoutineName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val name = binding.etRoutineName.text.toString()
                val result = FormValidator.validateRoutineName(name)
                if (result is FormValidator.ValidationResult.Error) {
                    binding.etRoutineName.error = result.message
                }
            }
        }

        binding.etGoal.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val goal = binding.etGoal.text.toString()
                val result = FormValidator.validateGoal(goal)
                if (result is FormValidator.ValidationResult.Error) {
                    binding.etGoal.error = result.message
                }
            }
        }

        binding.etSessionsPerWeek.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val sessions = binding.etSessionsPerWeek.text.toString()
                val result = FormValidator.validateSessionsPerWeek(sessions)
                if (result is FormValidator.ValidationResult.Error) {
                    binding.etSessionsPerWeek.error = result.message
                }
            }
        }
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
        // CAMBIO: Observar todos los deportes en lugar de solo predefinidos
        sportViewModel.allSportsState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { sports ->
                        updateSportsSpinner(sports)
                    }
                }
                is Resource.Error -> {
                    showToast("Error cargando deportes: ${resource.message}")
                }
                is Resource.Loading -> {
                    // Puedes mostrar un progress bar si quieres
                }
            }
        })

        routineViewModel.createRoutineState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    binding.btnCreateRoutine.isEnabled = true
                    showToast("✅ Rutina creada exitosamente")

                    // Navegar a otra pantalla después de 1 segundo
                    binding.root.postDelayed({
                        findNavController().navigateUp()
                    }, 1000)
                }
                is Resource.Error -> {
                    binding.btnCreateRoutine.isEnabled = true
                    showError(resource.message ?: "Error desconocido")
                }
                is Resource.Loading -> {
                    binding.btnCreateRoutine.isEnabled = false
                }
            }
        })
    }

    private fun updateSportsSpinner(sports: List<SportResponse>) {
        sportsMap.clear()
        val sportNames = mutableListOf("Seleccionar deporte (opcional)")

        sports.forEach { sport ->
            val displayName = if (sport.isPredefined) {
                " ${sport.name} (Predefinido)"
            } else {
                " ${sport.name} (Personalizado)"
            }
            sportNames.add(displayName)
            sportsMap[displayName] = sport.id
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            sportNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSports.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnCreateRoutine.setOnClickListener {
            if (validateAllFields()) {
                createRoutine()
            }
        }
    }

    private fun validateAllFields(): Boolean {
        var isValid = true

        // Validar nombre
        val nameResult = FormValidator.validateRoutineName(binding.etRoutineName.text.toString())
        if (nameResult is FormValidator.ValidationResult.Error) {
            binding.etRoutineName.error = nameResult.message
            isValid = false
        }

        // Validar objetivo
        val goalResult = FormValidator.validateGoal(binding.etGoal.text.toString())
        if (goalResult is FormValidator.ValidationResult.Error) {
            binding.etGoal.error = goalResult.message
            isValid = false
        }

        // Validar sesiones
        val sessionsResult = FormValidator.validateSessionsPerWeek(binding.etSessionsPerWeek.text.toString())
        if (sessionsResult is FormValidator.ValidationResult.Error) {
            binding.etSessionsPerWeek.error = sessionsResult.message
            isValid = false
        }

        // Validar días de entrenamiento
        val days = getSelectedTrainingDaysAsStrings()
        val daysResult = FormValidator.validateTrainingDays(days)
        if (daysResult is FormValidator.ValidationResult.Error) {
            showToast(daysResult.message)
            isValid = false
        }

        return isValid
    }

    private fun createRoutine() {
        val name = binding.etRoutineName.text.toString()
        val selectedSport = binding.spinnerSports.selectedItem.toString()
        val description = binding.etRoutineDescription.text.toString()
        val trainingDays = getSelectedTrainingDaysAsStrings()
        val goal = binding.etGoal.text.toString()
        val sessionsPerWeek = binding.etSessionsPerWeek.text.toString().toIntOrNull() ?: 3

        // Obtener sportId del mapa
        val sportId = if (selectedSport != "Seleccionar deporte (opcional)") {
            sportsMap[selectedSport] ?: run {
                showError("Error: Deporte no válido")
                return
            }
        } else {
            null
        }

        routineViewModel.createRoutine(
            name,
            if (description.isNotBlank()) description else null,
            sportId,
            trainingDays,
            goal,
            sessionsPerWeek
        )
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
}