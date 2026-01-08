package com.fitapp.appfit.ui.exercises

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.R
import com.fitapp.appfit.databinding.FragmentExercisesBinding
import com.fitapp.appfit.model.ExerciseViewModel
import com.fitapp.appfit.response.exercise.request.ExerciseFilterRequest
import com.fitapp.appfit.response.exercise.response.ExercisePageResponse
import com.fitapp.appfit.response.exercise.response.ExerciseResponse
import com.fitapp.appfit.ui.exercises.adapter.ExerciseAdapter
import com.fitapp.appfit.utils.Resource

class ExercisesFragment : Fragment() {

    private var _binding: FragmentExercisesBinding? = null
    private val binding get() = _binding!!
    private val exerciseViewModel: ExerciseViewModel by viewModels()
    private lateinit var exerciseAdapter: ExerciseAdapter

    companion object {
        private const val TAG = "ExercisesFragment"
    }

    // Filtro actual
    private var currentFilter = "available" // "all", "my", "available", "bySport"
    private var currentSportId: Long? = null
    private var currentPage = 0
    private var isLoading = false
    private var hasMorePages = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i(TAG, "onCreateView: Creando vista de ejercicios")
        _binding = FragmentExercisesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "onViewCreated: Configurando vista")

        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        setupScrollListener()

        // Cargar ejercicios iniciales (disponibles por defecto)
        loadExercises(reset = true)
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: Configurando RecyclerView")

        exerciseAdapter = ExerciseAdapter(
            onItemClick = { exercise ->
                Log.d(TAG, "setupRecyclerView: Click en ejercicio ${exercise.id}")
                navigateToExerciseDetail(exercise)
            },
            onEditClick = { exercise ->
                Log.d(TAG, "setupRecyclerView: Editar ejercicio ${exercise.id}")
                navigateToEditExercise(exercise)
            },
            onDeleteClick = { exercise ->
                Log.d(TAG, "setupRecyclerView: Eliminar ejercicio ${exercise.id}")
                showDeleteConfirmation(exercise)
            },
            onToggleStatusClick = { exercise ->
                Log.d(TAG, "setupRecyclerView: Cambiar estado ejercicio ${exercise.id}")
                toggleExerciseStatus(exercise)
            },
            isAdminMode = false
        )

        binding.recyclerExercises.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = exerciseAdapter
            setHasFixedSize(true)
        }
    }

    private fun navigateToExerciseDetail(exercise: ExerciseResponse) {
        try {
            val action = ExercisesFragmentDirections.actionNavigationExercisesToExerciseDetail(
                exerciseId = exercise.id
            )
            findNavController().navigate(action)
        } catch (e: Exception) {
            Log.e(TAG, "Error usando Safe Args: ${e.message}")
            val bundle = Bundle().apply {
                putLong("exerciseId", exercise.id)
            }
            findNavController().navigate(
                R.id.action_navigation_exercises_to_exercise_detail,
                bundle
            )
        }
    }

    private fun navigateToEditExercise(exercise: ExerciseResponse) {
        try {
            val action = ExercisesFragmentDirections.actionNavigationExercisesToEditExercise(
                exerciseId = exercise.id
            )
            findNavController().navigate(action)
        } catch (e: Exception) {
            Log.e(TAG, "Error usando Safe Args: ${e.message}")
            val bundle = Bundle().apply {
                putLong("exerciseId", exercise.id)
            }
            findNavController().navigate(
                R.id.action_navigation_exercises_to_edit_exercise,
                bundle
            )
        }
    }

    private fun setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Configurando listeners")

        binding.chipAvailable.isChecked = true

        // Botón flotante para crear ejercicio
        binding.fabCreateExercise.setOnClickListener {
            Log.d(TAG, "setupClickListeners: Click en crear ejercicio")
            navigateToCreateExercise()
        }

        // Filtros
        binding.chipAll.setOnClickListener {
            Log.d(TAG, "setupClickListeners: Filtro - Todos")
            currentFilter = "all"
            updateChipSelection()
            loadExercises(reset = true)
        }

        binding.chipMy.setOnClickListener {
            Log.d(TAG, "setupClickListeners: Filtro - Mis ejercicios")
            currentFilter = "my"
            updateChipSelection()
            loadExercises(reset = true)
        }

        binding.chipAvailable.setOnClickListener {
            Log.d(TAG, "setupClickListeners: Filtro - Disponibles")
            currentFilter = "available"
            updateChipSelection()
            loadExercises(reset = true)
        }

        binding.chipBySport.setOnClickListener {
            Log.d(TAG, "setupClickListeners: Filtro - Por deporte")
            currentFilter = "bySport"
            updateChipSelection()
            currentSportId = null
            loadExercises(reset = true)
        }
    }

    private fun updateChipSelection() {
        Log.d(TAG, "updateChipSelection: Actualizando selección de chips")
        binding.chipAll.isChecked = (currentFilter == "all")
        binding.chipMy.isChecked = (currentFilter == "my")
        binding.chipAvailable.isChecked = (currentFilter == "available")
        binding.chipBySport.isChecked = (currentFilter == "bySport")
    }

    private fun setupScrollListener() {
        Log.d(TAG, "setupScrollListener: Configurando scroll infinito")

        binding.recyclerExercises.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && hasMorePages) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= 20) {
                        loadMoreExercises()
                    }
                }
            }
        })
    }

    private fun setupObservers() {
        Log.d(TAG, "setupObservers: Configurando observadores")

        // Observar todos los ejercicios
        exerciseViewModel.allExercisesState.observe(viewLifecycleOwner, Observer { resource ->
            resource?.let {
                handleExercisesResponse(it, "No hay ejercicios disponibles")
            }
        })

        // Observar mis ejercicios
        exerciseViewModel.myExercisesState.observe(viewLifecycleOwner, Observer { resource ->
            resource?.let {
                handleExercisesResponse(it, "No has creado ejercicios")
            }
        })

        // Observar ejercicios disponibles
        exerciseViewModel.availableExercisesState.observe(viewLifecycleOwner, Observer { resource ->
            resource?.let {
                handleExercisesResponse(it, "No hay ejercicios disponibles")
            }
        })

        // Observar ejercicios por deporte
        exerciseViewModel.exercisesBySportState.observe(viewLifecycleOwner, Observer { resource ->
            resource?.let {
                handleExercisesResponse(it, "No hay ejercicios para este deporte")
            }
        })

        // Observar estado de eliminación
        exerciseViewModel.deleteExerciseState.observe(viewLifecycleOwner, Observer { resource ->
            resource?.let {
                when (it) {
                    is Resource.Success -> {
                        Log.i(TAG, "setupObservers: Ejercicio eliminado exitosamente")
                        Toast.makeText(requireContext(), "✅ Ejercicio eliminado", Toast.LENGTH_SHORT).show()
                        loadExercises(reset = true)
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "setupObservers: Error eliminando ejercicio: ${it.message}")
                        Toast.makeText(requireContext(), "❌ Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
                exerciseViewModel.clearDeleteState()
            }
        })

        // Observar estado de cambio de estado
        exerciseViewModel.toggleExerciseState.observe(viewLifecycleOwner, Observer { resource ->
            resource?.let {
                when (it) {
                    is Resource.Success -> {
                        Log.i(TAG, "setupObservers: Estado cambiado exitosamente")
                        Toast.makeText(requireContext(), "✅ Estado cambiado", Toast.LENGTH_SHORT).show()
                        loadExercises(reset = true)
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "setupObservers: Error cambiando estado: ${it.message}")
                        Toast.makeText(requireContext(), "❌ Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
                exerciseViewModel.clearToggleState()
            }
        })
    }

    private fun handleExercisesResponse(resource: Resource<ExercisePageResponse>, emptyMessage: String) {
        Log.d(TAG, "handleExercisesResponse: Procesando respuesta - $resource")

        isLoading = false
        binding.progressBar.isVisible = false
        binding.loadMoreProgress.isVisible = false

        when (resource) {
            is Resource.Success -> {
                resource.data?.let { pageResponse ->
                    Log.i(TAG, "handleExercisesResponse: Éxito - ${pageResponse.numberOfElements} ejercicios")

                    if (currentPage == 0) {
                        exerciseAdapter.clearExercises()
                    }

                    if (pageResponse.content.isNotEmpty()) {
                        exerciseAdapter.addExercises(pageResponse.content)
                        binding.layoutEmptyState.visibility = View.GONE
                        binding.recyclerExercises.visibility = View.VISIBLE

                        hasMorePages = !pageResponse.last
                        currentPage = pageResponse.pageNumber

                        Log.d(TAG, "handleExercisesResponse: Página $currentPage, total ${pageResponse.totalElements}")
                    } else {
                        if (currentPage == 0) {
                            showEmptyState(emptyMessage)
                        }
                        hasMorePages = false
                    }
                } ?: run {
                    Log.w(TAG, "handleExercisesResponse: Respuesta vacía")
                    if (currentPage == 0) {
                        showEmptyState(emptyMessage)
                    }
                }
            }
            is Resource.Error -> {
                Log.e(TAG, "handleExercisesResponse: Error - ${resource.message}")
                Toast.makeText(requireContext(), "❌ Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                if (currentPage == 0) {
                    showEmptyState("Error al cargar ejercicios")
                }
            }
            is Resource.Loading -> {
                Log.d(TAG, "handleExercisesResponse: Cargando...")
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
        Log.i(TAG, "loadExercises: Cargando ejercicios (reset: $reset)")

        if (reset) {
            currentPage = 0
            hasMorePages = true
            exerciseAdapter.clearExercises()
        }

        // AQUÍ ESTÁ LA SOLUCIÓN: usa copy() en lugar de apply()
        val filterRequest = ExerciseFilterRequest().copy(
            page = currentPage,
            size = 20,
            search = if (binding.etSearch.text.isNotBlank()) binding.etSearch.text.toString() else null
        )

        when (currentFilter) {
            "all" -> exerciseViewModel.searchExercises(filterRequest)
            "my" -> exerciseViewModel.searchMyExercises(filterRequest)
            "available" -> exerciseViewModel.searchAvailableExercises(filterRequest)
            "bySport" -> {
                currentSportId?.let { sportId ->
                    exerciseViewModel.searchExercisesBySport(sportId, filterRequest)
                } ?: run {
                    exerciseViewModel.searchAvailableExercises(filterRequest)
                }
            }
        }
    }

    private fun loadMoreExercises() {
        if (!isLoading && hasMorePages) {
            Log.d(TAG, "loadMoreExercises: Cargando más ejercicios, página ${currentPage + 1}")
            currentPage++
            loadExercises(reset = false)
        }
    }

    private fun showExerciseDetail(exercise: ExerciseResponse) {
        Log.i(TAG, "showExerciseDetail: Mostrando detalle ejercicio ${exercise.id}")
        // Crea un Bundle para pasar el ID
        val bundle = Bundle().apply {
            putLong("exerciseId", exercise.id)
        }
        findNavController().navigate(R.id.navigation_exercise_detail, bundle)
    }

    private fun editExercise(exercise: ExerciseResponse) {
        Log.i(TAG, "editExercise: Editando ejercicio ${exercise.id}")
        val bundle = Bundle().apply {
            putLong("exerciseId", exercise.id)
        }
        findNavController().navigate(R.id.navigation_edit_exercise, bundle)
    }

    private fun showDeleteConfirmation(exercise: ExerciseResponse) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar ejercicio")
            .setMessage("¿Estás seguro de eliminar el ejercicio '${exercise.name}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                exerciseViewModel.deleteExercise(exercise.id)
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun toggleExerciseStatus(exercise: ExerciseResponse) {
        Log.i(TAG, "toggleExerciseStatus: Cambiando estado ejercicio ${exercise.id}")
        exerciseViewModel.toggleExerciseStatus(exercise.id)
    }

    private fun navigateToCreateExercise() {
        Log.i(TAG, "navigateToCreateExercise: Navegando a crear ejercicio")
        findNavController().navigate(R.id.navigation_create_exercise)
    }

    private fun showEmptyState(message: String) {
        Log.d(TAG, "showEmptyState: Mostrando estado vacío - $message")
        binding.recyclerExercises.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.tvEmptyState.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "onDestroyView: Destruyendo vista")
        _binding = null
    }


}