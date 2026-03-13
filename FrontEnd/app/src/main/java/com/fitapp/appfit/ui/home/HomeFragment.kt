package com.fitapp.appfit.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.R
import com.fitapp.appfit.databinding.FragmentHomeBinding
import com.fitapp.appfit.model.RoutineViewModel
import com.fitapp.appfit.response.routine.response.RoutineStatisticsResponse
import com.fitapp.appfit.response.routine.response.RoutineSummaryResponse
import com.fitapp.appfit.ui.routines.adapter.RoutineAdapter
import com.fitapp.appfit.utils.Resource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val routineViewModel: RoutineViewModel by viewModels()
    private lateinit var routineAdapter: RoutineAdapter

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
        loadData()
    }

    private fun setupHeader() {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when {
            hour < 12 -> "Buenos días"
            hour < 19 -> "Buenas tardes"
            else -> "Buenas noches"
        }

        val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
        val dateStr = dateFormat.format(Date())
        binding.tvDate.text = dateStr.replaceFirstChar { it.uppercase() }
    }

    private fun setupRecyclerView() {
        routineAdapter = RoutineAdapter(
            onItemClick = { routine -> navigateToRoutineDetail(routine) },
            onEditClick = { routine -> navigateToEditRoutine(routine) },
            onStartClick = { routine -> startWorkout(routine) },
            onAddExercisesClick = { routine ->
                val action = HomeFragmentDirections.actionHomeToRoutineExercises(routine.id)
                findNavController().navigate(action)
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

        binding.tvSeeAll.setOnClickListener {
            findNavController().navigate(R.id.navigation_routines)
        }
    }

    private fun setupObservers() {
        routineViewModel.routineStatisticsState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> resource.data?.let { updateStats(it) }
                is Resource.Error -> showStatsError()
                else -> {}
            }
        }

        routineViewModel.lastUsedRoutinesState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> showRoutinesLoading()
                is Resource.Success -> {
                    hideRoutinesLoading()
                    val routines = resource.data ?: emptyList()
                    if (routines.isEmpty()) showEmptyRoutines()
                    else showRoutines(routines)
                }
                is Resource.Error -> {
                    hideRoutinesLoading()
                    showEmptyRoutines()
                }
                else -> {}
            }
        }

        routineViewModel.anyUpdateEvent.observe(viewLifecycleOwner) {
            Handler(Looper.getMainLooper()).postDelayed({ loadData() }, 400)
        }
    }

    private fun loadData() {
        routineViewModel.getRoutineStatistics()
        routineViewModel.getLastUsedRoutines(3)
    }

    // ==================== Stats ====================

    private fun updateStats(stats: RoutineStatisticsResponse) {
        binding.tvStatTotal.text = stats.totalRoutines.toString()
        binding.tvStatActive.text = stats.activeRoutines.toString()
        binding.tvStatInactive.text = stats.inactiveRoutines.toString()
    }

    private fun showStatsError() {
        binding.tvStatTotal.text = "—"
        binding.tvStatActive.text = "—"
        binding.tvStatInactive.text = "—"
    }

    // ==================== Rutinas ====================

    private fun showRoutinesLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerRecentRoutines.visibility = View.GONE
        binding.layoutEmptyRoutines.visibility = View.GONE
    }

    private fun hideRoutinesLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showRoutines(routines: List<RoutineSummaryResponse>) {
        binding.recyclerRecentRoutines.visibility = View.VISIBLE
        binding.layoutEmptyRoutines.visibility = View.GONE
        routineAdapter.submitList(routines)
    }

    private fun showEmptyRoutines() {
        binding.recyclerRecentRoutines.visibility = View.GONE
        binding.layoutEmptyRoutines.visibility = View.VISIBLE
    }

    // ==================== Navegación ====================

    private fun startWorkout(routine: RoutineSummaryResponse) {
        routineViewModel.markRoutineAsUsed(routine.id)
        val action = HomeFragmentDirections.actionHomeToWorkout(routine.id)
        findNavController().navigate(action)
    }

    private fun navigateToRoutineDetail(routine: RoutineSummaryResponse) {
        val action = HomeFragmentDirections.actionHomeToRoutineExercises(routine.id)
        findNavController().navigate(action)
    }

    private fun navigateToEditRoutine(routine: RoutineSummaryResponse) {
        val action = HomeFragmentDirections.actionHomeToEditRoutine(routine.id)
        findNavController().navigate(action)
    }

    // ==================== Lifecycle ====================

    override fun onResume() {
        super.onResume()
        routineViewModel.clearAllUpdateStates()
        loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}