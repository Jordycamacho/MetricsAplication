package com.fitapp.appfit.feature.parameter.ui

import android.app.AlertDialog
import android.os.Bundle
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
import com.fitapp.appfit.databinding.FragmentExerciseParamsBinding
import com.fitapp.appfit.feature.parameter.ParameterViewModel
import com.fitapp.appfit.feature.parameter.model.request.CustomParameterFilterRequest
import com.fitapp.appfit.feature.parameter.model.response.CustomParameterPageResponse
import com.fitapp.appfit.feature.parameter.model.response.CustomParameterResponse

class ExerciseParamsFragment : Fragment() {
    private var _binding: FragmentExerciseParamsBinding? = null
    private val binding get() = _binding!!
    private val parameterViewModel: ParameterViewModel by viewModels()
    private lateinit var parameterAdapter: ParameterAdapter

    private var onlyMine = false
    private var selectedType: String? = null
    private var isUpdatingChips = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExerciseParamsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFilterListeners()
        setupObservers()
        setupSearchListener()
        loadParameters()
    }

    private fun setupRecyclerView() {
        parameterAdapter = ParameterAdapter(
            onItemClick = { parameter -> showParameterDetail(parameter) },
            onEditClick = { parameter -> editParameter(parameter) },
            onDeleteClick = { parameter -> showDeleteConfirmation(parameter) }
        )
        binding.recyclerParameters.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = parameterAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupFilterListeners() {
        binding.fabCreateParameter.setOnClickListener {
            findNavController().navigate(R.id.navigation_create_parameter)
        }

        binding.chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isUpdatingChips) { onlyMine = false; loadParameters() }
        }
        binding.chipMy.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isUpdatingChips) { onlyMine = true; loadParameters() }
        }
        binding.chipTypeAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isUpdatingChips) { selectedType = null; loadParameters() }
        }
        binding.chipTypeNumber.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isUpdatingChips) { selectedType = "NUMBER"; loadParameters() }
        }
        binding.chipTypeInteger.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isUpdatingChips) { selectedType = "INTEGER"; loadParameters() }
        }
        binding.chipTypeText.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isUpdatingChips) { selectedType = "TEXT"; loadParameters() }
        }
        binding.chipTypeBoolean.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isUpdatingChips) { selectedType = "BOOLEAN"; loadParameters() }
        }
        binding.chipTypeDuration.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isUpdatingChips) { selectedType = "DURATION"; loadParameters() }
        }
        binding.chipTypeDistance.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isUpdatingChips) { selectedType = "DISTANCE"; loadParameters() }
        }
        binding.chipTypePercentage.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isUpdatingChips) { selectedType = "PERCENTAGE"; loadParameters() }
        }
    }

    private fun setupSearchListener() {
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { loadParameters(); true } else false
        }
    }

    private fun setupObservers() {
        parameterViewModel.allParametersState.observe(viewLifecycleOwner) { resource ->
            resource?.let { handleParametersResponse(it, "No hay parámetros disponibles") }
        }
        parameterViewModel.myParametersState.observe(viewLifecycleOwner) { resource ->
            resource?.let { handleParametersResponse(it, "Aún no has creado parámetros personales") }
        }
        parameterViewModel.deleteParameterState.observe(viewLifecycleOwner) { resource ->
            resource?.let {
                when (it) {
                    is Resource.Success -> { Toast.makeText(requireContext(), "Parámetro eliminado", Toast.LENGTH_SHORT).show(); loadParameters() }
                    is Resource.Error -> Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    else -> {}
                }
            }
        }
    }

    private fun handleParametersResponse(
        resource: Resource<CustomParameterPageResponse>,
        emptyMessage: String
    ) {
        when (resource) {
            is Resource.Success -> {
                hideLoading()
                val parameters = resource.data?.content ?: emptyList()
                if (parameters.isEmpty()) showEmptyState(emptyMessage)
                else { showParametersList(); parameterAdapter.updateList(parameters) }
            }
            is Resource.Error -> {
                hideLoading()
                showEmptyState("Error al cargar")
                Toast.makeText(requireContext(), resource.message ?: "Error", Toast.LENGTH_SHORT).show()
            }
            is Resource.Loading -> showLoading()
        }
    }

    private fun loadParameters() {
        val filterRequest = CustomParameterFilterRequest(
            search = binding.etSearch.text.toString().trim().ifEmpty { null },
            parameterType = selectedType,
            onlyMine = onlyMine,
            page = 0,
            size = 50,
            sortBy = "name",
            direction = "ASC"
        )
        if (onlyMine) parameterViewModel.searchMyParameters(filterRequest)
        else parameterViewModel.searchAllParameters(filterRequest)
    }

    private fun showParameterDetail(parameter: CustomParameterResponse) {
        ParameterDetailBottomSheet.Companion.newInstance(parameter)
            .show(parentFragmentManager, "parameter_detail")
    }

    private fun editParameter(parameter: CustomParameterResponse) {
        if (parameter.isGlobal && parameter.ownerId == null) {
            Toast.makeText(requireContext(), "No se puede editar un parámetro del sistema", Toast.LENGTH_SHORT).show()
            return
        }
        findNavController().navigate(
            R.id.navigation_edit_parameter,
            bundleOf("parameterId" to parameter.id)
        )
    }

    private fun showDeleteConfirmation(parameter: CustomParameterResponse) {
        if (parameter.isGlobal && parameter.ownerId == null) {
            Toast.makeText(requireContext(), "No se puede eliminar un parámetro del sistema", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar parámetro")
            .setMessage("¿Eliminar '${parameter.name}'? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ -> parameterViewModel.deleteParameter(parameter.id) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerParameters.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.GONE
    }

    private fun hideLoading() { binding.progressBar.visibility = View.GONE }

    private fun showParametersList() {
        binding.recyclerParameters.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
    }

    private fun showEmptyState(message: String) {
        binding.recyclerParameters.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.tvEmptyState.text = message
    }

    override fun onResume() { super.onResume(); loadParameters() }
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}