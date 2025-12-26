package com.fitapp.appfit.ui.sports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.R
import com.fitapp.appfit.databinding.FragmentSportsBinding
import com.fitapp.appfit.databinding.DialogCreateSportBinding
import com.fitapp.appfit.model.SportViewModel
import com.fitapp.appfit.response.sport.response.SportResponse
import com.fitapp.appfit.ui.sports.adapter.SportAdapter
import com.fitapp.appfit.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SportsFragment : Fragment() {

    private var _binding: FragmentSportsBinding? = null
    private val binding get() = _binding!!
    private val sportViewModel: SportViewModel by viewModels()
    private lateinit var sportAdapter: SportAdapter

    // Estado actual del filtro
    private var currentFilter = "all" // "all", "predefined", "custom"

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
        setupClickListeners()
        setupObservers()
        setupSearchListener()

        // Seleccionar chip por defecto
        binding.chipAll.isChecked = true

        // Cargar deportes al abrir la pantalla
        loadSports()
    }

    private fun setupRecyclerView() {
        sportAdapter = SportAdapter(
            onItemClick = { sport ->
                // Ahora no mostramos diálogo, puedes usar esto para navegar a otra pantalla
                // o mostrar opciones de acción
                showSportOptions(sport)
            },
            onDeleteClick = { sport ->
                showDeleteConfirmation(sport)
            }
        )

        binding.recyclerSports.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sportAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }
    }



    private fun setupClickListeners() {
        // Botón flotante para crear deporte
        binding.fabCreateSport.setOnClickListener {
            navigateToCreateSport()
        }

        // Chips de filtro
        binding.chipAll.setOnClickListener {
            currentFilter = "all"
            binding.chipAll.isChecked = true
            binding.chipPredefined.isChecked = false
            binding.chipCustom.isChecked = false
            loadSports()
        }

        binding.chipPredefined.setOnClickListener {
            currentFilter = "predefined"
            binding.chipAll.isChecked = false
            binding.chipPredefined.isChecked = true
            binding.chipCustom.isChecked = false
            loadPredefinedSports()
        }

        binding.chipCustom.setOnClickListener {
            currentFilter = "custom"
            binding.chipAll.isChecked = false
            binding.chipPredefined.isChecked = false
            binding.chipCustom.isChecked = true
            loadUserSports()
        }
    }

    private fun setupSearchListener() {
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    private fun setupObservers() {
        // Observar lista de deportes según filtro actual
        sportViewModel.sportsState.observe(viewLifecycleOwner, Observer { resource ->
            handleSportsResponse(resource, "No hay deportes disponibles")
        })

        sportViewModel.predefinedSportsState.observe(viewLifecycleOwner, Observer { resource ->
            handleSportsResponse(resource, "No hay deportes predefinidos")
        })

        sportViewModel.userSportsState.observe(viewLifecycleOwner, Observer { resource ->
            handleSportsResponse(resource, "No has creado deportes personalizados")
        })

        // Observar estado de eliminación
        sportViewModel.deleteSportState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "✅ Deporte eliminado", Toast.LENGTH_SHORT).show()
                    // Recargar según filtro actual
                    when (currentFilter) {
                        "all" -> loadSports()
                        "predefined" -> loadPredefinedSports()
                        "custom" -> loadUserSports()
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "❌ Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        })

        // Observar estado de creación
        sportViewModel.createSportState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "✅ Deporte creado", Toast.LENGTH_SHORT).show()
                    loadUserSports() // Recargar mis deportes
                    binding.chipCustom.isChecked = true
                    binding.chipAll.isChecked = false
                    binding.chipPredefined.isChecked = false
                    currentFilter = "custom"
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "❌ Error al crear: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    // Mostrar loading si quieres
                }
                else -> {}
            }
        })
    }

    private fun handleSportsResponse(resource: Resource<List<SportResponse>>, emptyMessage: String) {
        when (resource) {
            is Resource.Success -> {
                hideLoading()
                resource.data?.let { sports ->
                    if (sports.isEmpty()) {
                        showEmptyState(emptyMessage)
                    } else {
                        showSportsList()
                        sportAdapter.updateList(sports)
                    }
                }
            }
            is Resource.Error -> {
                hideLoading()
                showError(resource.message ?: "Error al cargar deportes")
                showEmptyState("Error al cargar")
            }
            is Resource.Loading -> {
                showLoading()
            }
        }
    }

    private fun loadSports() {
        sportViewModel.getSports()
    }

    private fun loadPredefinedSports() {
        sportViewModel.getPredefinedSports()
    }

    private fun loadUserSports() {
        sportViewModel.getUserSports()
    }

    private fun performSearch() {
        val query = binding.etSearch.text.toString().trim()
        if (query.isNotEmpty()) {
            Toast.makeText(requireContext(), "Buscando: $query", Toast.LENGTH_SHORT).show()
            // Aquí implementarías la búsqueda real
        }
    }

    private fun navigateToCreateSport() {
        // Crear diálogo simple para crear deporte
        showCreateSportDialog()
    }

    private fun showCreateSportDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_create_sport, null)
        val etName = view.findViewById<EditText>(R.id.et_sport_name)
        val etCategory = view.findViewById<EditText>(R.id.et_sport_category)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Crear Deporte Personalizado")
            .setView(view)
            .setPositiveButton("Crear") { dialogInterface, _ ->
                val name = etName.text.toString().trim()
                val category = etCategory.text.toString().trim()

                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                } else {
                    sportViewModel.createCustomSport(name, category, emptyMap())
                    dialogInterface.dismiss()
                }
            }
            .setNegativeButton("Cancelar") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun showSportOptions(sport: SportResponse) {
        // Aquí podrías mostrar un menú de opciones si quieres
        // Por ahora, solo mostramos un toast para indicar que se seleccionó
        if (sport.isPredefined) {
            Toast.makeText(requireContext(), "Deporte predefinido: ${sport.name}", Toast.LENGTH_SHORT).show()
        } else {
            // Para deportes personalizados, podrías mostrar opciones de edición
            Toast.makeText(requireContext(), "Tu deporte: ${sport.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmation(sport: SportResponse) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar Deporte")
            .setMessage("¿Estás seguro de que quieres eliminar '${sport.name}'?\n\nEsta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { dialog, _ ->
                sportViewModel.deleteCustomSport(sport.id)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerSports.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showSportsList() {
        binding.recyclerSports.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
    }

    private fun showEmptyState(message: String) {
        binding.recyclerSports.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.tvEmptyState.text = message
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), "❌ $message", Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        // Recargar datos cuando se vuelve a la pantalla
        when (currentFilter) {
            "all" -> loadSports()
            "predefined" -> loadPredefinedSports()
            "custom" -> loadUserSports()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}