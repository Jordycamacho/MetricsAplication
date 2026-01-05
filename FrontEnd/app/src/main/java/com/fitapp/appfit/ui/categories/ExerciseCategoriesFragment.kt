package com.fitapp.appfit.ui.categories

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.R
import com.fitapp.appfit.databinding.FragmentExerciseCategoriesBinding
import com.fitapp.appfit.model.ExerciseCategoryViewModel
import com.fitapp.appfit.response.category.request.ExerciseCategoryFilterRequest
import com.fitapp.appfit.response.category.response.ExerciseCategoryPageResponse
import com.fitapp.appfit.response.category.response.ExerciseCategoryResponse
import com.fitapp.appfit.ui.categories.adapter.ExerciseCategoryAdapter
import com.fitapp.appfit.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ExerciseCategoriesFragment : Fragment() {
    private var _binding: FragmentExerciseCategoriesBinding? = null
    private val binding get() = _binding!!
    private val categoryViewModel: ExerciseCategoryViewModel by viewModels()
    private lateinit var categoryAdapter: ExerciseCategoryAdapter

    // Filtro actual
    private var currentFilter = "all" // "all", "my", "available"
    private var currentSportId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        setupSearchListener()

        // Cargar categorías iniciales
        loadCategories()
    }

    private fun setupRecyclerView() {
        categoryAdapter = ExerciseCategoryAdapter(
            onItemClick = { category ->
                showCategoryDetail(category)
            },
            onEditClick = { category ->
                editCategory(category)
            },
            onDeleteClick = { category ->
                showDeleteConfirmation(category)
            }
        )

        binding.recyclerCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        // Botón flotante para crear categoría
        binding.fabCreateCategory.setOnClickListener {
            navigateToCreateCategory()
        }

        // Filtros
        binding.chipAll.setOnClickListener {
            currentFilter = "all"
            binding.chipAll.isChecked = true
            binding.chipMy.isChecked = false
            binding.chipAvailable.isChecked = false
            loadCategories()
        }

        binding.chipMy.setOnClickListener {
            currentFilter = "my"
            binding.chipAll.isChecked = false
            binding.chipMy.isChecked = true
            binding.chipAvailable.isChecked = false
            loadCategories()
        }

        binding.chipAvailable.setOnClickListener {
            currentFilter = "available"
            binding.chipAll.isChecked = false
            binding.chipMy.isChecked = false
            binding.chipAvailable.isChecked = true

            // Aquí podrías mostrar un diálogo para seleccionar deporte
            // Por ahora cargamos sin deporte (se cargará empty)
            currentSportId = null
            loadCategories()
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
        // Observar todas las categorías
        categoryViewModel.allCategoriesState.observe(viewLifecycleOwner, Observer { resource ->
            resource?.let {
                handleCategoriesResponse(it, "No hay categorías disponibles")
            }
        })

        // Observar mis categorías
        categoryViewModel.myCategoriesState.observe(viewLifecycleOwner, Observer { resource ->
            resource?.let {
                handleCategoriesResponse(it, "No has creado categorías personalizadas")
            }
        })

        // Observar categorías disponibles
        categoryViewModel.availableCategoriesState.observe(viewLifecycleOwner, Observer { resource ->
            resource?.let {
                handleCategoriesResponse(it, "No hay categorías disponibles para este deporte")
            }
        })

        // Observar estado de eliminación
        categoryViewModel.deleteCategoryState.observe(viewLifecycleOwner, Observer { resource ->
            resource?.let {
                when (it) {
                    is Resource.Success -> {
                        Toast.makeText(requireContext(), "✅ Categoría eliminada", Toast.LENGTH_SHORT).show()
                        loadCategories() // Recargar lista
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), "❌ Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        })
    }

    private fun handleCategoriesResponse(resource: Resource<ExerciseCategoryPageResponse>, emptyMessage: String) {
        when (resource) {
            is Resource.Success -> {
                hideLoading()
                resource.data?.let { pageResponse ->
                    val categories = pageResponse.content
                    if (categories.isEmpty()) {
                        showEmptyState(emptyMessage)
                    } else {
                        showCategoriesList()
                        categoryAdapter.updateList(categories)
                    }
                }
            }
            is Resource.Error -> {
                hideLoading()
                showError(resource.message ?: "Error al cargar categorías")
                showEmptyState("Error al cargar")
            }
            is Resource.Loading -> {
                showLoading()
            }
        }
    }

    private fun loadCategories() {
        val searchQuery = binding.etSearch.text.toString().trim()

        // Crear filtro con búsqueda si existe
        val filterRequest = ExerciseCategoryFilterRequest(
            search = searchQuery,
            page = 0,
            size = 20,
            sortBy = "name",
            direction = "ASC"
        )

        when (currentFilter) {
            "all" -> {
                categoryViewModel.searchAllCategories(filterRequest)
            }
            "my" -> {
                // Para "mis categorías", usar onlyMine = true
                val myFilter = filterRequest.copy(onlyMine = true, includePredefined = false)
                categoryViewModel.searchMyCategories(myFilter)
            }
            "available" -> {
                currentSportId?.let { sportId ->
                    val sportFilter = filterRequest.copy(sportId = sportId)
                    categoryViewModel.searchAvailableCategories(sportId, sportFilter)
                } ?: run {
                    // Para categorías disponibles sin deporte específico
                    categoryViewModel.searchAllCategories(filterRequest)
                }
            }
        }
    }

    private fun performSearch() {
        loadCategories()
    }

    private fun navigateToCreateCategory() {
        findNavController().navigate(R.id.navigation_create_category)
    }

    private fun showCategoryDetail(category: ExerciseCategoryResponse) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(category.name)
            .setMessage(
                """
                Descripción: ${category.description ?: "No especificada"}
                Tipo: ${if (category.isPredefined) "Predefinida" else "Personal"}
                Visibilidad: ${if (category.isPublic) "Pública" else "Privada"}
                Deporte: ${category.sportName ?: "Todos"}
                Creado por: ${category.ownerName ?: "Sistema"}
                Usos: ${category.usageCount}
                Creado: ${category.createdAt}
                Actualizado: ${category.updatedAt}
                """.trimIndent()
            )
            .setPositiveButton("Aceptar", null)
            .show()
    }

    private fun editCategory(category: ExerciseCategoryResponse) {
        if (category.isPredefined) {
            Toast.makeText(requireContext(), "No se puede editar una categoría predefinida", Toast.LENGTH_SHORT).show()
            return
        }
        val bundle = bundleOf("categoryId" to category.id)
        findNavController().navigate(R.id.navigation_edit_category, bundle)
    }

    private fun showDeleteConfirmation(category: ExerciseCategoryResponse) {
        if (category.isPredefined) {
            Toast.makeText(requireContext(), "No se puede eliminar una categoría predefinida", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar Categoría")
            .setMessage("¿Estás seguro de que quieres eliminar '${category.name}'?\n\nEsta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { dialog, _ ->
                categoryViewModel.deleteCategory(category.id)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerCategories.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showCategoriesList() {
        binding.recyclerCategories.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
    }

    private fun showEmptyState(message: String) {
        binding.recyclerCategories.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.tvEmptyState.text = message
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), "❌ $message", Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        loadCategories()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}