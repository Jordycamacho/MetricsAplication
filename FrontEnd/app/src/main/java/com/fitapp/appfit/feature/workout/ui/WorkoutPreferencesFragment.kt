package com.fitapp.appfit.feature.workout.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fitapp.appfit.databinding.FragmentWorkoutPreferencesBinding
import com.fitapp.appfit.feature.workout.util.WorkoutPreferences
import com.fitapp.appfit.feature.workout.util.WorkoutSoundManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkoutPreferencesFragment : Fragment() {

    private var _binding: FragmentWorkoutPreferencesBinding? = null
    private val binding get() = _binding!!

    private var isLoadingPreferences = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutPreferencesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        setupListeners()
        loadPreferences()
    }

    private fun loadPreferences() {
        isLoadingPreferences = true

        val ctx = requireContext()
        val vibrationEnabled = WorkoutPreferences.isVibrationEnabled(ctx)
        val soundEnabled = WorkoutPreferences.isSoundEnabled(ctx)
        val soundType = WorkoutPreferences.getSoundType(ctx)
        val setViewType = WorkoutPreferences.getSetViewType(ctx)

        binding.switchVibration.isChecked = vibrationEnabled
        binding.switchSound.isChecked = soundEnabled
        binding.cardSoundType.alpha = if (soundEnabled) 1f else 0.4f
        binding.cardSoundType.isEnabled = soundEnabled

        when (soundType) {
            WorkoutPreferences.SoundType.BEEP  -> binding.rgSoundType.check(binding.rbBeep.id)
            WorkoutPreferences.SoundType.BELL  -> binding.rgSoundType.check(binding.rbBell.id)
            WorkoutPreferences.SoundType.CHIME -> binding.rgSoundType.check(binding.rbChime.id)
        }

        when (setViewType) {
            WorkoutPreferences.SetViewType.CLASSIC -> binding.rgSetViewType.check(binding.rbViewClassic.id)
            WorkoutPreferences.SetViewType.MODERN  -> binding.rgSetViewType.check(binding.rbViewModern.id)
        }

        isLoadingPreferences = false
    }

    private fun setupListeners() {
        binding.switchVibration.setOnCheckedChangeListener { _, checked ->
            if (isLoadingPreferences) return@setOnCheckedChangeListener
            WorkoutPreferences.setVibrationEnabled(requireContext(), checked)
        }

        binding.switchSound.setOnCheckedChangeListener { _, checked ->
            Log.d("PrefsFragment", "switchSound changed to $checked, isLoading=$isLoadingPreferences")
            if (isLoadingPreferences) return@setOnCheckedChangeListener
            WorkoutPreferences.setSoundEnabled(requireContext(), checked)
            Log.d("PrefsFragment", "saved soundEnabled=$checked, verify=${WorkoutPreferences.isSoundEnabled(requireContext())}")

            binding.cardSoundType.animate()
                .alpha(if (checked) 1f else 0.4f)
                .setDuration(200)
                .start()
            binding.cardSoundType.isEnabled = checked

            if (checked) {
                CoroutineScope(Dispatchers.IO).launch {
                    WorkoutSoundManager.playRestFinished(requireContext())
                }
            }
        }

        binding.rgSoundType.setOnCheckedChangeListener { _, checkedId ->
            if (isLoadingPreferences) return@setOnCheckedChangeListener
            val type = when (checkedId) {
                binding.rbBell.id  -> WorkoutPreferences.SoundType.BELL
                binding.rbChime.id -> WorkoutPreferences.SoundType.CHIME
                else               -> WorkoutPreferences.SoundType.BEEP
            }
            WorkoutPreferences.setSoundType(requireContext(), type)

            if (WorkoutPreferences.isSoundEnabled(requireContext())) {
                CoroutineScope(Dispatchers.IO).launch {
                    WorkoutSoundManager.playRestFinished(requireContext())
                }
            }
        }

        // ⭐ NUEVO: RadioGroup para vista de sets
        binding.rgSetViewType.setOnCheckedChangeListener { _, checkedId ->
            if (isLoadingPreferences) return@setOnCheckedChangeListener
            val type = when (checkedId) {
                binding.rbViewClassic.id -> WorkoutPreferences.SetViewType.CLASSIC
                else                     -> WorkoutPreferences.SetViewType.MODERN
            }
            WorkoutPreferences.setSetViewType(requireContext(), type)
            Log.d("PrefsFragment", "setSetViewType changed to $type")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}