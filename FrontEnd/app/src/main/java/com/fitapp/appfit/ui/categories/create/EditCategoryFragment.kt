package com.fitapp.appfit.ui.categories.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fitapp.appfit.R
import com.fitapp.appfit.databinding.FragmentEditCategoryBinding
import com.fitapp.appfit.model.ExerciseCategoryViewModel
import com.fitapp.appfit.model.SportViewModel
import com.fitapp.appfit.response.category.request.ExerciseCategoryRequest
import com.fitapp.appfit.response.category.response.ExerciseCategoryResponse
import com.fitapp.appfit.response.sport.response.SportResponse
import com.fitapp.appfit.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EditCategoryFragment : Fragment() {

    private var _binding: FragmentEditCategoryBinding? = null
    private val binding get() = _binding!!

    private val categoryViewModel: ExerciseCategoryViewModel by viewModels()
    private val sportViewModel: SportViewModel by viewModels()
    private val args: EditCategoryFragmentArgs by navArgs()

    private val sportsMap = mutableMapOf<String, Long?>()
    private var selectedSportId: Long? = null
    private var currentCategory: ExerciseCategoryResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        binding.btnSave.setOnClickListener { updateCategory() }
        binding.btnDelete.setOnClickListener { showDeleteConfirmation() }
        setupObservers()
        sportViewModel.getAllSports()
        categoryViewModel.getCategoryById(args.categoryId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupObservers() {
        sportViewModel.allSportsState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> resource.data?.let { sports ->
                    updateSportsSpinner(sports)
                    currentCategory?.sportId?.let { selectSport(it) }
                }
                is Resource.Error -> updateSportsSpinner(emptyList())
                else -> {}
            }
        }

        categoryViewModel.categoryDetailState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    resource.data?.let { populateData(it) }
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "No se pudo cargar la categoría", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Loading -> showLoading()
                else -> {}
            }
        }

        categoryViewModel.updateCategoryState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Cambios guardados ✓", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    hideLoading()
                    val msg = when {
                        resource.message?.contains("409") == true ||
                                resource.message?.contains("DUPLICATE") == true ->
                            "Ya tienes una categoría con ese nombre"
                        resource.message?.contains("403") == true ->
                            "No tienes permiso para editar esta categoría"
                        else -> resource.message ?: "Error al guardar"
                    }
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                }
                is Resource.Loading -> showLoading()
                else -> {}
            }
        }

        categoryViewModel.deleteCategoryState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Categoría eliminada", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), resource.message ?: "Error al eliminar", Toast.LENGTH_LONG).show()
                }
                is Resource.Loading -> showLoading()
                else -> {}
            }
        }
    }

    private fun populateData(category: ExerciseCategoryResponse) {
        currentCategory = category
        binding.etName.setText(category.name)
        selectedSportId = category.sportId
        category.sportId?.let { selectSport(it) }

        if (category.isPredefined) {
            disableEditing()
            Toast.makeText(requireContext(), "Las categorías del sistema no se pueden modificar", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateSportsSpinner(sports: List<SportResponse>) {
        sportsMap.clear()
        val names = mutableListOf("Sin deporte específico")
        sportsMap["Sin deporte específico"] = null

        sports.forEach { sport ->
            names.add(sport.name)
            sportsMap[sport.name] = sport.id
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, names)
        binding.spinnerSports.setAdapter(adapter)
        binding.spinnerSports.setText("Sin deporte específico", false)

        binding.spinnerSports.setOnItemClickListener { _, _, position, _ ->
            val selected = binding.spinnerSports.adapter.getItem(position) as String
            selectedSportId = sportsMap[selected]
        }
        binding.spinnerSports.setOnClickListener { binding.spinnerSports.showDropDown() }
    }

    private fun selectSport(sportId: Long) {
        selectedSportId = sportId
        val key = sportsMap.entries.find { it.value == sportId }?.key ?: return
        binding.spinnerSports.setText(key, false)
    }

    private fun updateCategory() {
        val name = binding.etName.text?.toString()?.trim() ?: ""

        if (name.isEmpty()) {
            binding.etName.error = "El nombre es obligatorio"
            return
        }
        if (name.length > 100) {
            binding.etName.error = "Máximo 100 caracteres"
            return
        }

        categoryViewModel.updateCategory(
            args.categoryId,
            ExerciseCategoryRequest(
                name = name,
                description = null,
                // Mantener la visibilidad original del servidor, no la cambiamos desde aquí
                isPublic = currentCategory?.isPublic ?: false,
                sportId = selectedSportId,
                parentCategoryId = null
            )
        )
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar categoría")
            .setMessage("¿Eliminar \"${currentCategory?.name}\"? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { dialog, _ ->
                categoryViewModel.deleteCategory(args.categoryId)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun disableEditing() {
        binding.etName.isEnabled = false
        binding.spinnerSports.isEnabled = false
        binding.btnSave.visibility = View.GONE
        binding.btnDelete.visibility = View.GONE
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false
        binding.btnDelete.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnSave.isEnabled = true
        binding.btnDelete.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        categoryViewModel.clearDetailState()
        categoryViewModel.clearUpdateState()
        categoryViewModel.clearDeleteState()
    }
}