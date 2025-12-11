package com.fitapp.appfit.ui.routines

import android.os.Bundle
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

        // Cargar rutinas cuando se abre la pantalla
        loadRoutines()
    }

    private fun setupRecyclerView() {
        routineAdapter = RoutineAdapter(
            onItemClick = { routine ->
                // Ver detalle de rutina
                showRoutineDetail(routine)
            },
            onEditClick = { routine ->
                // Editar rutina
                editRoutine(routine)
            },
            onStartClick = { routine ->
                // Iniciar entrenamiento
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
        // Botón flotante para crear rutina
        binding.fabCreateRoutine.setOnClickListener {
            navigateToCreateRoutine()
        }

        // Tarjeta para crear rutina
        binding.cardCreateRoutine.setOnClickListener {
            navigateToCreateRoutine()
        }

        // Buscar rutinas
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
                    resource.data?.let { routines ->
                        if (routines.isEmpty()) {
                            showEmptyState()
                        } else {
                            showRoutinesList()
                            routineAdapter.updateRoutines(routines)
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

        // Observar estado de carga
        routineViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        })
    }

    private fun loadRoutines() {
        routineViewModel.getRoutines()
    }

    private fun searchRoutines() {
        val query = binding.etSearchRoutines.text.toString().trim()
        if (query.isNotEmpty()) {
            // TODO: Implementar búsqueda
            Toast.makeText(requireContext(), "Buscando: $query", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToCreateRoutine() {
        findNavController().navigate(R.id.navigation_create_routine)
    }

    private fun showRoutineDetail(routine: RoutineSummaryResponse) {
        // TODO: Navegar a pantalla de detalle
        Toast.makeText(requireContext(), "Ver detalle: ${routine.name}", Toast.LENGTH_SHORT).show()
    }

    private fun editRoutine(routine: RoutineSummaryResponse) {
        // TODO: Navegar a pantalla de edición
        Toast.makeText(requireContext(), "Editar: ${routine.name}", Toast.LENGTH_SHORT).show()
    }

    private fun startWorkout(routine: RoutineSummaryResponse) {
        // TODO: Iniciar entrenamiento
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
        // Refrescar lista cuando vuelva a la pantalla
        loadRoutines()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}