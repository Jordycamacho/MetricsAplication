package com.fitapp.appfit.feature.marketplace.ui.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentAddPackageItemsBinding
import com.fitapp.appfit.feature.marketplace.model.request.AddPackageItemRequest
import com.fitapp.appfit.feature.marketplace.ui.MarketplaceViewModel
import com.fitapp.appfit.feature.marketplace.ui.detail.PackageItemAdapter

class AddPackageItemsFragment : Fragment() {

    private var _binding: FragmentAddPackageItemsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MarketplaceViewModel
    private lateinit var itemAdapter: PackageItemAdapter
    private var packageId: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPackageItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(MarketplaceViewModel::class.java)
        packageId = arguments?.getLong("packageId") ?: return

        setupRecyclerView()
        observeViewModel()
        setupListeners()

        // Cargar package
        viewModel.getPackageById(packageId)
    }

    private fun setupRecyclerView() {
        itemAdapter = PackageItemAdapter()
        binding.rvPackageItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.detailState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> {
                    state.data?.let { pkg ->
                        binding.tvPackageName.text = pkg.name
                        pkg.items?.let { itemAdapter.submitList(it) }
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        viewModel.addItemState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Item agregado", Toast.LENGTH_SHORT).show()
                    // Recargar package
                    viewModel.getPackageById(packageId)
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            btnAddRoutine.setOnClickListener {
                showItemSelector("ROUTINE")
            }

            btnAddExercise.setOnClickListener {
                showItemSelector("EXERCISE")
            }

            btnAddSport.setOnClickListener {
                showItemSelector("SPORT")
            }

            btnAddParameter.setOnClickListener {
                showItemSelector("PARAMETER")
            }

            btnFinish.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun showItemSelector(itemType: String) {
        // TODO: implementar selector según tipo
        // Por ahora solo mostramos un toast
        Toast.makeText(requireContext(), "Selector de $itemType", Toast.LENGTH_SHORT).show()

        // Ejemplo de agregar un item (reemplazar con selector real)
        val request = AddPackageItemRequest(
            itemType = itemType,
            sportId = if (itemType == "SPORT") 1L else null,
            parameterId = if (itemType == "PARAMETER") 1L else null,
            routineId = if (itemType == "ROUTINE") 1L else null,
            exerciseId = if (itemType == "EXERCISE") 1L else null,
            categoryId = null,
            displayOrder = null,
            notes = null
        )
        viewModel.addItemToPackage(packageId, request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}