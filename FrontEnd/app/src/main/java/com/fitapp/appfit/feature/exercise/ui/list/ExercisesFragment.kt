package com.fitapp.appfit.feature.exercise.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentExercisesBinding
import com.fitapp.appfit.feature.exercise.model.exercise.request.ExerciseFilterRequest
import com.fitapp.appfit.feature.exercise.model.exercise.response.ExercisePageResponse
import com.fitapp.appfit.feature.exercise.model.exercise.response.ExerciseResponse
import com.fitapp.appfit.feature.exercise.ExerciseViewModel
import com.fitapp.appfit.feature.exercise.ui.list.ExerciseAdapter

class ExercisesFragment : Fragment() {

    private var _binding: FragmentExercisesBinding? = null
    private val binding get() = _binding!!
    private val exerciseViewModel: ExerciseViewModel by viewModels()
    private lateinit var exerciseAdapter: ExerciseAdapter

    private var currentFilter = "available"
    private var currentSportId: Long? = null
    private var currentPage = 0
    private var isLoading = false
    private var hasMorePages = true
    private var isUpdatingChips = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExercisesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupChipListeners()
        setupObservers()
        setupScrollListener()
        binding.fabCreateExercise.setOnClickListener { navigateToCreateExercise() }
        loadExercises(reset = true)
    }

    private fun setupRecyclerView() {
        exerciseAdapter = ExerciseAdapter(
            onItemClick = { exercise -> navigateToExerciseDetail(exercise) },
            isAdminMode = false
        )
        binding.recyclerExercises.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = exerciseAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupChipListeners() {
        binding.chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isUpdatingChips) { currentFilter = "all"; loadExercises(reset = true) }
        }
        binding.chipMy.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isUpdatingChips) { currentFilter = "my"; loadExercises(reset = true) }
        }
        binding.chipAvailable.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isUpdatingChips) { currentFilter = "available"; loadExercises(reset = true) }
        }
        binding.chipBySport.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isUpdatingChips) {
                currentFilter = "bySport"
                currentSportId = null
                loadExercises(reset = true)
            }
        }
    }

    private fun setupScrollListener() {
        binding.recyclerExercises.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lm = recyclerView.layoutManager as LinearLayoutManager
                val visible = lm.childCount
                val total = lm.itemCount
                val firstVisible = lm.findFirstVisibleItemPosition()
                if (!isLoading && hasMorePages && (visible + firstVisible) >= total && firstVisible >= 0 && total >= 20) {
                    loadMoreExercises()
                }
            }
        })
    }

    private fun setupObservers() {
        exerciseViewModel.allExercisesState.observe(viewLifecycleOwner) { resource ->
            resource?.let { handleExercisesResponse(it, "No hay ejercicios disponibles") }
        }
        exerciseViewModel.myExercisesState.observe(viewLifecycleOwner) { resource ->
            resource?.let { handleExercisesResponse(it, "No has creado ejercicios") }
        }
        exerciseViewModel.availableExercisesState.observe(viewLifecycleOwner) { resource ->
            resource?.let { handleExercisesResponse(it, "No hay ejercicios disponibles") }
        }
        exerciseViewModel.exercisesBySportState.observe(viewLifecycleOwner) { resource ->
            resource?.let { handleExercisesResponse(it, "No hay ejercicios para este deporte") }
        }

        exerciseViewModel.deleteExerciseState.observe(viewLifecycleOwner) { resource ->
            resource?.let {
                when (it) {
                    is Resource.Success -> {
                        Toast.makeText(requireContext(), "Ejercicio eliminado", Toast.LENGTH_SHORT).show()
                        loadExercises(reset = true)
                    }
                    is Resource.Error -> Toast.makeText(requireContext(), it.message ?: "Error", Toast.LENGTH_SHORT).show()
                    else -> {}
                }
                exerciseViewModel.clearDeleteState()
            }
        }

        exerciseViewModel.toggleStatusState.observe(viewLifecycleOwner) { resource ->
            resource?.let {
                when (it) {
                    is Resource.Success<Unit> -> {
                        Toast.makeText(requireContext(), "Estado actualizado", Toast.LENGTH_SHORT).show()
                        loadExercises(reset = true)
                    }
                    is Resource.Error -> Toast.makeText(requireContext(), it.message ?: "Error", Toast.LENGTH_SHORT).show()
                    else -> {}
                }
                exerciseViewModel.clearToggleState()
            }
        }
    }

    private fun handleExercisesResponse(resource: Resource<ExercisePageResponse>, emptyMessage: String) {
        isLoading = false
        binding.progressBar.isVisible = false
        binding.loadMoreProgress.isVisible = false

        when (resource) {
            is Resource.Success -> {
                resource.data?.let { pageResponse ->
                    if (currentPage == 0) exerciseAdapter.clearExercises()
                    if (pageResponse.content.isNotEmpty()) {
                        exerciseAdapter.addExercises(pageResponse.content)
                        binding.layoutEmptyState.visibility = View.GONE
                        binding.recyclerExercises.visibility = View.VISIBLE
                        hasMorePages = !pageResponse.last
                        currentPage = pageResponse.pageNumber
                    } else {
                        if (currentPage == 0) showEmptyState(emptyMessage)
                        hasMorePages = false
                    }
                } ?: run { if (currentPage == 0) showEmptyState(emptyMessage) }
            }
            is Resource.Error -> {
                Toast.makeText(requireContext(), resource.message ?: "Error", Toast.LENGTH_SHORT).show()
                if (currentPage == 0) showEmptyState("Error al cargar ejercicios")
            }
            is Resource.Loading -> {
                if (currentPage == 0) {
                    binding.progressBar.isVisible = true
                    binding.layoutEmptyState.visibility = View.GONE
                } else {
                    binding.loadMoreProgress.isVisible = true
                }
                isLoading = true
            }
        }
    }

    private fun loadExercises(reset: Boolean = false) {
        if (reset) {
            currentPage = 0
            hasMorePages = true
            exerciseAdapter.clearExercises()
        }

        val filterRequest = ExerciseFilterRequest().copy(
            page = currentPage,
            size = 20,
            search = binding.etSearch.text?.let { if (it.isNotBlank()) binding.etSearch.text.toString() else null }
        )

        when (currentFilter) {
            "all" -> exerciseViewModel.searchExercises(filterRequest)
            "my" -> exerciseViewModel.searchMyExercises(filterRequest)
            "available" -> exerciseViewModel.searchAvailableExercises(filterRequest)
            "bySport" -> {
                currentSportId?.let { exerciseViewModel.searchExercisesBySport(it, filterRequest) }
                    ?: exerciseViewModel.searchAvailableExercises(filterRequest)
            }
        }
    }

    private fun loadMoreExercises() {
        if (!isLoading && hasMorePages) { currentPage++; loadExercises(reset = false) }
    }

    private fun navigateToExerciseDetail(exercise: ExerciseResponse) {
        try {
            findNavController().navigate(
                ExercisesFragmentDirections.actionNavigationExercisesToExerciseDetail(exercise.id)
            )
        } catch (e: Exception) {
            findNavController().navigate(
                R.id.action_navigation_exercises_to_exercise_detail,
                Bundle().apply { putLong("exerciseId", exercise.id) })
        }
    }

    private fun navigateToCreateExercise() {
        findNavController().navigate(R.id.navigation_create_exercise)
    }

    private fun showEmptyState(message: String) {
        binding.recyclerExercises.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.tvEmptyState.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}