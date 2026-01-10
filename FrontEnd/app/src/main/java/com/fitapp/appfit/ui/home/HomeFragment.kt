// com.fitapp.appfit.ui.home/HomeFragment.kt
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
import com.fitapp.appfit.ui.routines.adapter.RoutineAdapter
import com.fitapp.appfit.utils.Resource

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val routineViewModel: RoutineViewModel by viewModels()
    private lateinit var recentRoutineAdapter: RoutineAdapter
    private lateinit var lastUsedRoutineAdapter: RoutineAdapter

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

        loadLastUsedRoutines() // CAMBIO: Cargar últimas rutinas usadas en lugar de recientes
    }

    private fun setupRecyclerView() {
        // Adapter para las últimas rutinas usadas
        lastUsedRoutineAdapter = RoutineAdapter(
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
            adapter = lastUsedRoutineAdapter
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
        routineViewModel.lastUsedRoutinesState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    resource.data?.let { lastUsedRoutines ->
                        if (lastUsedRoutines.isEmpty()) {
                            showEmptyRecentRoutines()
                        } else {
                            showRecentRoutinesList()
                            lastUsedRoutineAdapter.submitList(lastUsedRoutines.toList())
                        }
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    val errorMsg = resource.message ?: "Error al cargar últimas rutinas usadas"
                    if (errorMsg.contains("500")) {
                        Log.e(TAG, "Error 500, reintentando en 2 segundos...")
                        Handler(Looper.getMainLooper()).postDelayed({
                            loadLastUsedRoutines()
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
                loadLastUsedRoutines()
            }, 500)
        }
    }

    private fun loadLastUsedRoutines() {
        Log.d(TAG, "📥 Home: Cargando últimas rutinas usadas...")
        routineViewModel.getLastUsedRoutines(3) // Límite de 3 rutinas
    }

    private fun startWorkout(routine: com.fitapp.appfit.response.routine.response.RoutineSummaryResponse) {
        // CAMBIO: Marcar rutina como usada al iniciar entrenamiento
        routineViewModel.markRoutineAsUsed(routine.id)

        // Mostrar mensaje
        Toast.makeText(
            requireContext(),
            "✅ Iniciando entrenamiento: ${routine.name}",
            Toast.LENGTH_SHORT
        ).show()

        // Aquí puedes agregar la navegación a la pantalla de entrenamiento
        // findNavController().navigate(R.id.navigation_workout)
    }

    private fun showRoutineDetail(routine: com.fitapp.appfit.response.routine.response.RoutineSummaryResponse) {
        Toast.makeText(requireContext(), "Ver detalle: ${routine.name}", Toast.LENGTH_SHORT).show()
    }

    private fun editRoutine(routine: com.fitapp.appfit.response.routine.response.RoutineSummaryResponse) {
        Toast.makeText(requireContext(), "Editar: ${routine.name}", Toast.LENGTH_SHORT).show()
        // Aquí puedes agregar navegación a editar rutina si lo deseas
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
        // CAMBIO: Mensaje más específico
        binding.textEmptyRecentRoutines.text = "No hay rutinas usadas recientemente"
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), "❌ $message", Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "🔄 Home reanudado")
        routineViewModel.clearAllUpdateStates()
        Handler(Looper.getMainLooper()).postDelayed({
            loadLastUsedRoutines()
        }, 300)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}