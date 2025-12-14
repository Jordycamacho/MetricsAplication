package com.fitapp.appfit.ui.home

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
import com.fitapp.appfit.databinding.FragmentHomeBinding
import com.fitapp.appfit.model.RoutineViewModel
import com.fitapp.appfit.response.routine.response.RoutineSummaryResponse
import com.fitapp.appfit.ui.routines.adapter.RoutineAdapter
import com.fitapp.appfit.utils.Resource

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val routineViewModel: RoutineViewModel by viewModels()
    private lateinit var recentRoutineAdapter: RoutineAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        setupObservers()

        // Cargar rutinas cuando se abre la pantalla
        loadRecentRoutines()
    }

    private fun setupRecyclerView() {
        recentRoutineAdapter = RoutineAdapter(
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

        binding.recyclerRecentRoutines.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = recentRoutineAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        // Botón para crear rutina
        binding.cardCreateRoutine.setOnClickListener {
            findNavController().navigate(R.id.navigation_create_routine)
        }

        // Ver todas las rutinas (puedes agregar un botón/texto para esto)
        binding.textViewSeeAll.setOnClickListener {
            // Navegar a la pantalla completa de rutinas
            // Puedes usar BottomNavigation o cualquier navegación que tengas configurada
            findNavController().navigate(R.id.navigation_routines)
        }
    }

    private fun setupObservers() {
        // Observar lista de rutinas recientes
        routineViewModel.routinesListState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    resource.data?.let { pageResponse ->
                        val routines = pageResponse.content
                        if (routines.isEmpty()) {
                            showEmptyRecentRoutines()
                        } else {
                            showRecentRoutinesList()
                            // Tomar solo las primeras 3 rutinas para mostrar en el home
                            val recentRoutines = routines.take(3)
                            recentRoutineAdapter.updateRoutines(recentRoutines)
                            updateProgressStats(routines)
                        }
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    showError(resource.message ?: "Error al cargar rutinas")
                    showEmptyRecentRoutines()
                }
                is Resource.Loading -> {
                    showLoading()
                }
            }
        })
    }

    private fun loadRecentRoutines() {
        routineViewModel.getRoutines()
    }

    private fun updateProgressStats(routines: List<RoutineSummaryResponse>) {
        // Aquí puedes actualizar las estadísticas de progreso
        val activeRoutines = routines.count { it.isActive }
        val totalRoutines = routines.size

        // Actualizar el texto de progreso
        binding.textProgress.text = "$activeRoutines de $totalRoutines rutinas activas"

        // Actualizar la barra de progreso
        if (totalRoutines > 0) {
            val progress = (activeRoutines * 100) / totalRoutines
            binding.progressBarWeekly.progress = progress
        }
    }

    private fun showRoutineDetail(routine: RoutineSummaryResponse) {
        // Navegar a pantalla de detalle
        Toast.makeText(requireContext(), "Ver detalle: ${routine.name}", Toast.LENGTH_SHORT).show()
        // TODO: Implementar navegación a pantalla de detalle
    }

    private fun editRoutine(routine: RoutineSummaryResponse) {
        // Navegar a pantalla de edición
        Toast.makeText(requireContext(), "Editar: ${routine.name}", Toast.LENGTH_SHORT).show()
    }

    private fun startWorkout(routine: RoutineSummaryResponse) {
        // Iniciar entrenamiento
        Toast.makeText(requireContext(), "Iniciar entrenamiento: ${routine.name}", Toast.LENGTH_SHORT).show()
        // TODO: Implementar navegación a pantalla de entrenamiento
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerRecentRoutines.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showRecentRoutinesList() {
        binding.recyclerRecentRoutines.visibility = View.VISIBLE
        binding.textEmptyRecentRoutines.visibility = View.GONE
    }

    private fun showEmptyRecentRoutines() {
        binding.recyclerRecentRoutines.visibility = View.GONE
        binding.textEmptyRecentRoutines.visibility = View.VISIBLE
        binding.textEmptyRecentRoutines.text = "No hay rutinas recientes"
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), "❌ $message", Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        // Refrescar rutinas cuando vuelva a la pantalla
        loadRecentRoutines()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}