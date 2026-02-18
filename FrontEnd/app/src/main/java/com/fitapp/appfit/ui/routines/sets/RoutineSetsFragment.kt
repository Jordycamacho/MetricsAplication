package com.fitapp.appfit.ui.routines.sets

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
import com.fitapp.appfit.databinding.FragmentRoutineSetsBinding
import com.fitapp.appfit.model.RoutineSetTemplateViewModel
import com.fitapp.appfit.response.routine.response.RoutineSetTemplateResponse
import com.fitapp.appfit.ui.routines.adapter.SetTemplateAdapter
import com.fitapp.appfit.utils.Resource

class RoutineSetsFragment : Fragment() {

    private var _binding: FragmentRoutineSetsBinding? = null
    private val binding get() = _binding!!

    private val args: RoutineSetsFragmentArgs by navArgs()
    private val viewModel: RoutineSetTemplateViewModel by viewModels()

    private lateinit var adapter: SetTemplateAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoutineSetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupFab()

        viewModel.getSetTemplatesByRoutineExercise(args.routineExerciseId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.toolbar.title = "Sets del ejercicio"
    }

    private fun setupRecyclerView() {
        adapter = SetTemplateAdapter(
            onEditClick = { set -> editSet(set) },
            onDeleteClick = { set -> confirmDelete(set) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.getSetTemplatesByExerciseState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    resource.data?.let { sets ->
                        if (sets.isEmpty()) {
                            binding.tvEmpty.visibility = View.VISIBLE
                            binding.recyclerView.visibility = View.GONE
                        } else {
                            binding.tvEmpty.visibility = View.GONE
                            binding.recyclerView.visibility = View.VISIBLE
                            adapter.submitList(sets)
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

        viewModel.deleteSetTemplateState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Set eliminado", Toast.LENGTH_SHORT).show()
                    viewModel.getSetTemplatesByRoutineExercise(args.routineExerciseId)
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
        binding.fabAddSet.setOnClickListener {
            val action = RoutineSetsFragmentDirections.actionRoutineSetsToAddEditSet(args.routineExerciseId, -1L)
            findNavController().navigate(action)
        }
    }

    private fun editSet(set: RoutineSetTemplateResponse) {
        val action = RoutineSetsFragmentDirections.actionRoutineSetsToAddEditSet(args.routineExerciseId, set.id)
        findNavController().navigate(action)
    }

    private fun confirmDelete(set: RoutineSetTemplateResponse) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar set")
            .setMessage("¿Estás seguro de eliminar el set ${set.position}?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteSetTemplate(set.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
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