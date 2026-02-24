package com.fitapp.appfit.ui.workout

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.databinding.FragmentWorkoutBinding
import com.fitapp.appfit.model.RoutineViewModel
import com.fitapp.appfit.utils.Resource

class WorkoutFragment : Fragment() {

    private var _binding: FragmentWorkoutBinding? = null
    private val binding get() = _binding!!

    private val args: WorkoutFragmentArgs by navArgs()
    private val viewModel: RoutineViewModel by viewModels()

    private lateinit var adapter: WorkoutDayAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeData()
        loadRoutine()
    }



    private fun setupRecyclerView() {
        adapter = WorkoutDayAdapter { exercise, set, valueType, newValue ->
            Log.d("Workout", "Ejercicio ${exercise.exerciseName}, Set ${set.position}, $valueType = $newValue")
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun observeData() {
        viewModel.workoutRoutineState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading()
                is Resource.Success -> {
                    hideLoading()
                    resource.data?.let { routine ->
                        adapter.submitRoutine(routine)
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                }
                else -> {}
            }
        }
    }

    private fun loadRoutine() {
        viewModel.getRoutineForTraining(args.routineId)
        viewModel.markRoutineAsUsed(args.routineId)
    }

    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.recyclerView.isVisible = false
    }

    private fun hideLoading() {
        binding.progressBar.isVisible = false
        binding.recyclerView.isVisible = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}