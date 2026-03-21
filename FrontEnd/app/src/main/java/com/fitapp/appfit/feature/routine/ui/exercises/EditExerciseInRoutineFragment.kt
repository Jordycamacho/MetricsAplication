package com.fitapp.appfit.feature.routine.ui.exercises

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fitapp.appfit.R
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentEditExerciseInRoutineBinding
import com.fitapp.appfit.feature.routine.model.rutinexercise.request.AddExerciseToRoutineRequest
import com.fitapp.appfit.feature.routine.ui.RoutineExerciseViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar

class EditExerciseInRoutineFragment : Fragment() {

    private var _binding: FragmentEditExerciseInRoutineBinding? = null
    private val binding get() = _binding!!

    private val args: EditExerciseInRoutineFragmentArgs by navArgs()
    private val viewModel: RoutineExerciseViewModel by viewModels()

    private val chipToDayMap = mapOf(
        R.id.chip_monday    to "MONDAY",
        R.id.chip_tuesday   to "TUESDAY",
        R.id.chip_wednesday to "WEDNESDAY",
        R.id.chip_thursday  to "THURSDAY",
        R.id.chip_friday    to "FRIDAY",
        R.id.chip_saturday  to "SATURDAY",
        R.id.chip_sunday    to "SUNDAY"
    )

    private val dayToChipMap = chipToDayMap.entries.associate { (k, v) -> v to k }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditExerciseInRoutineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupDayChips()
        prefillData()
        setupSaveButton()
        setupObservers()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupDayChips() {
        val allowedDays = args.trainingDays
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()

        val showAll = allowedDays.isEmpty()

        chipToDayMap.forEach { (chipId, day) ->
            val chip = binding.chipGroupDays.findViewById<Chip>(chipId)
            chip?.visibility = if (showAll || day in allowedDays) View.VISIBLE else View.GONE
        }
    }

    private fun prefillData() {
        binding.tvExerciseName.text = args.exerciseName

        val currentDay = args.dayOfWeek
        if (!currentDay.isNullOrEmpty()) {
            val chipId = dayToChipMap[currentDay]
            if (chipId != null) {
                binding.chipGroupDays.check(chipId)
            }
        }

        binding.etSessionOrder.setText(args.sessionOrder.takeIf { it > 0 }?.toString() ?: "1")
        binding.etRestAfter.setText(args.restAfterExercise.takeIf { it > 0 }?.toString() ?: "60")
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener { saveChanges() }
    }

    private fun saveChanges() {
        val selectedChipId = binding.chipGroupDays.checkedChipId
        val selectedDay = chipToDayMap[selectedChipId]

        if (selectedDay == null) {
            Snackbar.make(binding.root, "Selecciona un día de entrenamiento", Snackbar.LENGTH_SHORT).show()
            return
        }

        val sessionOrder = binding.etSessionOrder.text.toString().toIntOrNull()
        if (sessionOrder == null || sessionOrder < 1) {
            binding.etSessionOrder.error = "Orden inválido"
            binding.etSessionOrder.requestFocus()
            return
        }

        val restAfter = binding.etRestAfter.text.toString().toIntOrNull() ?: 60

        val request = AddExerciseToRoutineRequest(
            exerciseId = args.exerciseId,
            sessionNumber = null,
            dayOfWeek = selectedDay,
            sessionOrder = sessionOrder,
            restAfterExercise = restAfter,
            targetParameters = null,
            sets = null
        )

        viewModel.updateExerciseInRoutine(args.routineId, args.routineExerciseId, request)
    }

    private fun setupObservers() {
        viewModel.updateExerciseState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> setFormEnabled(false)
                is Resource.Success -> {
                    setFormEnabled(true)
                    Snackbar.make(binding.root, "✅ Ejercicio actualizado", Snackbar.LENGTH_SHORT).show()
                    viewModel.clearUpdateState()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    setFormEnabled(true)
                    Snackbar.make(
                        binding.root,
                        resource.message ?: "Error al actualizar",
                        Snackbar.LENGTH_LONG
                    ).show()
                    viewModel.clearUpdateState()
                }
                null -> {}
                else -> {}
            }
        }
    }

    private fun setFormEnabled(enabled: Boolean) {
        binding.btnSave.isEnabled = enabled
        binding.etSessionOrder.isEnabled = enabled
        binding.etRestAfter.isEnabled = enabled
        chipToDayMap.keys.forEach { chipId ->
            binding.chipGroupDays.findViewById<Chip>(chipId)?.isEnabled = enabled
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}