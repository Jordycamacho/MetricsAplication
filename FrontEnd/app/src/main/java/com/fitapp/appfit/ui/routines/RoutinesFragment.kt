package com.fitapp.appfit.ui.routines

import android.os.Bundle
import android.util.Log
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

    companion object {
        private const val TAG = "RoutinesFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoutinesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        setupNavigationResultListener()

        // Cargar rutinas cuando se abre la pantalla
        loadRoutines()
    }

    private fun setupNavigationResultListener() {
        // Escuchar resultados de navegación desde edición
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
            NavigationKeys.ROUTINE_UPDATED
        )?.observe(viewLifecycleOwner) { updated ->
            if (updated == true) {
                Log.d(TAG, "🔄 Recibida señal de rutina actualizada")
                loadRoutines()
                // Limpiar el estado
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>(
                    NavigationKeys.ROUTINE_UPDATED
                )
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
            NavigationKeys.ROUTINE_DELETED
        )?.observe(viewLifecycleOwner) { deleted ->
            if (deleted == true) {
                Log.d(TAG, "🗑️ Recibida señal de rutina eliminada")
                loadRoutines()
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>(
                    NavigationKeys.ROUTINE_DELETED
                )
            }
        }
    }

    private fun setupRecyclerView() {
        routineAdapter = RoutineAdapter(
            onItemClick = { routine ->
                showRoutineDetail(routine)
            },
            onEditClick = { routine ->
                editRoutine(routine)
            },
            onStartClick = { routine ->
                startWorkout(routine)
            }
        )

        binding.recyclerRoutines.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = routineAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.fabCreateRoutine.setOnClickListener {
            navigateToCreateRoutine()
        }

        binding.cardCreateRoutine.setOnClickListener {
            navigateToCreateRoutine()
        }

        binding.etSearchRoutines.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                searchRoutines()
                true
            } else {
                false
            }
        }
    }

    private fun setupObservers() {
        // Observar lista de rutinas
        routineViewModel.routinesListState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    resource.data?.let { pageResponse ->
                        val routinesList = pageResponse.content
                        if (routinesList.isEmpty()) {
                            showEmptyState()
                        } else {
                            showRoutinesList()
                            routineAdapter.updateRoutines(routinesList)
                        }
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    showError(resource.message ?: "Error al cargar rutinas")
                    showEmptyState()
                }
                is Resource.Loading -> {
                    showLoading()
                }
            }
        })

        // Observar cambios globales desde ViewModel
        routineViewModel.routinesUpdated.observe(viewLifecycleOwner) { updated ->
            if (updated == true) {
                Log.d(TAG, "🔄 ViewModel notificó actualización - Recargando lista")
                loadRoutines()
                routineViewModel.resetUpdateState()
            }
        }

        routineViewModel.routineDeleted.observe(viewLifecycleOwner) { deletedId ->
            deletedId?.let {
                Log.d(TAG, "🗑️ ViewModel notificó eliminación de ID: $it - Recargando lista")
                loadRoutines()
                routineViewModel.resetDeleteState()
            }
        }
    }

    private fun loadRoutines() {
        Log.d(TAG, "📥 Cargando lista de rutinas...")
        routineViewModel.getRoutines(page = 0, size = 20)
    }

    private fun searchRoutines() {
        val query = binding.etSearchRoutines.text.toString().trim()
        if (query.isNotEmpty()) {
            Toast.makeText(requireContext(), "Buscando: $query", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToCreateRoutine() {
        findNavController().navigate(R.id.navigation_create_routine)
    }

    private fun showRoutineDetail(routine: RoutineSummaryResponse) {
        Toast.makeText(requireContext(), "Ver detalle: ${routine.name}", Toast.LENGTH_SHORT).show()
    }

    private fun editRoutine(routine: RoutineSummaryResponse) {
        try {
            val action = RoutinesFragmentDirections.actionNavigationRoutinesToNavigationEditRoutine(routine.id)
            findNavController().navigate(action)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Error al navegar: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun startWorkout(routine: RoutineSummaryResponse) {
        Toast.makeText(requireContext(), "Iniciar: ${routine.name}", Toast.LENGTH_SHORT).show()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerRoutines.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showRoutinesList() {
        binding.recyclerRoutines.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.recyclerRoutines.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), "❌ $message", Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "🔄 Fragmento reanudado - Recargando rutinas")
        loadRoutines()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}