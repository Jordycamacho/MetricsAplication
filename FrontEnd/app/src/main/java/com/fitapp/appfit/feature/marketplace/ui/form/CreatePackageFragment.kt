package com.fitapp.appfit.feature.marketplace.ui.form

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fitapp.appfit.R
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentCreatePackageBinding
import com.fitapp.appfit.feature.marketplace.model.enums.PackageType
import com.fitapp.appfit.feature.marketplace.model.enums.SubscriptionType
import com.fitapp.appfit.feature.marketplace.model.request.CreatePackageRequest
import com.fitapp.appfit.feature.marketplace.ui.MarketplaceViewModel
import java.math.BigDecimal

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

        setupSpinners()
        observeViewModel()
        setupListeners()
    }

    private fun setupSpinners() {
        // Package Type Spinner
        val packageTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            PackageType.values().map { it.displayName }
        )
        binding.spinnerPackageType.setAdapter(packageTypeAdapter)

        // Subscription Type Spinner
        val subscriptionAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            SubscriptionType.values().map { it.displayName }
        )
        binding.spinnerSubscription.setAdapter(subscriptionAdapter)
        binding.spinnerSubscription.setText(SubscriptionType.FREE.displayName, false)
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
                        Toast.makeText(requireContext(), "Paquete creado exitosamente", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(requireContext(), "Error: ${state.message}", Toast.LENGTH_LONG).show()
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

            cbIsFree.setOnCheckedChangeListener { _, isChecked ->
                updatePriceVisibility()
            }
        }
    }

    private fun validateInputs(): Boolean {
        with(binding) {
            when {
                etName.text.isNullOrBlank() -> {
                    tilName.error = "El nombre es requerido"
                    return false
                }
                etName.text!!.length < 3 -> {
                    tilName.error = "El nombre debe tener al menos 3 caracteres"
                    return false
                }
                spinnerPackageType.text.isNullOrBlank() -> {
                    Toast.makeText(requireContext(), "Selecciona un tipo de paquete", Toast.LENGTH_SHORT).show()
                    return false
                }
                !cbIsFree.isChecked && etPrice.text.isNullOrBlank() -> {
                    tilPrice.error = "El precio es requerido"
                    return false
                }
                !cbIsFree.isChecked && etPrice.text.toString().toDoubleOrNull() == null -> {
                    tilPrice.error = "Ingresa un precio válido"
                    return false
                }
            }
        }
        return true
    }

    private fun updatePriceVisibility() {
        binding.layoutPrice.visibility =
            if (binding.cbIsFree.isChecked) View.GONE else View.VISIBLE
    }

    private fun createPackage() {
        val packageTypeStr = binding.spinnerPackageType.text.toString()
        val packageType = PackageType.values().find { it.displayName == packageTypeStr }?.name
            ?: return showError("Tipo de paquete inválido")

        val subscriptionStr = binding.spinnerSubscription.text.toString()
        val subscriptionType = SubscriptionType.values().find { it.displayName == subscriptionStr }?.name
            ?: SubscriptionType.FREE.name

        val price = if (binding.cbIsFree.isChecked) {
            null
        } else {
            binding.etPrice.text?.toString()?.toDoubleOrNull()?.let { BigDecimal.valueOf(it) }
        }

        val tagsStr = binding.etTags.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        val tagsJson = tagsStr?.let {
            val tagList = it.split(",").map { tag -> tag.trim() }
            com.google.gson.Gson().toJson(tagList)
        }

        val request = CreatePackageRequest(
            name = binding.etName.text?.toString()?.trim() ?: "",
            description = binding.etDescription.text?.toString()?.trim()?.ifEmpty { null },
            packageType = packageType,
            isFree = binding.cbIsFree.isChecked,
            price = price as Double?,
            currency = binding.etCurrency.text?.toString()?.trim()?.ifEmpty { "USD" } ?: "USD",
            requiresSubscription = subscriptionType,
            thumbnailUrl = null,
            tags = tagsJson,
            initialItems = emptyList()
        )

        android.util.Log.d("CreatePackage", "Request JSON: ${com.google.gson.Gson().toJson(request)}")

        viewModel.createPackage(request)
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}