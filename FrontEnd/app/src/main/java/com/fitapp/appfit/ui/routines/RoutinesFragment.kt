package com.fitapp.appfit.ui.routines

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.R
import com.fitapp.appfit.constants.NavigationKeys
import com.fitapp.appfit.databinding.FragmentRoutinesListBinding
import com.fitapp.appfit.model.RoutineViewModel
import com.fitapp.appfit.response.routine.response.RoutineSummaryResponse
import com.fitapp.appfit.ui.routines.adapter.RoutineAdapter
import com.fitapp.appfit.utils.Resource

class RoutinesFragment : Fragment() {

    private var _binding: FragmentRoutinesListBinding? = null
    private val binding get() = _binding!!

    private val routineViewModel: RoutineViewModel by viewModels()
    private lateinit var routineAdapter: RoutineAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoutinesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupFab()
        setupObservers()
        setupNavigationResults()
    }

    // ── RecyclerView ──────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        routineAdapter = RoutineAdapter(
            onItemClick = { navigateToExercises(it) },
            onEditClick = { editRoutine(it) },
            onStartClick = { startWorkout(it) },
            onAddExercisesClick = { navigateToExercises(it) }
        )
        binding.recyclerRoutines.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = routineAdapter
            setHasFixedSize(true)
        }
    }

    // ── Búsqueda ──────────────────────────────────────────────────────────────

    private fun setupSearch() {
        binding.etSearchRoutines.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                if (query.isEmpty()) {
                    loadRoutines()
                } else {
                    routineViewModel.getRoutinesWithFilters(name = query)
                }
            }
        })
    }

    // ── FAB ───────────────────────────────────────────────────────────────────

    private fun setupFab() {
        binding.fabCreateRoutine.setOnClickListener {
            findNavController().navigate(R.id.navigation_create_routine)
        }
    }

    // ── Observadores ─────────────────────────────────────────────────────────

    private fun setupObservers() {
        routineViewModel.routinesListState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading()
                is Resource.Success -> {
                    hideLoading()
                    val list = resource.data?.content ?: emptyList()
                    if (list.isEmpty()) showEmpty() else showList(list)
                }
                is Resource.Error -> {
                    hideLoading()
                    showEmpty()
                    Toast.makeText(requireContext(), resource.message ?: "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }

        routineViewModel.filteredRoutinesState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                val list = resource.data?.content ?: emptyList()
                if (list.isEmpty()) showEmpty() else showList(list)
            }
        }

        // Recarga automática tras cualquier mutación (crear, editar, eliminar, toggle)
        routineViewModel.anyUpdateEvent.observe(viewLifecycleOwner) {
            loadRoutines()
        }
    }

    // ── Resultados de navegación (desde EditRoutine vía SavedStateHandle) ─────

    private fun setupNavigationResults() {
        val handle = findNavController().currentBackStackEntry?.savedStateHandle

        handle?.getLiveData<Boolean>(NavigationKeys.ROUTINE_UPDATED)?.observe(viewLifecycleOwner) { updated ->
            if (updated == true) {
                loadRoutines()
                handle.remove<Boolean>(NavigationKeys.ROUTINE_UPDATED)
            }
        }

        handle?.getLiveData<Boolean>(NavigationKeys.ROUTINE_DELETED)?.observe(viewLifecycleOwner) { deleted ->
            if (deleted == true) {
                loadRoutines()
                handle.remove<Boolean>(NavigationKeys.ROUTINE_DELETED)
            }
        }
    }

    // ── Navegación ────────────────────────────────────────────────────────────

    private fun editRoutine(routine: RoutineSummaryResponse) {
        val action = RoutinesFragmentDirections.actionNavigationRoutinesToNavigationEditRoutine(routine.id)
        findNavController().navigate(action)
    }

    private fun startWorkout(routine: RoutineSummaryResponse) {
        routineViewModel.markRoutineAsUsed(routine.id)
        val action = RoutinesFragmentDirections.actionNavigationRoutinesToNavigationWorkout(routine.id)
        findNavController().navigate(action)
    }

    private fun navigateToExercises(routine: RoutineSummaryResponse) {
        val action = RoutinesFragmentDirections.actionNavigationRoutinesToRoutineExercises(routine.id)
        findNavController().navigate(action)
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private fun loadRoutines() { routineViewModel.getRoutines(page = 0, size = 20) }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerRoutines.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.GONE
    }

    private fun hideLoading() { binding.progressBar.visibility = View.GONE }

    private fun showList(routines: List<RoutineSummaryResponse>) {
        binding.recyclerRoutines.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
        routineAdapter.updateRoutines(routines)
    }

    private fun showEmpty() {
        binding.recyclerRoutines.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        loadRoutines()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}