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
import com.fitapp.appfit.enums.DayOfWeek
import com.fitapp.appfit.model.RoutineViewModel
import com.fitapp.appfit.model.SportViewModel
import com.fitapp.appfit.response.sport.SportResponse
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

        sportViewModel.getPredefinedSports()
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
        sportViewModel.sportsState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { sports ->
                        updateSportsSpinner(sports)
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Error cargando deportes", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                }
            }
        })

        routineViewModel.createRoutineState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    binding.btnCreateRoutine.isEnabled = true
                    Toast.makeText(requireContext(), "Rutina creada exitosamente", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    binding.btnCreateRoutine.isEnabled = true
                    Toast.makeText(requireContext(), "Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    binding.btnCreateRoutine.isEnabled = false
                }
            }
        })
    }

    private fun updateSportsSpinner(sports: List<SportResponse>) {
        sportsMap.clear()
        val sportNames = mutableListOf("Seleccionar deporte")

        sports.forEach { sport ->
            println("Adding sport: ${sport.name} with ID: ${sport.id}")
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

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnCreateRoutine.setOnClickListener {
            createRoutine()
        }
    }

    private fun createRoutine() {
        val name = binding.etRoutineName.text.toString()
        val selectedSport = binding.spinnerSports.selectedItem.toString()
        val description = binding.etRoutineDescription.text.toString()
        val trainingDays = getSelectedTrainingDaysAsStrings()
        val goal = binding.etGoal.text.toString()
        val sessionsPerWeek = binding.etSessionsPerWeek.text.toString().toIntOrNull() ?: 3

        if (name.isBlank()) {
            binding.etRoutineName.error = "El nombre es obligatorio"
            return
        }

        if (goal.isBlank()) {
            binding.etGoal.error = "El objetivo es obligatorio"
            return
        }

        if (sessionsPerWeek == null) {
            binding.etSessionsPerWeek.error = "Las sesiones por semana son requeridas"
            return
        }

        val sportId = if (selectedSport != "Seleccionar deporte") {
            sportsMap[selectedSport] ?: run {
                Toast.makeText(requireContext(), "Error Deporte no válido", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            null
        }

        routineViewModel.createRoutine(
            name, description, sportId, trainingDays, goal, sessionsPerWeek
        )
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