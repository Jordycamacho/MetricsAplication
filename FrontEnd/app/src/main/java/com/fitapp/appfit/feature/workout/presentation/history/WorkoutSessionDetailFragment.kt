package com.fitapp.appfit.feature.workout.presentation.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.R
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentWorkoutSessionDetailBinding
import com.fitapp.appfit.feature.workout.model.response.SessionExerciseResponse
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionResponse
import com.fitapp.appfit.feature.workout.presentation.execution.SessionExerciseAdapter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WorkoutSessionDetailFragment : Fragment() {

    private var _binding: FragmentWorkoutSessionDetailBinding? = null
    private val binding get() = _binding!!

    private val args: WorkoutSessionDetailFragmentArgs by navArgs()
    private val viewModel: WorkoutSessionDetailViewModel by viewModels()
    private val exerciseAdapter = SessionExerciseAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutSessionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        // Eliminado el menú de eliminar porque no tenías action_delete
        viewModel.loadSessionDetails(args.sessionId)
    }

    private fun setupRecyclerView() {
        binding.rvExercises.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = exerciseAdapter
            setHasFixedSize(false)
        }
    }

    private fun setupObservers() {
        viewModel.sessionDetailState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.scrollView.isVisible = false
                    binding.tvError.isVisible = false
                }
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    binding.scrollView.isVisible = true
                    binding.tvError.isVisible = false
                    resource.data?.let { displaySessionDetails(it) }
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    binding.scrollView.isVisible = false
                    binding.tvError.isVisible = true
                    binding.tvError.text = resource.message ?: "Error al cargar sesión"
                }
            }
        }

        viewModel.comparisonState.observe(viewLifecycleOwner) { comparison ->
            if (comparison != null) displayComparison(comparison)
            else binding.layoutComparison.isVisible = false
        }

        viewModel.previousExercisesState.observe(viewLifecycleOwner) { prevExercises: List<SessionExerciseResponse> ->
            exerciseAdapter.setPreviousExercises(prevExercises)
        }
    }

    private fun displaySessionDetails(session: WorkoutSessionResponse) {
        binding.tvRoutineName.text = session.routineName ?: "Entrenamiento"
        binding.tvDate.text = formatDate(session.startTime)
        binding.tvDuration.text = formatDuration(session.durationSeconds)
        binding.tvExerciseCount.text = "${session.exercises?.size ?: 0}"

        val totalSets = session.exercises?.sumOf { it.sets?.size ?: 0 } ?: 0
        binding.tvSetCount.text = "$totalSets"

        binding.tvVolume.text = if ((session.totalVolume ?: 0.0) > 0) {
            String.format("%.1f kg", session.totalVolume)
        } else "0.0 kg"

        session.performanceScore?.let { score ->
            binding.layoutPerformance.isVisible = true
            binding.tvPerformanceScore.text = "$score/10"
            binding.progressPerformance.progress = score * 10
        } ?: run { binding.layoutPerformance.isVisible = false }

        exerciseAdapter.submitList(session.exercises ?: emptyList())

        val prs = detectPersonalRecords(session)
        binding.layoutPersonalRecords.isVisible = prs.isNotEmpty()
        if (prs.isNotEmpty()) {
            // Lambda con tipo explícito
            binding.tvPersonalRecords.text = prs.joinToString("\n") { record: PersonalRecord ->
                "• ${record.exerciseName}: ${record.description}"
            }
        }
    }

    private fun displayComparison(comparison: SessionComparison) {
        binding.layoutComparison.isVisible = true
        binding.tvComparisonDate.text = "vs ${formatDateShort(comparison.previousSessionDate)}"

        val vd = comparison.volumeDifference
        binding.tvVolumeComparison.text = when {
            vd > 0 -> "+${String.format("%.1f", vd)} kg"
            vd < 0 -> "${String.format("%.1f", vd)} kg"
            else   -> "= 0 kg"
        }
        binding.tvVolumeComparison.setTextColor(requireContext().getColor(
            when { vd > 0 -> R.color.set_completed_green; vd < 0 -> R.color.red; else -> R.color.text_secondary_dark }
        ))

        val dd = comparison.durationDifference
        binding.tvDurationComparison.text = when {
            dd > 0 -> "+${formatDuration(dd)}"
            dd < 0 -> "-${formatDuration(-dd)}"
            else   -> "= 0m"
        }
        binding.tvDurationComparison.setTextColor(
            requireContext().getColor(R.color.text_primary_dark)
        )

        val sd = comparison.setsDifference
        binding.tvSetsComparison.text = when { sd > 0 -> "+$sd"; sd < 0 -> "$sd"; else -> "= 0" }
        binding.tvSetsComparison.setTextColor(requireContext().getColor(
            when { sd > 0 -> R.color.set_completed_green; sd < 0 -> R.color.red; else -> R.color.text_secondary_dark }
        ))
    }

    private fun detectPersonalRecords(session: WorkoutSessionResponse): List<PersonalRecord> {
        val prs = mutableListOf<PersonalRecord>()
        session.exercises?.forEach { exercise ->
            exercise.sets?.forEach { set ->
                if (set.parameters?.any { it.isPersonalRecord == true } == true) {
                    val param = set.parameters.find {
                        it.parameterType?.uppercase() in listOf("NUMBER", "WEIGHT")
                    }
                    val desc = if (param?.numericValue != null && param.integerValue != null)
                        "${String.format("%.1f", param.numericValue)}kg × ${param.integerValue} reps"
                    else "Nuevo récord"
                    val name = when {
                        !exercise.exerciseName.isNullOrBlank() -> exercise.exerciseName
                        exercise.exerciseId != null -> "Ejercicio #${exercise.exerciseId}"
                        else -> "Ejercicio"
                    }
                    prs.add(PersonalRecord(name, desc))
                }
            }
        }
        return prs.distinctBy { it.exerciseName }
    }

    private fun formatDate(iso: String?): String {
        if (iso == null) return ""
        return try {
            LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy · HH:mm"))
        } catch (e: Exception) { iso }
    }

    private fun formatDateShort(iso: String?): String {
        if (iso == null) return ""
        return try {
            LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .format(DateTimeFormatter.ofPattern("d MMM · HH:mm"))
        } catch (e: Exception) { iso }
    }

    private fun formatDuration(seconds: Long?): String {
        if (seconds == null || seconds <= 0) return "0m"
        val h = seconds / 3600; val m = (seconds % 3600) / 60
        return when { h > 0 -> "${h}h ${m}m"; m > 0 -> "${m}m"; else -> "< 1m" }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

data class PersonalRecord(val exerciseName: String, val description: String)

data class SessionComparison(
    val previousSessionDate: String,
    val volumeDifference: Double,
    val durationDifference: Long,
    val setsDifference: Int
)