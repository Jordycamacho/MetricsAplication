package com.fitapp.appfit.feature.workout.presentation.preferences

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fitapp.appfit.databinding.FragmentWorkoutPreferencesBinding
import com.fitapp.appfit.feature.workout.util.TimerSoundPlayer
import com.fitapp.appfit.feature.workout.util.WorkoutPreferences
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
        val setViewType = WorkoutPreferences.getSetViewType(ctx)
        val timerVolume = WorkoutPreferences.getTimerVolume(ctx)

        binding.switchVibration.isChecked = vibrationEnabled
        binding.switchSound.isChecked = soundEnabled
        binding.cardTimerVolume.alpha = if (soundEnabled) 1f else 0.4f
        binding.cardTimerVolume.isEnabled = soundEnabled
        binding.cardTimerSounds.alpha = if (soundEnabled) 1f else 0.4f
        binding.cardTimerSounds.isEnabled = soundEnabled

        // Volumen
        binding.sliderTimerVolume.value = timerVolume.toFloat()
        updateVolumeLabel(timerVolume)

        // Vista de sets
        when (setViewType) {
            WorkoutPreferences.SetViewType.CLASSIC -> binding.rgSetViewType.check(binding.rbViewClassic.id)
            WorkoutPreferences.SetViewType.MODERN  -> binding.rgSetViewType.check(binding.rbViewModern.id)
        }

        // Sonidos por tipo de timer
        loadTimerSoundPreferences()

        isLoadingPreferences = false
    }

    private fun loadTimerSoundPreferences() {
        val ctx = requireContext()

        val setRestSound = WorkoutPreferences.getTimerSound(ctx, WorkoutPreferences.TimerSoundType.SET_REST)
        val exerciseRestSound = WorkoutPreferences.getTimerSound(ctx, WorkoutPreferences.TimerSoundType.EXERCISE_REST)
        val durationCompleteSound = WorkoutPreferences.getTimerSound(ctx, WorkoutPreferences.TimerSoundType.DURATION_COMPLETE)

        // Set Rest
        binding.rgSetRestSound.check(when (setRestSound) {
            WorkoutPreferences.SoundVariant.BEEP -> binding.rbSetRestBeep.id
            WorkoutPreferences.SoundVariant.BELL -> binding.rbSetRestBell.id
            WorkoutPreferences.SoundVariant.CHIME -> binding.rbSetRestChime.id
            WorkoutPreferences.SoundVariant.BUZZ -> binding.rbSetRestBuzz.id
            WorkoutPreferences.SoundVariant.PING -> binding.rbSetRestPing.id
        })

        // Exercise Rest
        binding.rgExerciseRestSound.check(when (exerciseRestSound) {
            WorkoutPreferences.SoundVariant.BEEP -> binding.rbExerciseRestBeep.id
            WorkoutPreferences.SoundVariant.BELL -> binding.rbExerciseRestBell.id
            WorkoutPreferences.SoundVariant.CHIME -> binding.rbExerciseRestChime.id
            WorkoutPreferences.SoundVariant.BUZZ -> binding.rbExerciseRestBuzz.id
            WorkoutPreferences.SoundVariant.PING -> binding.rbExerciseRestPing.id
        })

        // Duration Complete
        binding.rgDurationCompleteSound.check(when (durationCompleteSound) {
            WorkoutPreferences.SoundVariant.BEEP -> binding.rbDurationBeep.id
            WorkoutPreferences.SoundVariant.BELL -> binding.rbDurationBell.id
            WorkoutPreferences.SoundVariant.CHIME -> binding.rbDurationChime.id
            WorkoutPreferences.SoundVariant.BUZZ -> binding.rbDurationBuzz.id
            WorkoutPreferences.SoundVariant.PING -> binding.rbDurationPing.id
        })
    }

    private fun setupListeners() {
        binding.switchVibration.setOnCheckedChangeListener { _, checked ->
            if (isLoadingPreferences) return@setOnCheckedChangeListener
            WorkoutPreferences.setVibrationEnabled(requireContext(), checked)
        }

        binding.switchSound.setOnCheckedChangeListener { _, checked ->
            if (isLoadingPreferences) return@setOnCheckedChangeListener
            WorkoutPreferences.setSoundEnabled(requireContext(), checked)

            binding.cardTimerVolume.animate().alpha(if (checked) 1f else 0.4f).setDuration(200).start()
            binding.cardTimerVolume.isEnabled = checked
            binding.cardTimerSounds.animate().alpha(if (checked) 1f else 0.4f).setDuration(200).start()
            binding.cardTimerSounds.isEnabled = checked

            if (checked) {
                CoroutineScope(Dispatchers.IO).launch {
                    TimerSoundPlayer.playTimerSound(
                        requireContext(),
                        WorkoutPreferences.TimerSoundType.SET_REST
                    )
                }
            }
        }

        // Volumen
        binding.sliderTimerVolume.addOnChangeListener { _, value, fromUser ->
            if (!fromUser || isLoadingPreferences) return@addOnChangeListener
            val volumeInt = value.toInt()
            WorkoutPreferences.setTimerVolume(requireContext(), volumeInt)
            updateVolumeLabel(volumeInt)

            // Preview
            CoroutineScope(Dispatchers.IO).launch {
                TimerSoundPlayer.playTimerSound(
                    requireContext(),
                    WorkoutPreferences.TimerSoundType.SET_REST
                )
            }
        }

        // Vista de sets
        binding.rgSetViewType.setOnCheckedChangeListener { _, checkedId ->
            if (isLoadingPreferences) return@setOnCheckedChangeListener
            val type = when (checkedId) {
                binding.rbViewClassic.id -> WorkoutPreferences.SetViewType.CLASSIC
                else -> WorkoutPreferences.SetViewType.MODERN
            }
            WorkoutPreferences.setSetViewType(requireContext(), type)
        }

        // Set Rest Sound
        binding.rgSetRestSound.setOnCheckedChangeListener { _, checkedId ->
            if (isLoadingPreferences) return@setOnCheckedChangeListener
            val variant = radioIdToSoundVariant(checkedId, isSetRestGroup = true)
            WorkoutPreferences.setTimerSound(
                requireContext(),
                WorkoutPreferences.TimerSoundType.SET_REST,
                variant
            )
            playPreviewSound(WorkoutPreferences.TimerSoundType.SET_REST)
        }

        // Exercise Rest Sound
        binding.rgExerciseRestSound.setOnCheckedChangeListener { _, checkedId ->
            if (isLoadingPreferences) return@setOnCheckedChangeListener
            val variant = radioIdToSoundVariant(checkedId, isSetRestGroup = false)
            WorkoutPreferences.setTimerSound(
                requireContext(),
                WorkoutPreferences.TimerSoundType.EXERCISE_REST,
                variant
            )
            playPreviewSound(WorkoutPreferences.TimerSoundType.EXERCISE_REST)
        }

        // Duration Complete Sound
        binding.rgDurationCompleteSound.setOnCheckedChangeListener { _, checkedId ->
            if (isLoadingPreferences) return@setOnCheckedChangeListener
            val variant = when (checkedId) {
                binding.rbDurationBeep.id -> WorkoutPreferences.SoundVariant.BEEP
                binding.rbDurationBell.id -> WorkoutPreferences.SoundVariant.BELL
                binding.rbDurationChime.id -> WorkoutPreferences.SoundVariant.CHIME
                binding.rbDurationBuzz.id -> WorkoutPreferences.SoundVariant.BUZZ
                binding.rbDurationPing.id -> WorkoutPreferences.SoundVariant.PING
                else -> WorkoutPreferences.SoundVariant.BEEP
            }
            WorkoutPreferences.setTimerSound(
                requireContext(),
                WorkoutPreferences.TimerSoundType.DURATION_COMPLETE,
                variant
            )
            playPreviewSound(WorkoutPreferences.TimerSoundType.DURATION_COMPLETE)
        }
    }

    private fun radioIdToSoundVariant(checkedId: Int, isSetRestGroup: Boolean): WorkoutPreferences.SoundVariant {
        return if (isSetRestGroup) {
            when (checkedId) {
                binding.rbSetRestBeep.id -> WorkoutPreferences.SoundVariant.BEEP
                binding.rbSetRestBell.id -> WorkoutPreferences.SoundVariant.BELL
                binding.rbSetRestChime.id -> WorkoutPreferences.SoundVariant.CHIME
                binding.rbSetRestBuzz.id -> WorkoutPreferences.SoundVariant.BUZZ
                binding.rbSetRestPing.id -> WorkoutPreferences.SoundVariant.PING
                else -> WorkoutPreferences.SoundVariant.BEEP
            }
        } else {
            when (checkedId) {
                binding.rbExerciseRestBeep.id -> WorkoutPreferences.SoundVariant.BEEP
                binding.rbExerciseRestBell.id -> WorkoutPreferences.SoundVariant.BELL
                binding.rbExerciseRestChime.id -> WorkoutPreferences.SoundVariant.CHIME
                binding.rbExerciseRestBuzz.id -> WorkoutPreferences.SoundVariant.BUZZ
                binding.rbExerciseRestPing.id -> WorkoutPreferences.SoundVariant.PING
                else -> WorkoutPreferences.SoundVariant.BEEP
            }
        }
    }

    private fun playPreviewSound(timerType: WorkoutPreferences.TimerSoundType) {
        CoroutineScope(Dispatchers.IO).launch {
            TimerSoundPlayer.playTimerSound(requireContext(), timerType)
        }
    }

    private fun updateVolumeLabel(volume: Int) {
        binding.tvVolumeValue.text = "$volume%"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}