package com.fitapp.appfit.feature.marketplace.ui.form

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fitapp.appfit.R
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentCreatePackageBinding
import com.fitapp.appfit.feature.marketplace.model.request.CreatePackageRequest
import com.fitapp.appfit.feature.marketplace.ui.MarketplaceViewModel

class CreatePackageFragment : Fragment() {

    private var _binding: FragmentCreatePackageBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MarketplaceViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePackageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(MarketplaceViewModel::class.java)

        observeViewModel()
        setupListeners()
    }

    private fun observeViewModel() {
        viewModel.createState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnCreate.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    state.data?.let { pkg ->
                        Toast.makeText(requireContext(), "Paquete creado", Toast.LENGTH_SHORT).show()
                        val bundle = Bundle().apply {
                            putLong("packageId", pkg.id)
                        }
                        findNavController().navigate(
                            R.id.action_create_package_to_add_items,
                            bundle
                        )
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCreate.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            btnCreate.setOnClickListener {
                if (validateInputs()) {
                    createPackage()
                }
            }

            spinnerPackageType.setOnItemClickListener { _, _, _, _ ->
                updatePriceVisibility()
            }

            cbIsFree.setOnCheckedChangeListener { _, isChecked ->
                updatePriceVisibility()
            }
        }
    }

    private fun validateInputs(): Boolean {
        with(binding) {
            if (etName.text.isNullOrBlank()) {
                Toast.makeText(requireContext(), "Nombre requerido", Toast.LENGTH_SHORT).show()
                return false
            }
            if (!cbIsFree.isChecked && etPrice.text.isNullOrBlank()) {
                Toast.makeText(requireContext(), "Precio requerido", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    private fun updatePriceVisibility() {
        binding.layoutPrice.visibility =
            if (binding.cbIsFree.isChecked) View.GONE else View.VISIBLE
    }

    private fun createPackage() {
        val request = CreatePackageRequest(
            name = binding.etName.text?.toString() ?: "",
            description = binding.etDescription.text?.toString()?.ifBlank { null },
            packageType = binding.spinnerPackageType.text.toString(),
            isFree = binding.cbIsFree.isChecked,
            price = if (binding.cbIsFree.isChecked) null
            else binding.etPrice.text?.toString()?.toDoubleOrNull(),
            currency = binding.etCurrency.text?.toString()?.ifBlank { "USD" },
            requiresSubscription = binding.spinnerSubscription.text.toString(),
            thumbnailUrl = null,
            tags = binding.etTags.text?.toString()?.ifBlank { null }
        )
        viewModel.createPackage(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}