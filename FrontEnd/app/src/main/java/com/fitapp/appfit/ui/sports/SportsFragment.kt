package com.fitapp.appfit.ui.sports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.databinding.DialogCreateSportBinding
import com.fitapp.appfit.databinding.FragmentSportsBinding
import com.fitapp.appfit.model.SportViewModel
import com.fitapp.appfit.response.sport.response.SportResponse
import com.fitapp.appfit.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SportsFragment : Fragment() {

    private var _binding: FragmentSportsBinding? = null
    private val binding get() = _binding!!
    private val sportViewModel: SportViewModel by viewModels()
    private lateinit var sportAdapter: SportAdapter

    private var currentFilter = "all"
    private var isUpdatingChips = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupChipListeners()
        setupObservers()
        setupSearchListener()
        binding.fabCreateSport.setOnClickListener { showCreateSportDialog() }
        loadSports()
    }

    private fun setupRecyclerView() {
        sportAdapter = SportAdapter(
            onItemClick = { sport -> showSportDetail(sport) },
            onDeleteClick = { sport -> showDeleteConfirmation(sport) }
        )
        binding.recyclerSports.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sportAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }
    }

    private fun setupChipListeners() {
        // Mismo fix que en ExerciseParamsFragment: solo reaccionar cuando isChecked = true
        binding.chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isUpdatingChips) { currentFilter = "all"; loadSports() }
        }
        binding.chipPredefined.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isUpdatingChips) { currentFilter = "predefined"; loadPredefinedSports() }
        }
        binding.chipCustom.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isUpdatingChips) { currentFilter = "custom"; loadUserSports() }
        }
    }

    private fun setupSearchListener() {
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { performSearch(); true } else false
        }
    }

    private fun setupObservers() {
        sportViewModel.sportsState.observe(viewLifecycleOwner) { resource ->
            handleSportsResponse(resource, "No hay deportes disponibles")
        }
        sportViewModel.predefinedSportsState.observe(viewLifecycleOwner) { resource ->
            handleSportsResponse(resource, "No hay deportes predefinidos")
        }
        sportViewModel.userSportsState.observe(viewLifecycleOwner) { resource ->
            handleSportsResponse(resource, "Aún no has creado deportes personalizados")
        }

        sportViewModel.deleteSportState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Deporte eliminado", Toast.LENGTH_SHORT).show()
                    reloadCurrentFilter()
                }
                is Resource.Error -> Toast.makeText(requireContext(), resource.message ?: "Error al eliminar", Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }

        sportViewModel.createSportState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Deporte creado", Toast.LENGTH_SHORT).show()
                    // Cambiar a "Mis deportes" sin disparar doble carga
                    isUpdatingChips = true
                    currentFilter = "custom"
                    isUpdatingChips = false
                    loadUserSports()
                }
                is Resource.Error -> Toast.makeText(requireContext(), resource.message ?: "Error al crear", Toast.LENGTH_SHORT).show()
                is Resource.Loading -> {}
                else -> {}
            }
        }
    }

    private fun handleSportsResponse(resource: Resource<List<SportResponse>>, emptyMessage: String) {
        when (resource) {
            is Resource.Success -> {
                hideLoading()
                val sports = resource.data ?: emptyList()
                if (sports.isEmpty()) showEmptyState(emptyMessage)
                else { showSportsList(); sportAdapter.updateList(sports) }
            }
            is Resource.Error -> {
                hideLoading()
                showEmptyState("Error al cargar")
                Toast.makeText(requireContext(), resource.message ?: "Error", Toast.LENGTH_SHORT).show()
            }
            is Resource.Loading -> showLoading()
        }
    }

    private fun loadSports() { sportViewModel.getSports() }
    private fun loadPredefinedSports() { sportViewModel.getPredefinedSports() }
    private fun loadUserSports() { sportViewModel.getUserSports() }

    private fun reloadCurrentFilter() {
        when (currentFilter) {
            "all" -> loadSports()
            "predefined" -> loadPredefinedSports()
            "custom" -> loadUserSports()
        }
    }

    private fun performSearch() {
        val query = binding.etSearch.text.toString().trim()
        if (query.isNotEmpty()) {
            // TODO: implementar búsqueda real cuando el backend la soporte
        }
    }

    private fun showSportDetail(sport: SportResponse) {
        // Predefinidos y personalizados: por ahora sin acción adicional
        // TODO: navegar a detalle o mostrar BottomSheet si se necesita
    }

    private fun showCreateSportDialog() {
        val dialogBinding = DialogCreateSportBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nuevo deporte")
            .setView(dialogBinding.root)
            .setPositiveButton("Crear") { dialog, _ ->
                val name = dialogBinding.etSportName.text.toString().trim()
                val category = dialogBinding.etSportCategory.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                } else {
                    sportViewModel.createCustomSport(name, category, emptyMap())
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showDeleteConfirmation(sport: SportResponse) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar deporte")
            .setMessage("¿Eliminar '${sport.name}'? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { dialog, _ ->
                sportViewModel.deleteCustomSport(sport.id)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerSports.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.GONE
    }

    private fun hideLoading() { binding.progressBar.visibility = View.GONE }

    private fun showSportsList() {
        binding.recyclerSports.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
    }

    private fun showEmptyState(message: String) {
        binding.recyclerSports.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.tvEmptyState.text = message
    }

    override fun onResume() {
        super.onResume()
        reloadCurrentFilter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}