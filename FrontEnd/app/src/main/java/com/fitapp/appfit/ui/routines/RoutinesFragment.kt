package com.fitapp.appfit.ui.routines

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
        loadRoutines() // CORREGIDO: Esto carga todas las rutinas, no las últimas usadas
    }

    private fun setupNavigationResultListener() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
            NavigationKeys.ROUTINE_UPDATED
        )?.observe(viewLifecycleOwner) { updated ->
            if (updated == true) {
                Log.d(TAG, "🔄 Recibida señal de rutina actualizada desde SavedStateHandle")
                Handler(Looper.getMainLooper()).postDelayed({
                    loadRoutines()
                }, 500)
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>(
                    NavigationKeys.ROUTINE_UPDATED
                )
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
            NavigationKeys.ROUTINE_DELETED
        )?.observe(viewLifecycleOwner) { deleted ->
            if (deleted == true) {
                Log.d(TAG, "🗑️ Recibida señal de rutina eliminada desde SavedStateHandle")
                Handler(Looper.getMainLooper()).postDelayed({
                    loadRoutines()
                }, 500)
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
        setupRoutinesListObserver()
        setupUpdateObservers()

        // NO agregues el observador para lastUsedRoutinesState aquí
        // Solo HomeFragment necesita observar lastUsedRoutinesState
    }

    private fun setupRoutinesListObserver() {
        // Observar lista de rutinas (TODAS las rutinas)
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
                            routineAdapter.submitList(routinesList.toList())
                        }
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    val errorMsg = resource.message ?: "Error al cargar rutinas"
                    if (errorMsg.contains("500")) {
                        Log.e(TAG, "Error 500 del servidor, esperando 2 segundos...")
                        Handler(Looper.getMainLooper()).postDelayed({
                            loadRoutines()
                        }, 2000)
                    } else {
                        showError(errorMsg)
                        showEmptyState()
                    }
                }
                is Resource.Loading -> {
                    showLoading()
                }
            }
        })
    }

    private fun setupUpdateObservers() {
        // Observador ÚNICO para todas las actualizaciones
        routineViewModel.anyUpdateEvent.observe(viewLifecycleOwner) {
            Log.d(TAG, "🔄 Evento de actualización recibido - Recargando lista")
            Handler(Looper.getMainLooper()).postDelayed({
                loadRoutines()
            }, 500)
        }

        routineViewModel.refreshTrigger.observe(viewLifecycleOwner) {
            Log.d(TAG, "🔄 Refresh trigger recibido - Recargando lista")
            Handler(Looper.getMainLooper()).postDelayed({
                loadRoutines()
            }, 500)
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
        // Marcar rutina como usada
        routineViewModel.markRoutineAsUsed(routine.id)

        Toast.makeText(
            requireContext(),
            "✅ Iniciando entrenamiento: ${routine.name}",
            Toast.LENGTH_SHORT
        ).show()

        // Aquí puedes agregar navegación a la pantalla de entrenamiento
        // findNavController().navigate(R.id.navigation_workout)
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
        Log.d(TAG, "🔄 Fragmento reanudado - Limpiando estados y recargando")
        routineViewModel.clearAllUpdateStates()

        Handler(Looper.getMainLooper()).postDelayed({
            loadRoutines()
        }, 300)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}