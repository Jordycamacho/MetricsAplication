package com.fitapp.appfit.feature.workout.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentWorkoutSessionDetailBinding
import com.fitapp.appfit.feature.workout.ui.adapter.SessionExerciseAdapter
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Muestra el detalle completo de una sesión de workout:
 * - Métricas generales
 * - Ejercicios y sets ejecutados
 * - Comparación con sesión anterior
 * - Records personales batidos
 */
class WorkoutSessionDetailFragment : Fragment() {

    private var _binding: FragmentWorkoutSessionDetailBinding? = null
    private val binding get() = _binding!!

    private val args: WorkoutSessionDetailFragmentArgs by navArgs()
    private val viewModel: WorkoutSessionDetailViewModel by viewModels()
    private lateinit var exerciseAdapter: SessionExerciseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutSessionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupListeners()

        // Cargar detalles de la sesión
        viewModel.loadSessionDetails(args.sessionId)
    }

    private fun setupRecyclerView() {
        exerciseAdapter = SessionExerciseAdapter()

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

                    resource.data?.let { session ->
                        displaySessionDetails(session)
                    }
                }

                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    binding.scrollView.isVisible = false
                    binding.tvError.isVisible = true
                    binding.tvError.text = resource.message ?: "Error al cargar sesión"
                }
            }
        }

        // Observar comparación con sesión anterior
        viewModel.comparisonState.observe(viewLifecycleOwner) { comparison ->
            comparison?.let { displayComparison(it) }
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun displaySessionDetails(session: com.fitapp.appfit.feature.workout.model.response.WorkoutSessionResponse) {
        // Header
        binding.tvRoutineName.text = session.routineName ?: "Entrenamiento"
        binding.tvDate.text = formatDate(session.startTime)

        // Métricas principales
        binding.tvDuration.text = formatDuration(session.durationSeconds)
        binding.tvExerciseCount.text = "${session.exercises?.size ?: 0}"

        val totalSets = session.exercises?.sumOf { it.sets?.size ?: 0 } ?: 0
        binding.tvSetCount.text = "$totalSets"

        binding.tvVolume.text = if (session.totalVolume != null && session.totalVolume > 0) {
            String.format("%.1f kg", session.totalVolume)
        } else {
            "-- kg"
        }

        // Performance score
        session.performanceScore?.let { score ->
            binding.layoutPerformance.isVisible = true
            binding.tvPerformanceScore.text = "$score/10"
            binding.progressPerformance.progress = score * 10
        } ?: run {
            binding.layoutPerformance.isVisible = false
        }

        // Lista de ejercicios
        session.exercises?.let { exercises ->
            exerciseAdapter.submitList(exercises)
        }

        // PRs batidos
        val prs = detectPersonalRecords(session)
        if (prs.isNotEmpty()) {
            binding.layoutPersonalRecords.isVisible = true
            binding.tvPersonalRecords.text = prs.joinToString("\n") {
                "• ${it.exerciseName}: ${it.description}"
            }
        } else {
            binding.layoutPersonalRecords.isVisible = false
        }
    }

    private fun displayComparison(comparison: SessionComparison) {
        binding.layoutComparison.isVisible = true

        binding.tvComparisonDate.text = "vs ${formatDate(comparison.previousSessionDate)}"

        // Volumen
        val volumeDiff = comparison.volumeDifference
        binding.tvVolumeComparison.text = when {
            volumeDiff > 0 -> "+${String.format("%.1f", volumeDiff)} kg"
            volumeDiff < 0 -> String.format("%.1f", volumeDiff) + " kg"
            else -> "="
        }
        binding.tvVolumeComparison.setTextColor(
            requireContext().getColor(
                when {
                    volumeDiff > 0 -> com.fitapp.appfit.R.color.set_completed_green
                    volumeDiff < 0 -> com.fitapp.appfit.R.color.red
                    else -> com.fitapp.appfit.R.color.text_secondary_dark
                }
            )
        )

        // Duración
        val durationDiff = comparison.durationDifference
        binding.tvDurationComparison.text = when {
            durationDiff > 0 -> "+${formatDuration(durationDiff)}"
            durationDiff < 0 -> "-${formatDuration(-durationDiff)}"
            else -> "="
        }

        // Sets
        val setsDiff = comparison.setsDifference
        binding.tvSetsComparison.text = when {
            setsDiff > 0 -> "+$setsDiff"
            setsDiff < 0 -> "$setsDiff"
            else -> "="
        }
    }

    private fun detectPersonalRecords(
        session: com.fitapp.appfit.feature.workout.model.response.WorkoutSessionResponse
    ): List<PersonalRecord> {
        val prs = mutableListOf<PersonalRecord>()

        session.exercises?.forEach { exercise ->
            exercise.sets?.forEach { set ->
                set.parameters?.forEach { param ->
                    if (param.isPersonalRecord == true) {
                        val weight = param.numericValue
                        val reps = set.parameters?.find { it.parameterType?.uppercase() == "REPETITIONS" }?.integerValue

                        prs.add(
                            PersonalRecord(
                                exerciseName = exercise.exerciseName ?: "Ejercicio",
                                description = if (weight != null && reps != null) {
                                    "${String.format("%.1f", weight)}kg × $reps reps"
                                } else {
                                    "Nuevo récord"
                                }
                            )
                        )
                    }
                }
            }
        }

        return prs.distinctBy { it.exerciseName }
    }

    private fun formatDate(isoDate: String): String {
        return try {
            val dateTime = LocalDateTime.parse(isoDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            dateTime.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy - HH:mm"))
        } catch (e: Exception) {
            isoDate
        }
    }

    private fun formatDuration(seconds: Long?): String {
        if (seconds == null || seconds <= 0) return "--"

        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }

    private fun showDeleteConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar sesión")
            .setMessage("¿Estás seguro de eliminar esta sesión? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteSession(args.sessionId)
                findNavController().navigateUp()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class PersonalRecord(
    val exerciseName: String,
    val description: String
)

data class SessionComparison(
    val previousSessionDate: String,
    val volumeDifference: Double,
    val durationDifference: Long,
    val setsDifference: Int
)