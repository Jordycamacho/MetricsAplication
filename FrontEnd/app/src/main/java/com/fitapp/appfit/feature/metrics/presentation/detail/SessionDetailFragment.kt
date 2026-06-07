package com.fitapp.appfit.feature.metrics.presentation.detail

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
import com.fitapp.appfit.databinding.FragmentMetricsSessionDetailBinding
import com.fitapp.appfit.feature.metrics.domain.model.SessionComparison
import com.fitapp.appfit.feature.metrics.domain.util.DaySessionLabelFormatter
import com.fitapp.appfit.feature.metrics.presentation.widgets.SessionComparisonAdapter
import com.fitapp.appfit.feature.workout.model.response.SessionExerciseResponse
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SessionDetailFragment : Fragment() {

    private var _binding: FragmentMetricsSessionDetailBinding? = null
    private val binding get() = _binding!!

    private val args: SessionDetailFragmentArgs by navArgs()
    private val viewModel: SessionDetailViewModel by viewModels()
    private val exerciseAdapter = SessionComparisonAdapter()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMetricsSessionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.rvExercises.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = exerciseAdapter
        }
        setupObservers()
        viewModel.loadSessionDetails(args.sessionId)
    }

    private fun setupObservers() {
        viewModel.sessionState.observe(viewLifecycleOwner) { resource ->
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
                    resource.data?.let { displaySession(it) }
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    binding.scrollView.isVisible = false
                    binding.tvError.isVisible = true
                    binding.tvError.text = resource.message
                }
            }
        }
        viewModel.comparisonState.observe(viewLifecycleOwner) { comparison ->
            if (comparison != null) displayComparison(comparison)
            else binding.layoutComparison.isVisible = false
        }
        viewModel.previousExercisesState.observe(viewLifecycleOwner) { prev ->
            exerciseAdapter.setPreviousExercises(prev)
        }
    }

    private fun displaySession(session: WorkoutSessionResponse) {
        binding.tvRoutineName.text = session.routineName ?: "Entrenamiento"
        val dayLabel = DaySessionLabelFormatter.shortLabel(
            session.dayOfWeek, session.sessionNumber, session.dayLabel
        )
        binding.tvDaySession.isVisible = dayLabel != "Sesión completa"
        binding.tvDaySession.text = dayLabel
        binding.tvDate.text = formatDate(session.startTime)
        binding.tvDuration.text = formatDuration(session.durationSeconds)
        binding.tvExerciseCount.text = "${session.exercises?.size ?: 0}"
        binding.tvSetCount.text = "${session.exercises?.sumOf { it.sets?.size ?: 0 } ?: 0}"
        binding.tvVolume.text = String.format("%.1f kg", session.totalVolume ?: 0.0)

        session.performanceScore?.let { score ->
            binding.layoutPerformance.isVisible = true
            binding.tvPerformanceScore.text = "$score/10"
            binding.progressPerformance.progress = score * 10
        } ?: run { binding.layoutPerformance.isVisible = false }

        exerciseAdapter.submitList(session.exercises.orEmpty())

        val prs = detectPersonalRecords(session)
        binding.layoutPersonalRecords.isVisible = prs.isNotEmpty()
        if (prs.isNotEmpty()) {
            binding.tvPersonalRecords.text = prs.joinToString("\n") { "• ${it.first}: ${it.second}" }
        }
    }

    private fun displayComparison(comparison: SessionComparison) {
        binding.layoutComparison.isVisible = true
        binding.tvComparisonDate.text = "vs ${formatDateShort(comparison.previousSessionDate)}"
        val vd = comparison.volumeDifference
        binding.tvVolumeComparison.text = when {
            vd > 0 -> "+${String.format("%.1f", vd)} kg"
            vd < 0 -> "${String.format("%.1f", vd)} kg"
            else -> "= 0 kg"
        }
        binding.tvSetsComparison.text = when {
            comparison.setsDifference > 0 -> "+${comparison.setsDifference}"
            comparison.setsDifference < 0 -> "${comparison.setsDifference}"
            else -> "= 0"
        }
    }

    private fun detectPersonalRecords(session: WorkoutSessionResponse): List<Pair<String, String>> {
        val prs = mutableListOf<Pair<String, String>>()
        session.exercises.orEmpty().forEach { exercise ->
            exercise.sets.orEmpty().forEach { set ->
                if (set.parameters?.any { it.isPersonalRecord == true } == true) {
                    val name = exercise.exerciseName ?: "Ejercicio"
                    prs.add(name to "Nuevo récord")
                }
            }
        }
        return prs.distinct()
    }

    private fun formatDate(iso: String?) = try {
        LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy · HH:mm"))
    } catch (_: Exception) { iso ?: "" }

    private fun formatDateShort(iso: String?) = try {
        LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("d MMM · HH:mm"))
    } catch (_: Exception) { iso ?: "" }

    private fun formatDuration(seconds: Long?) = when {
        seconds == null || seconds <= 0 -> "0m"
        seconds >= 3600 -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
        else -> "${seconds / 60}m"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
