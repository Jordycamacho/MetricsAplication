package com.fitapp.appfit.ui.categories.create

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.fitapp.appfit.R
import com.fitapp.appfit.databinding.FragmentCreateCategoryBinding
import com.fitapp.appfit.model.ExerciseCategoryViewModel
import com.fitapp.appfit.model.SportViewModel
import com.fitapp.appfit.response.category.request.ExerciseCategoryFilterRequest
import com.fitapp.appfit.response.category.request.ExerciseCategoryRequest
import com.fitapp.appfit.response.sport.response.SportResponse
import com.fitapp.appfit.utils.Resource

class CreateCategoryFragment : Fragment() {

    private var _binding: FragmentCreateCategoryBinding? = null
    private val binding get() = _binding!!
    private val categoryViewModel: ExerciseCategoryViewModel by viewModels()
    private val sportViewModel: SportViewModel by viewModels()

    private val sportsMap = mutableMapOf<String, Long>()
    private val parentCategoriesMap = mutableMapOf<String, Long>()
    private var selectedSportId: Long? = null
    private var selectedParentCategoryId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupClickListeners()
        setupObservers()
        loadInitialData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            createCategory()
        }
    }

    private fun loadInitialData() {
        // Cargar deportes
        sportViewModel.getAllSports()
        // Cargar categorías disponibles para padre
        loadAvailableCategories()
    }

    private fun loadAvailableCategories() {
        Log.d("CreateCategory", "Cargando categorías disponibles para padre...")
        val filterRequest = ExerciseCategoryFilterRequest(
            search = "",
            includePredefined = true,  // Incluir predefinidas
            onlyMine = true,           // Solo las del usuario
            page = 0,
            size = 100,                // Obtener más elementos
            sortBy = "name",
            direction = "ASC"
        )
        categoryViewModel.loadCategoriesForSpinner(filterRequest)
    }

    private fun setupObservers() {
        // Observar deportes
        sportViewModel.allSportsState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    Log.d("CreateCategory", "Deportes cargados: ${resource.data?.size ?: 0}")
                    resource.data?.let { sports ->
                        updateSportsSpinner(sports)
                    }
                }
                is Resource.Error -> {
                    Log.e("CreateCategory", "Error al cargar deportes: ${resource.message}")
                    Toast.makeText(
                        requireContext(),
                        "Error al cargar deportes: ${resource.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Usar lista vacía si hay error
                    updateSportsSpinner(emptyList())
                }
                else -> {
                    // Loading state
                }
            }
        })

        // Observar categorías disponibles para padre
        categoryViewModel.categoriesForSpinnerState.observe(viewLifecycleOwner, Observer { resource ->
            Log.d("CreateCategory", "Observer categorías spinner: $resource")
            when (resource) {
                is Resource.Success -> {
                    Log.d("CreateCategory", "Categorías cargadas: ${resource.data?.size ?: 0}")
                    resource.data?.let { categories ->
                        if (categories.isNotEmpty()) {
                            Log.d("CreateCategory", "Mostrando ${categories.size} categorías en spinner")
                            categories.forEach { cat ->
                                Log.d("CreateCategory", "Categoría: ${cat.name} (id: ${cat.id})")
                            }
                        }
                        updateParentCategorySpinner(categories)
                    }
                }
                is Resource.Error -> {
                    Log.e("CreateCategory", "Error al cargar categorías: ${resource.message}")
                    Toast.makeText(
                        requireContext(),
                        "Error al cargar categorías: ${resource.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Usar lista vacía si hay error
                    updateParentCategorySpinner(emptyList())
                }
                is Resource.Loading -> {
                    Log.d("CreateCategory", "Cargando categorías...")
                }
                else -> {}
            }
        })

        // Observar estado de creación
        categoryViewModel.createCategoryState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    Toast.makeText(
                        requireContext(),
                        "✅ Categoría creada exitosamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(
                        requireContext(),
                        "❌ Error: ${resource.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is Resource.Loading -> {
                    showLoading()
                }
                else -> {}
            }
        })
    }

    private fun updateSportsSpinner(sports: List<SportResponse>) {
        sportsMap.clear()
        val sportNames = mutableListOf<String>()

        // Agregar opción vacía
        val emptyOption = "Seleccionar deporte (opcional)"
        sportNames.add(emptyOption)
        sportsMap[emptyOption] = 0

        sports.forEach { sport ->
            val displayName = "${sport.name} (${if (sport.isPredefined) "Predefinido" else "Personalizado"})"
            sportNames.add(displayName)
            sportsMap[displayName] = sport.id
        }

        Log.d("CreateCategory", "Actualizando spinner deportes con ${sportNames.size} elementos")

        // Usar layout personalizado
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, sportNames)
        binding.spinnerSports.setAdapter(adapter)

        // Establecer texto inicial
        binding.spinnerSports.setText(emptyOption, false)

        // Configurar threshold para mostrar sugerencias
        binding.spinnerSports.threshold = 1

        // Configurar listener para selección
        binding.spinnerSports.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = binding.spinnerSports.adapter.getItem(position) as String
            selectedSportId = sportsMap[selectedItem]?.takeIf { it > 0 }
            Log.d("CreateCategory", "Deporte seleccionado: $selectedItem -> $selectedSportId")
        }

        // Permitir que se pueda hacer clic para abrir el dropdown
        binding.spinnerSports.setOnClickListener {
            binding.spinnerSports.showDropDown()
        }
    }

    private fun updateParentCategorySpinner(categories: List<com.fitapp.appfit.response.category.response.ExerciseCategoryResponse>) {
        parentCategoriesMap.clear()
        val categoryNames = mutableListOf<String>()

        // Agregar opción vacía
        val emptyOption = "Seleccionar categoría padre (opcional)"
        categoryNames.add(emptyOption)
        parentCategoriesMap[emptyOption] = 0

        if (categories.isNotEmpty()) {
            categories.forEach { category ->
                val displayName = "${category.name} (${if (category.isPredefined) "Predefinida" else "Personal"})"
                categoryNames.add(displayName)
                parentCategoriesMap[displayName] = category.id
            }
        } else {
            Log.w("CreateCategory", "No hay categorías disponibles para mostrar")
        }

        Log.d("CreateCategory", "Actualizando spinner categorías padre con ${categoryNames.size} elementos")

        // Usar layout personalizado
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, categoryNames)
        binding.spinnerParentCategory.setAdapter(adapter)

        // Establecer texto inicial
        binding.spinnerParentCategory.setText(emptyOption, false)

        // Configurar threshold para mostrar sugerencias
        binding.spinnerParentCategory.threshold = 1

        // Configurar listener para selección
        binding.spinnerParentCategory.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = binding.spinnerParentCategory.adapter.getItem(position) as String
            selectedParentCategoryId = parentCategoriesMap[selectedItem]?.takeIf { it > 0 }
            Log.d("CreateCategory", "Categoría padre seleccionada: $selectedItem -> $selectedParentCategoryId")
        }

        // Permitir que se pueda hacer clic para abrir el dropdown
        binding.spinnerParentCategory.setOnClickListener {
            binding.spinnerParentCategory.showDropDown()
        }
    }

    private fun createCategory() {
        val name = binding.etName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        // Validaciones básicas
        if (name.isEmpty()) {
            binding.etName.error = "El nombre es obligatorio"
            return
        }

        if (name.length > 100) {
            binding.etName.error = "El nombre no puede tener más de 100 caracteres"
            return
        }

        if (description.length > 500) {
            binding.etDescription.error = "La descripción no puede tener más de 500 caracteres"
            return
        }

        // Crear objeto de solicitud
        val categoryRequest = ExerciseCategoryRequest(
            name = name,
            description = if (description.isNotEmpty()) description else null,
            isPublic = false,
            sportId = selectedSportId,
            parentCategoryId = selectedParentCategoryId
        )

        Log.d("CreateCategory", "Creando categoría: $categoryRequest")
        // Llamar al ViewModel para crear la categoría
        categoryViewModel.createCategory(categoryRequest)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false
        binding.spinnerSports.isEnabled = false
        binding.spinnerParentCategory.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnSave.isEnabled = true
        binding.spinnerSports.isEnabled = true
        binding.spinnerParentCategory.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        categoryViewModel.clearCreateState()
        categoryViewModel.clearSpinnerState()
    }
}