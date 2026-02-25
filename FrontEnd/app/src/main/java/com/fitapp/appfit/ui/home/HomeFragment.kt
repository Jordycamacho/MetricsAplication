package com.fitapp.appfit.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.R
import com.fitapp.appfit.databinding.FragmentHomeBinding
import com.fitapp.appfit.model.RoutineViewModel
import com.fitapp.appfit.ui.routines.adapter.RoutineAdapter
import com.fitapp.appfit.utils.Resource

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val routineViewModel: RoutineViewModel by viewModels()
    private lateinit var lastUsedRoutineAdapter: RoutineAdapter

    companion object {
        private const val TAG = "HomeFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        loadLastUsedRoutines()
    }

    private fun setupRecyclerView() {
        lastUsedRoutineAdapter = RoutineAdapter(
            onItemClick = { routine -> showRoutineDetail(routine) },
            onEditClick = { routine -> editRoutine(routine) },
            onStartClick = { routine -> startWorkout(routine) }
        )
        binding.recyclerRecentRoutines.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = lastUsedRoutineAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.cardCreateRoutine.setOnClickListener {
            findNavController().navigate(R.id.navigation_create_routine)
        }
        binding.textViewSeeAll.setOnClickListener {
            findNavController().navigate(R.id.navigation_routines)
        }
    }

    private fun setupObservers() {
        routineViewModel.lastUsedRoutinesState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    resource.data?.let { routines ->
                        if (routines.isEmpty()) showEmptyRecentRoutines()
                        else { showRecentRoutinesList(); lastUsedRoutineAdapter.submitList(routines.toList()) }
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    val errorMsg = resource.message ?: "Error al cargar rutinas"
                    if (errorMsg.contains("500")) {
                        Handler(Looper.getMainLooper()).postDelayed({ loadLastUsedRoutines() }, 2000)
                    } else {
                        showEmptyRecentRoutines()
                    }
                }
                is Resource.Loading -> showLoading()
            }
        })

        routineViewModel.anyUpdateEvent.observe(viewLifecycleOwner) {
            Handler(Looper.getMainLooper()).postDelayed({ loadLastUsedRoutines() }, 500)
        }
    }

    private fun loadLastUsedRoutines() {
        routineViewModel.getLastUsedRoutines(3)
    }

    private fun startWorkout(routine: com.fitapp.appfit.response.routine.response.RoutineSummaryResponse) {
        routineViewModel.markRoutineAsUsed(routine.id)
        Toast.makeText(requireContext(), "Iniciando: ${routine.name}", Toast.LENGTH_SHORT).show()
    }

    private fun showRoutineDetail(routine: com.fitapp.appfit.response.routine.response.RoutineSummaryResponse) {
        Toast.makeText(requireContext(), routine.name, Toast.LENGTH_SHORT).show()
    }

    private fun editRoutine(routine: com.fitapp.appfit.response.routine.response.RoutineSummaryResponse) {
        Toast.makeText(requireContext(), "Editar: ${routine.name}", Toast.LENGTH_SHORT).show()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerRecentRoutines.visibility = View.GONE
    }

    private fun hideLoading() { binding.progressBar.visibility = View.GONE }

    private fun showRecentRoutinesList() {
        binding.recyclerRecentRoutines.visibility = View.VISIBLE
        binding.textEmptyRecentRoutines.visibility = View.GONE
    }

    private fun showEmptyRecentRoutines() {
        binding.recyclerRecentRoutines.visibility = View.GONE
        binding.textEmptyRecentRoutines.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        routineViewModel.clearAllUpdateStates()
        Handler(Looper.getMainLooper()).postDelayed({ loadLastUsedRoutines() }, 300)
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}