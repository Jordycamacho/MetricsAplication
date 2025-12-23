package com.fitapp.appfit.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

    companion object {
        private const val TAG = "HomeFragment"
    }

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
        binding.cardCreateRoutine.setOnClickListener {
            findNavController().navigate(R.id.navigation_create_routine)
        }

        binding.textViewSeeAll.setOnClickListener {
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
                            val recentRoutines = routines.take(3)
                            recentRoutineAdapter.submitList(recentRoutines.toList()) // Usa toList()
                            updateProgressStats(routines)
                        }
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    val errorMsg = resource.message ?: "Error al cargar rutinas"
                    if (errorMsg.contains("500")) {
                        Log.e(TAG, "Error 500, reintentando en 2 segundos...")
                        Handler(Looper.getMainLooper()).postDelayed({
                            loadRecentRoutines()
                        }, 2000)
                    } else {
                        showError(errorMsg)
                        showEmptyRecentRoutines()
                    }
                }
                is Resource.Loading -> {
                    showLoading()
                }
            }
        })

        // Observador único para actualizaciones
        routineViewModel.anyUpdateEvent.observe(viewLifecycleOwner) {
            Log.d(TAG, "🔄 Home: Evento de actualización recibido")
            Handler(Looper.getMainLooper()).postDelayed({
                loadRecentRoutines()
            }, 500)
        }
    }

    private fun loadRecentRoutines() {
        Log.d(TAG, "📥 Home: Cargando rutinas recientes...")
        routineViewModel.getRoutines()
    }

    private fun updateProgressStats(routines: List<RoutineSummaryResponse>) {
        val activeRoutines = routines.count { it.isActive }
        val totalRoutines = routines.size

        binding.textProgress.text = "$activeRoutines de $totalRoutines rutinas activas"

        if (totalRoutines > 0) {
            val progress = (activeRoutines * 100) / totalRoutines
            binding.progressBarWeekly.progress = progress
        }
    }

    private fun showRoutineDetail(routine: RoutineSummaryResponse) {
        Toast.makeText(requireContext(), "Ver detalle: ${routine.name}", Toast.LENGTH_SHORT).show()
    }

    private fun editRoutine(routine: RoutineSummaryResponse) {
        Toast.makeText(requireContext(), "Editar: ${routine.name}", Toast.LENGTH_SHORT).show()
    }

    private fun startWorkout(routine: RoutineSummaryResponse) {
        Toast.makeText(requireContext(), "Iniciar entrenamiento: ${routine.name}", Toast.LENGTH_SHORT).show()
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
        Log.d(TAG, "🔄 Home reanudado")
        routineViewModel.clearAllUpdateStates()
        Handler(Looper.getMainLooper()).postDelayed({
            loadRecentRoutines()
        }, 300)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}