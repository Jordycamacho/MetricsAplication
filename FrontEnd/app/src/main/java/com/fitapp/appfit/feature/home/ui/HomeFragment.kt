package com.fitapp.appfit.feature.home.ui

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.R
import com.fitapp.appfit.databinding.FragmentHomeBinding
import com.fitapp.appfit.feature.home.domain.DayActivity
import com.fitapp.appfit.feature.home.domain.WeeklyStatsHelper
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineSummaryResponse
import com.fitapp.appfit.feature.routine.ui.list.RoutineAdapter
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionSummaryResponse
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(requireActivity().application)
    }

    private lateinit var routineAdapter: RoutineAdapter
    private var heroRoutine: RoutineSummaryResponse? = null
    private var plannedTodayRoutine: RoutineSummaryResponse? = null
    private var lastSessionId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHeader()
        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        homeViewModel.loadHomeData()
    }

    private fun setupHeader() {
        updateGreeting(null)
        val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
        binding.tvDate.text = dateFormat.format(Date()).replaceFirstChar { it.uppercase() }
    }

    private fun updateGreeting(firstName: String?) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeGreeting = when {
            hour < 12 -> "Buenos días"
            hour < 19 -> "Buenas tardes"
            else -> "Buenas noches"
        }
        binding.tvGreeting.text = if (firstName.isNullOrBlank()) timeGreeting else "$timeGreeting, $firstName"
    }

    private fun setupRecyclerView() {
        routineAdapter = RoutineAdapter(
            onItemClick = { routine -> navigateToRoutineDetail(routine) },
            onEditClick = { routine -> navigateToEditRoutine(routine) },
            onStartClick = { routine -> startWorkout(routine) },
            onAddExercisesClick = { routine ->
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeToRoutineExercises(routine.id)
                )
            }
        )
        binding.recyclerRecentRoutines.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = routineAdapter
            setHasFixedSize(false)
        }
    }

    private fun setupClickListeners() {
        binding.fabCreateRoutine.setOnClickListener {
            findNavController().navigate(R.id.navigation_create_routine)
        }
        binding.chipShortcutCreate.setOnClickListener {
            findNavController().navigate(R.id.navigation_create_routine)
        }
        binding.chipShortcutRoutines.setOnClickListener {
            findNavController().navigate(R.id.navigation_routines)
        }
        binding.chipShortcutHistory.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeToWorkoutHistory())
        }
        binding.tvSeeAll.setOnClickListener {
            findNavController().navigate(R.id.navigation_routines)
        }
        binding.cardHeroRoutine.setOnClickListener {
            heroRoutine?.let { startWorkout(it) }
        }
        binding.btnContinueWorkout.setOnClickListener {
            val routineId = homeViewModel.uiState.value?.activeWorkout?.routineId ?: return@setOnClickListener
            findNavController().navigate(HomeFragmentDirections.actionHomeToWorkout(routineId))
        }
        binding.cardStreak.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeToWorkoutHistory())
        }
        binding.btnPlannedStart.setOnClickListener {
            plannedTodayRoutine?.let { startWorkout(it) }
        }
        binding.cardPlannedToday.setOnClickListener {
            plannedTodayRoutine?.let { startWorkout(it) }
        }
        binding.cardLastSession.setOnClickListener {
            val sessionId = lastSessionId ?: return@setOnClickListener
            findNavController().navigate(HomeFragmentDirections.actionHomeToWorkoutDetail(sessionId))
        }
    }

    private fun setupObservers() {
        homeViewModel.uiState.observe(viewLifecycleOwner) { state ->
            updateGreeting(state.userFirstName)
            updateWeeklyStats(state.weeklySessions, state.weeklyVolumeKg, state.streakWeeks)
            updateActivityDots(state.activityDots)
            updateActiveWorkoutBanner(state.activeWorkout)
            updateHeroRoutine(state.heroRoutine, state.plannedTodayRoutine)
            updateLastSession(state.lastSession)
            updateOtherRoutines(state.otherRoutines, state.isLoadingRoutines)
        }
    }

    private fun updateWeeklyStats(sessions: Int?, volumeKg: Double?, streakWeeks: Int?) {
        binding.tvStatSessions.text = sessions?.toString() ?: "—"
        binding.tvStatVolume.text = when {
            volumeKg == null -> "—"
            volumeKg >= 1000 -> String.format(Locale.getDefault(), "%.1fk", volumeKg / 1000)
            volumeKg > 0 -> String.format(Locale.getDefault(), "%.0f", volumeKg)
            else -> "0"
        }
        binding.tvStatStreak.text = streakWeeks?.toString() ?: "—"
    }

    private fun updateActivityDots(dots: List<DayActivity>) {
        binding.layoutWeekActivity.removeAllViews()
        if (dots.isEmpty()) return

        dots.forEach { day ->
            val column = LinearLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
            }

            val label = TextView(requireContext()).apply {
                text = day.dayLabel
                textSize = 11f
                setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (day.isToday) R.color.gold_primary else R.color.text_secondary_dark
                    )
                )
                if (day.isToday) setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
            }

            val dotSize = resources.getDimensionPixelSize(R.dimen.home_activity_dot_size)
            val dot = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(dotSize, dotSize).apply {
                    topMargin = resources.getDimensionPixelSize(R.dimen.home_activity_dot_margin)
                }
                background = ContextCompat.getDrawable(
                    requireContext(),
                    if (day.hasWorkout) R.drawable.bg_home_activity_dot_active
                    else R.drawable.bg_home_activity_dot_inactive
                )
            }

            column.addView(label)
            column.addView(dot)
            binding.layoutWeekActivity.addView(column)
        }
    }

    private fun updateActiveWorkoutBanner(activeWorkout: ActiveWorkoutBanner?) {
        if (activeWorkout == null) {
            binding.cardContinueWorkout.isVisible = false
            return
        }
        val elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(
            System.currentTimeMillis() - activeWorkout.startedAt
        ).coerceAtLeast(1)
        binding.tvContinueWorkoutHint.text = "Llevas $elapsedMinutes min · toca para retomar"
        binding.cardContinueWorkout.isVisible = true
    }

    private fun updateHeroRoutine(
        routine: RoutineSummaryResponse?,
        plannedAlternative: RoutineSummaryResponse?
    ) {
        heroRoutine = routine
        plannedTodayRoutine = plannedAlternative

        if (routine == null) {
            binding.cardHeroRoutine.isVisible = false
            binding.cardPlannedToday.isVisible = false
            return
        }

        binding.cardHeroRoutine.isVisible = true
        binding.tvHeroRoutineName.text = routine.name
        binding.tvHeroPlannedBadge.isVisible =
            WeeklyStatsHelper.isPlannedForToday(routine, LocalDate.now())

        val metaParts = buildList {
            routine.sportName?.takeIf { it.isNotBlank() }?.let { add(it) }
            WeeklyStatsHelper.formatLastUsed(routine.lastUsedAt)?.let { add(it) }
            add("${routine.exerciseCount} ejercicios")
        }
        binding.tvHeroSubtitle.text = metaParts.joinToString("  ·  ")

        if (plannedAlternative != null) {
            binding.cardPlannedToday.isVisible = true
            binding.tvPlannedToday.text = "También toca: ${plannedAlternative.name}"
        } else {
            binding.cardPlannedToday.isVisible = false
        }
    }

    private fun updateLastSession(session: WorkoutSessionSummaryResponse?) {
        if (session == null) {
            binding.cardLastSession.isVisible = false
            lastSessionId = null
            return
        }
        lastSessionId = session.id
        binding.tvLastSessionSummary.text = WeeklyStatsHelper.formatLastSessionSummary(session)
        binding.cardLastSession.isVisible = true
    }

    private fun updateOtherRoutines(routines: List<RoutineSummaryResponse>, isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading

        if (isLoading) {
            binding.layoutEmptyRoutines.isVisible = false
            binding.recyclerRecentRoutines.isVisible = false
            binding.layoutOtherRoutinesHeader.isVisible = false
            return
        }

        if (heroRoutine == null) {
            binding.layoutEmptyRoutines.isVisible = true
            binding.recyclerRecentRoutines.isVisible = false
            binding.layoutOtherRoutinesHeader.isVisible = false
            binding.cardHeroRoutine.isVisible = false
            return
        }

        binding.layoutEmptyRoutines.isVisible = false

        if (routines.isEmpty()) {
            binding.layoutOtherRoutinesHeader.isVisible = false
            binding.recyclerRecentRoutines.isVisible = false
        } else {
            binding.layoutOtherRoutinesHeader.isVisible = true
            binding.recyclerRecentRoutines.isVisible = true
            routineAdapter.submitList(routines)
        }
    }

    private fun startWorkout(routine: RoutineSummaryResponse) {
        homeViewModel.markRoutineAsUsed(routine.id)
        findNavController().navigate(HomeFragmentDirections.actionHomeToWorkout(routine.id))
    }

    private fun navigateToRoutineDetail(routine: RoutineSummaryResponse) {
        findNavController().navigate(HomeFragmentDirections.actionHomeToRoutineExercises(routine.id))
    }

    private fun navigateToEditRoutine(routine: RoutineSummaryResponse) {
        findNavController().navigate(HomeFragmentDirections.actionHomeToEditRoutine(routine.id))
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.refreshActiveWorkoutBanner()
        if (homeViewModel.uiState.value?.isLoadingRoutines == true) {
            homeViewModel.loadHomeData()
        } else {
            homeViewModel.refreshHomeAfterWorkout()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
