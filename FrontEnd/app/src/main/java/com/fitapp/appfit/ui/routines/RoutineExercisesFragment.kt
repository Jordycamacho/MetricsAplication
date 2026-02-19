package com.fitapp.appfit.ui.routines

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.databinding.FragmentRoutineExercisesBinding
import com.fitapp.appfit.model.RoutineExerciseViewModel
import com.fitapp.appfit.response.routine.response.RoutineExerciseResponse
import com.fitapp.appfit.ui.routines.adapter.RoutineExerciseAdapter
import com.fitapp.appfit.utils.Resource

class RoutineExercisesFragment : Fragment() {

    private var _binding: FragmentRoutineExercisesBinding? = null
    private val binding get() = _binding!!

    private val args: RoutineExercisesFragmentArgs by navArgs()
    private val viewModel: RoutineExerciseViewModel by viewModels()

    private lateinit var adapter: RoutineExerciseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoutineExercisesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupFab()

        viewModel.loadRoutineExercises(args.routineId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.toolbar.title = "Ejercicios de la rutina"
    }

    private fun setupRecyclerView() {
        adapter = RoutineExerciseAdapter(
            onEditClick = { exercise -> editExercise(exercise) },
            onDeleteClick = { exercise -> confirmDelete(exercise) },
            onAddSetClick = { exercise -> navigateToConfigureSets(exercise) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.exercisesState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    resource.data?.let {
                        if (it.isEmpty()) {
                            binding.tvEmpty.visibility = View.VISIBLE
                            binding.recyclerView.visibility = View.GONE
                        } else {
                            binding.tvEmpty.visibility = View.GONE
                            binding.recyclerView.visibility = View.VISIBLE
                            adapter.submitList(it)
                        }
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> showLoading()
                else -> {}
            }
        })

        viewModel.deleteState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Ejercicio eliminado", Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Error al eliminar: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
            viewModel.clearDeleteState()
        })
    }

    private fun setupFab() {
        binding.fabAddExercise.setOnClickListener {
            // Navegar a la pantalla para agregar nuevo ejercicio a esta rutina
            val action = RoutineExercisesFragmentDirections
                .actionRoutineExercisesToAddExercises(args.routineId)
            findNavController().navigate(action)
        }
    }

    private fun editExercise(exercise: RoutineExerciseResponse) {
        // Aquí deberías navegar a AddExercisesToRoutineFragment en modo edición,
        // pasando el exerciseId y quizás los datos actuales.
        // Asumiendo que existe una acción con argumento exerciseId y routineId
        val action = RoutineExercisesFragmentDirections
            .actionRoutineExercisesToEditExercise(args.routineId, exercise.id)
        findNavController().navigate(action)
    }

    private fun confirmDelete(exercise: RoutineExerciseResponse) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar ejercicio")
            .setMessage("¿Estás seguro de eliminar ${exercise.exerciseName} de la rutina?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteExercise(args.routineId, exercise.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun navigateToConfigureSets(exercise: RoutineExerciseResponse) {
        val action = RoutineExercisesFragmentDirections.actionRoutineExercisesToRoutineSets(
            routineExerciseId = exercise.id,
            exerciseId = exercise.exerciseId
        )
        findNavController().navigate(action)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}