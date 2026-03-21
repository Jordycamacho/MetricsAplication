package com.fitapp.appfit.feature.exercise.ui.category

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.R
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentExerciseCategoriesBinding
import com.fitapp.appfit.feature.exercise.model.category.request.ExerciseCategoryFilterRequest
import com.fitapp.appfit.feature.exercise.model.category.response.ExerciseCategoryPageResponse
import com.fitapp.appfit.feature.exercise.model.category.response.ExerciseCategoryResponse
import com.fitapp.appfit.feature.exercise.ExerciseCategoryViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ExerciseCategoriesFragment : Fragment() {

    private var _binding: FragmentExerciseCategoriesBinding? = null
    private val binding get() = _binding!!

    private val categoryViewModel: ExerciseCategoryViewModel by viewModels()
    private lateinit var categoryAdapter: ExerciseCategoryAdapter

    private var currentFilter = "all"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupChips()
        setupSearch()
        setupFab()
        setupObservers()
        loadCategories()
    }

    private fun setupRecyclerView() {
        categoryAdapter = ExerciseCategoryAdapter(
            onItemClick = { showCategoryDetail(it) },
            onEditClick = { editCategory(it) },
            onDeleteClick = { showDeleteConfirmation(it) }
        )
        binding.recyclerCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupChips() {
        binding.chipAll.setOnClickListener { currentFilter = "all"; loadCategories() }
        binding.chipMy.setOnClickListener { currentFilter = "my"; loadCategories() }
        binding.chipPredefined.setOnClickListener { currentFilter = "predefined"; loadCategories() }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { loadCategories() }
        })
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { loadCategories(); true } else false
        }
    }

    private fun setupFab() {
        binding.fabCreateCategory.setOnClickListener {
            findNavController().navigate(R.id.navigation_create_category)
        }
    }

    private fun setupObservers() {
        categoryViewModel.allCategoriesState.observe(viewLifecycleOwner) { resource ->
            if (currentFilter == "all" || currentFilter == "predefined") {
                resource?.let { handleResponse(it) }
            }
        }

        categoryViewModel.myCategoriesState.observe(viewLifecycleOwner) { resource ->
            if (currentFilter == "my") {
                resource?.let { handleResponse(it) }
            }
        }

        categoryViewModel.deleteCategoryState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    categoryViewModel.clearDeleteState()
                    Toast.makeText(requireContext(), "Categoría eliminada", Toast.LENGTH_SHORT).show()
                    loadCategories()
                }
                is Resource.Error -> {
                    categoryViewModel.clearDeleteState()
                    Toast.makeText(requireContext(), resource.message ?: "Error al eliminar", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun handleResponse(resource: Resource<ExerciseCategoryPageResponse>) {
        when (resource) {
            is Resource.Success -> {
                hideLoading()
                val categories = resource.data?.content ?: emptyList()
                if (categories.isEmpty()) showEmpty() else showList(categories)
            }
            is Resource.Error -> {
                hideLoading()
                showEmpty()
                binding.tvEmptyState.text = "Error al cargar"
                binding.tvEmptySubtitle.text = resource.message ?: ""
            }
            is Resource.Loading -> showLoading()
        }
    }

    private fun loadCategories() {
        val query = binding.etSearch.text?.toString()?.trim()
        when (currentFilter) {
            "all" -> categoryViewModel.searchAllCategories(
                ExerciseCategoryFilterRequest(search = query, page = 0, size = 50)
            )
            "my" -> categoryViewModel.searchMyCategories(
                ExerciseCategoryFilterRequest(
                    search = query, onlyMine = true, includePredefined = false, page = 0, size = 50
                )
            )
            "predefined" -> categoryViewModel.searchAllCategories(
                ExerciseCategoryFilterRequest(
                    search = query,
                    isPredefined = true,
                    includePredefined = true,
                    page = 0,
                    size = 50
                )
            )
        }
    }

    private fun editCategory(category: ExerciseCategoryResponse) {
        if (category.isPredefined) {
            Toast.makeText(requireContext(), "Las categorías del sistema no se pueden editar", Toast.LENGTH_SHORT).show()
            return
        }
        findNavController().navigate(
            R.id.navigation_edit_category,
            bundleOf("categoryId" to category.id)
        )
    }

    private fun showDeleteConfirmation(category: ExerciseCategoryResponse) {
        if (category.isPredefined) {
            Toast.makeText(requireContext(), "Las categorías del sistema no se pueden eliminar", Toast.LENGTH_SHORT).show()
            return
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar categoría")
            .setMessage("¿Eliminar \"${category.name}\"? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { dialog, _ ->
                categoryViewModel.deleteCategory(category.id)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showCategoryDetail(category: ExerciseCategoryResponse) {
        val tipo = if (category.isPredefined) "Sistema" else "Personal"
        val deporte = category.sportName ?: "Todos los deportes"
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(category.name)
            .setMessage("Tipo: $tipo\nDeporte: $deporte\nUsos: ${category.usageCount}")
            .setPositiveButton("Cerrar", null)
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

    private fun showList(categories: List<ExerciseCategoryResponse>) {
        binding.recyclerCategories.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
        categoryAdapter.updateList(categories)
    }

    private fun showEmpty() {
        binding.recyclerCategories.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
        val query = binding.etSearch.text?.toString()?.trim() ?: ""
        binding.tvEmptyState.text = when {
            query.isNotEmpty() -> "Sin resultados para \"$query\""
            currentFilter == "my" -> "Sin categorías propias"
            currentFilter == "predefined" -> "Sin categorías del sistema"
            else -> "Sin categorías"
        }
        binding.tvEmptySubtitle.text = if (currentFilter == "my" && query.isEmpty())
            "Pulsa + para crear la primera" else ""
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