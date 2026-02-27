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
import com.fitapp.appfit.R
import com.fitapp.appfit.databinding.FragmentCreateCategoryBinding
import com.fitapp.appfit.model.ExerciseCategoryViewModel
import com.fitapp.appfit.model.SportViewModel
import com.fitapp.appfit.response.category.request.ExerciseCategoryRequest
import com.fitapp.appfit.response.sport.response.SportResponse
import com.fitapp.appfit.utils.Resource

class CreateCategoryFragment : Fragment() {

    private var _binding: FragmentCreateCategoryBinding? = null
    private val binding get() = _binding!!

    private val categoryViewModel: ExerciseCategoryViewModel by viewModels()
    private val sportViewModel: SportViewModel by viewModels()

    private val sportsMap = mutableMapOf<String, Long?>()
    private var selectedSportId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        binding.btnSave.setOnClickListener { createCategory() }
        setupObservers()
        sportViewModel.getAllSports()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {
        sportViewModel.allSportsState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> resource.data?.let { updateSportsSpinner(it) }
                is Resource.Error -> updateSportsSpinner(emptyList())
                else -> {}
            }
        }

        categoryViewModel.createCategoryState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "Categoría creada ✓", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    hideLoading()
                    val msg = when {
                        resource.message?.contains("409") == true ||
                                resource.message?.contains("DUPLICATE") == true ->
                            "Ya tienes una categoría con ese nombre"
                        else -> resource.message ?: "Error al crear"
                    }
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                }
                is Resource.Loading -> showLoading()
                else -> {}
            }
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

    private fun createCategory() {
        val name = binding.etName.text?.toString()?.trim() ?: ""

        if (name.isEmpty()) {
            binding.etName.error = "El nombre es obligatorio"
            return
        }
        if (name.length > 100) {
            binding.etName.error = "Máximo 100 caracteres"
            return
        }

        categoryViewModel.createCategory(
            ExerciseCategoryRequest(
                name = name,
                description = null,
                isPublic = false,      // todas privadas por defecto
                sportId = selectedSportId,
                parentCategoryId = null
            )
        )
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnSave.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        categoryViewModel.clearCreateState()
    }
}