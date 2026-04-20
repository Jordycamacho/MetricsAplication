package com.fitapp.appfit.feature.routine.ui.list

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
import com.fitapp.appfit.shared.constants.NavigationKeys
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentRoutinesListBinding
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineSummaryResponse
import com.fitapp.appfit.feature.routine.ui.RoutineViewModel
import java.util.concurrent.atomic.AtomicBoolean

class RoutinesFragment : Fragment() {

    private var _binding: FragmentRoutinesListBinding? = null
    private val binding get() = _binding!!

    private val routineViewModel: RoutineViewModel by viewModels()
    private lateinit var routineAdapter: RoutineAdapter

    private val generationLock = AtomicBoolean(false)

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

    private fun setupSearch() {
        binding.etSearchRoutines.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                if (query.isEmpty()) routineViewModel.getRoutines(0, 20)
                else routineViewModel.getRoutinesWithFilters(name = query)
            }
        })
    }

    private fun setupFab() {
        binding.fabCreateRoutine.setOnClickListener {
            findNavController().navigate(R.id.navigation_create_routine)
        }
        binding.btnGenerateGym.setOnClickListener {
            if (generationLock.compareAndSet(false, true)) {
                lockGenerateButtons()
                routineViewModel.generateDefaultRoutine("GYM")
            }
        }
        binding.btnGenerateBoxing.setOnClickListener {
            if (generationLock.compareAndSet(false, true)) {
                lockGenerateButtons()
                routineViewModel.generateDefaultRoutine("BOXING")
            }
        }
    }

    private fun setupObservers() {

        routineViewModel.routinesListState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    if (!generationLock.get()) {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.recyclerRoutines.visibility = View.GONE
                        binding.layoutEmptyState.visibility = View.GONE
                    }
                }
                is Resource.Success -> {
                    generationLock.set(false)
                    val list = resource.data?.content ?: emptyList()
                    if (list.isEmpty()) showEmpty() else showList(list)
                }
                is Resource.Error -> {
                    generationLock.set(false)
                    hideLoading()
                    showEmpty()
                    // Mostrar error mejorado
                    showErrorToast(resource.message ?: "Error al cargar rutinas")
                }
            }
        }

        routineViewModel.filteredRoutinesState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val list = resource.data?.content ?: emptyList()
                    if (list.isEmpty()) showEmpty() else showList(list)
                }
                is Resource.Error -> {
                    hideLoading()
                    showErrorToast(resource.message ?: "Error al filtrar")
                }
                else -> {}
            }
        }

        routineViewModel.generateDefaultState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.layoutEmptyState.visibility = View.GONE
                    binding.btnGenerateGym.visibility = View.GONE
                    binding.btnGenerateBoxing.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "✓ Rutina creada", Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    generationLock.set(false)
                    hideLoading()
                    unlockGenerateButtons()
                    showErrorToast(resource.message ?: "Error al generar rutina")
                }
            }
        }

        routineViewModel.anyUpdateEvent.observe(viewLifecycleOwner) {
            routineViewModel.getRoutines(0, 20)
        }
    }

    private fun setupNavigationResults() {
        val handle = findNavController().currentBackStackEntry?.savedStateHandle
        handle?.getLiveData<Boolean>(NavigationKeys.ROUTINE_UPDATED)?.observe(viewLifecycleOwner) { updated ->
            if (updated == true) {
                routineViewModel.getRoutines(0, 20)
                handle.remove<Boolean>(NavigationKeys.ROUTINE_UPDATED)
            }
        }
        handle?.getLiveData<Boolean>(NavigationKeys.ROUTINE_DELETED)?.observe(viewLifecycleOwner) { deleted ->
            if (deleted == true) {
                routineViewModel.getRoutines(0, 20)
                handle.remove<Boolean>(NavigationKeys.ROUTINE_DELETED)
            }
        }
    }

    private fun editRoutine(routine: RoutineSummaryResponse) {
        findNavController().navigate(
            RoutinesFragmentDirections.actionNavigationRoutinesToNavigationEditRoutine(routine.id)
        )
    }

    private fun startWorkout(routine: RoutineSummaryResponse) {
        routineViewModel.markRoutineAsUsed(routine.id)
        findNavController().navigate(
            RoutinesFragmentDirections.actionNavigationRoutinesToNavigationWorkout(routine.id)
        )
    }

    private fun navigateToExercises(routine: RoutineSummaryResponse) {
        findNavController().navigate(
            RoutinesFragmentDirections.actionNavigationRoutinesToRoutineExercises(routine.id)
        )
    }

    private fun lockGenerateButtons() {
        binding.btnGenerateGym.isEnabled = false
        binding.btnGenerateBoxing.isEnabled = false
        binding.btnGenerateGym.alpha = 0.4f
        binding.btnGenerateBoxing.alpha = 0.4f
        binding.btnGenerateGym.text = "Generando..."
        binding.btnGenerateBoxing.text = "Generando..."
    }

    private fun unlockGenerateButtons() {
        binding.btnGenerateGym.isEnabled = true
        binding.btnGenerateBoxing.isEnabled = true
        binding.btnGenerateGym.alpha = 1f
        binding.btnGenerateBoxing.alpha = 1f
        binding.btnGenerateGym.text = "Generar esta rutina"
        binding.btnGenerateBoxing.text = "Generar esta rutina"
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showList(routines: List<RoutineSummaryResponse>) {
        binding.progressBar.visibility = View.GONE
        binding.recyclerRoutines.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
        binding.btnGenerateGym.visibility = View.GONE
        binding.btnGenerateBoxing.visibility = View.GONE
        routineAdapter.updateRoutines(routines)
    }

    private fun showEmpty() {
        binding.progressBar.visibility = View.GONE
        binding.recyclerRoutines.visibility = View.GONE
        if (!generationLock.get()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            unlockGenerateButtons()
            binding.btnGenerateGym.visibility = View.VISIBLE
            binding.btnGenerateBoxing.visibility = View.VISIBLE
        }
    }

    private fun showErrorToast(message: String) {
        val duration = if (message.contains("\n") || message.length > 50) {
            Toast.LENGTH_LONG
        } else {
            Toast.LENGTH_SHORT
        }
        Toast.makeText(requireContext(), message, duration).show()
    }

    override fun onResume() {
        super.onResume()
        routineViewModel.getRoutines(0, 20)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}