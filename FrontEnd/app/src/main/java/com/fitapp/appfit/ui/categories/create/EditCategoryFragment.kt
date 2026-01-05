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
import androidx.navigation.fragment.navArgs
import com.fitapp.appfit.R
import com.fitapp.appfit.databinding.FragmentEditCategoryBinding
import com.fitapp.appfit.model.ExerciseCategoryViewModel
import com.fitapp.appfit.model.SportViewModel
import com.fitapp.appfit.response.category.request.ExerciseCategoryFilterRequest
import com.fitapp.appfit.response.category.request.ExerciseCategoryRequest
import com.fitapp.appfit.response.sport.response.SportResponse
import com.fitapp.appfit.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EditCategoryFragment : Fragment() {

    private var _binding: FragmentEditCategoryBinding? = null
    private val binding get() = _binding!!
    private val categoryViewModel: ExerciseCategoryViewModel by viewModels()
    private val sportViewModel: SportViewModel by viewModels()
    private val args: EditCategoryFragmentArgs by navArgs()

    private val sportsMap = mutableMapOf<String, Long>()
    private val parentCategoriesMap = mutableMapOf<String, Long>()
    private var selectedSportId: Long? = null
    private var selectedParentCategoryId: Long? = null
    private var currentCategoryId: Long = 0
    private var currentCategory: com.fitapp.appfit.response.category.response.ExerciseCategoryResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentCategoryId = args.categoryId

        setupToolbar()
        setupClickListeners()
        setupObservers()

        // Cargar datos iniciales
        loadInitialData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            updateCategory()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun loadInitialData() {
        // Cargar deportes
        sportViewModel.getAllSports()

        // Cargar categorías disponibles para padre
        loadAvailableCategories()

        // Cargar datos de la categoría
        loadCategoryData()
    }

    private fun loadAvailableCategories() {
        Log.d("EditCategory", "Cargando categorías disponibles para padre...")
        val filterRequest = ExerciseCategoryFilterRequest(
            search = "",
            includePredefined = true,
            onlyMine = true,
            page = 0,
            size = 100,
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
                    Log.d("EditCategory", "Deportes cargados: ${resource.data?.size ?: 0}")
                    resource.data?.let { sports ->
                        updateSportsSpinner(sports)
                    }
                }
                is Resource.Error -> {
                    Log.e("EditCategory", "Error al cargar deportes: ${resource.message}")
                    Toast.makeText(
                        requireContext(),
                        "Error al cargar deportes: ${resource.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateSportsSpinner(emptyList())
                }
                else -> {}
            }
        })

        // Observar categorías disponibles para padre
        categoryViewModel.categoriesForSpinnerState.observe(viewLifecycleOwner, Observer { resource ->
            Log.d("EditCategory", "Observer categorías spinner: $resource")
            when (resource) {
                is Resource.Success -> {
                    Log.d("EditCategory", "Categorías cargadas: ${resource.data?.size ?: 0}")
                    resource.data?.let { categories ->
                        if (categories.isNotEmpty()) {
                            Log.d("EditCategory", "Mostrando ${categories.size} categorías en spinner")
                        }
                        updateParentCategorySpinner(categories)

                        // Después de cargar las categorías, seleccionar la padre si existe
                        currentCategory?.parentCategoryId?.let { parentId ->
                            selectParentCategory(parentId, categories)
                        }
                    }
                }
                is Resource.Error -> {
                    Log.e("EditCategory", "Error al cargar categorías: ${resource.message}")
                    Toast.makeText(
                        requireContext(),
                        "Error al cargar categorías: ${resource.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateParentCategorySpinner(emptyList())
                }
                else -> {}
            }
        })

        // Observar estado de detalle
        categoryViewModel.categoryDetailState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    resource.data?.let { category ->
                        currentCategory = category
                        populateCategoryData(category)
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(
                        requireContext(),
                        "❌ Error al cargar la categoría: ${resource.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    findNavController().navigateUp()
                }
                is Resource.Loading -> {
                    showLoading()
                }
                else -> {}
            }
        })

        // Observar estado de actualización
        categoryViewModel.updateCategoryState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    Toast.makeText(
                        requireContext(),
                        "✅ Categoría actualizada exitosamente",
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

        // Observar estado de eliminación
        categoryViewModel.deleteCategoryState.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    Toast.makeText(
                        requireContext(),
                        "🗑️ Categoría eliminada exitosamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(
                        requireContext(),
                        "❌ Error al eliminar: ${resource.message}",
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

        Log.d("EditCategory", "Actualizando spinner deportes con ${sportNames.size} elementos")

        // Usar layout personalizado
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, sportNames)
        binding.spinnerSports.setAdapter(adapter)

        // Establecer texto inicial
        binding.spinnerSports.setText(emptyOption, false)

        // Configurar threshold para mostrar sugerencias
        binding.spinnerSports.threshold = 1

        // Configurar listener
        binding.spinnerSports.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = binding.spinnerSports.adapter.getItem(position) as String
            selectedSportId = sportsMap[selectedItem]?.takeIf { it > 0 }
            Log.d("EditCategory", "Deporte seleccionado: $selectedItem -> $selectedSportId")
        }

        // Permitir clic para abrir dropdown
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
            // Filtrar la categoría actual
            val filteredCategories = categories.filter {
                it.id != currentCategoryId
            }

            filteredCategories.forEach { category ->
                val displayName = "${category.name} (${if (category.isPredefined) "Predefinida" else "Personal"})"
                categoryNames.add(displayName)
                parentCategoriesMap[displayName] = category.id
            }
        } else {
            Log.w("EditCategory", "No hay categorías disponibles para mostrar")
        }

        Log.d("EditCategory", "Actualizando spinner categorías padre con ${categoryNames.size} elementos")

        // Usar layout personalizado
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, categoryNames)
        binding.spinnerParentCategory.setAdapter(adapter)

        // Establecer texto inicial
        binding.spinnerParentCategory.setText(emptyOption, false)

        // Configurar threshold para mostrar sugerencias
        binding.spinnerParentCategory.threshold = 1

        // Configurar listener
        binding.spinnerParentCategory.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = binding.spinnerParentCategory.adapter.getItem(position) as String
            selectedParentCategoryId = parentCategoriesMap[selectedItem]?.takeIf { it > 0 }
            Log.d("EditCategory", "Categoría padre seleccionada: $selectedItem -> $selectedParentCategoryId")
        }

        // Permitir clic para abrir dropdown
        binding.spinnerParentCategory.setOnClickListener {
            binding.spinnerParentCategory.showDropDown()
        }
    }

    private fun loadCategoryData() {
        Log.d("EditCategory", "Cargando categoría con ID: $currentCategoryId")
        categoryViewModel.getCategoryById(currentCategoryId)
    }

    private fun selectSportInSpinner(sportId: Long?) {
        sportId?.let { id ->
            // Buscar el deporte en el mapa por ID
            val sportKey = sportsMap.entries.find { it.value == id }?.key
            sportKey?.let {
                binding.spinnerSports.setText(it, false)
                selectedSportId = id
                Log.d("EditCategory", "Deporte seleccionado en spinner: $it -> $id")
            }
        }
    }

    private fun selectParentCategory(parentId: Long?, categories: List<com.fitapp.appfit.response.category.response.ExerciseCategoryResponse>) {
        parentId?.let { id ->
            // Buscar la categoría padre en la lista original (sin filtrar)
            val parentCategory = categories.find { it.id == id }
            parentCategory?.let { category ->
                val displayName = "${category.name} (${if (category.isPredefined) "Predefinida" else "Personal"})"

                // Verificar si esta displayName está en el mapa
                if (parentCategoriesMap.containsKey(displayName)) {
                    binding.spinnerParentCategory.setText(displayName, false)
                    selectedParentCategoryId = id
                    Log.d("EditCategory", "Categoría padre seleccionada en spinner: $displayName -> $id")
                } else {
                    // Si no está en el mapa, mantener el ID pero mostrar texto vacío
                    selectedParentCategoryId = id
                    Log.d("EditCategory", "Categoría padre no disponible en spinner, manteniendo ID: $id")
                }
            }
        }
    }

    private fun populateCategoryData(category: com.fitapp.appfit.response.category.response.ExerciseCategoryResponse) {
        binding.etName.setText(category.name)
        binding.etDescription.setText(category.description ?: "")

        // Mostrar información de uso
        binding.tvUsageInfo.text = "📊 Usos: ${category.usageCount} veces"
        binding.tvUsageInfo.visibility = View.VISIBLE

        // Guardar los IDs para referencia
        selectedSportId = category.sportId
        selectedParentCategoryId = category.parentCategoryId

        Log.d("EditCategory", "Categoría cargada: ${category.name}")
        Log.d("EditCategory", "sportId: ${category.sportId}, parentCategoryId: ${category.parentCategoryId}")

        // Seleccionar deporte en spinner si existe
        selectSportInSpinner(category.sportId)

        // No permitir editar si es predefinida
        if (category.isPredefined) {
            disableEditing()
            Toast.makeText(
                requireContext(),
                "⚠️ Esta es una categoría predefinida del sistema y no se puede editar",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun disableEditing() {
        binding.etName.isEnabled = false
        binding.etDescription.isEnabled = false
        binding.spinnerSports.isEnabled = false
        binding.spinnerParentCategory.isEnabled = false
        binding.btnSave.visibility = View.GONE
        binding.btnDelete.visibility = View.GONE
    }

    private fun updateCategory() {
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

        Log.d("EditCategory", "Actualizando categoría $currentCategoryId con: $categoryRequest")
        // Llamar al ViewModel para actualizar la categoría
        categoryViewModel.updateCategory(currentCategoryId, categoryRequest)
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar Categoría")
            .setMessage("¿Estás seguro de que quieres eliminar esta categoría?\n\n⚠️ Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { dialog, _ ->
                categoryViewModel.deleteCategory(currentCategoryId)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false
        binding.btnDelete.isEnabled = false
        binding.spinnerSports.isEnabled = false
        binding.spinnerParentCategory.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnSave.isEnabled = true
        binding.btnDelete.isEnabled = true
        binding.spinnerSports.isEnabled = true
        binding.spinnerParentCategory.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        categoryViewModel.clearDetailState()
        categoryViewModel.clearUpdateState()
        categoryViewModel.clearDeleteState()
        categoryViewModel.clearSpinnerState()
    }
}