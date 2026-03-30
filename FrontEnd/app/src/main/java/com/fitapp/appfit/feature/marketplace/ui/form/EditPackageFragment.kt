package com.fitapp.appfit.feature.marketplace.ui.form

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentEditPackageBinding
import com.fitapp.appfit.feature.marketplace.model.request.UpdatePackageRequest
import com.fitapp.appfit.feature.marketplace.ui.MarketplaceViewModel

class EditPackageFragment : Fragment() {

    private var _binding: FragmentEditPackageBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MarketplaceViewModel
    private var packageId: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPackageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(MarketplaceViewModel::class.java)

        packageId = arguments?.getLong("packageId") ?: return
        observeViewModel()
        setupListeners()
        loadPackage()
    }

    private fun loadPackage() {
        viewModel.getPackageById(packageId)
    }

    private fun observeViewModel() {
        viewModel.detailState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    state.data?.let { renderPackage(it) }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.updateState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.btnUpdate.isEnabled = false
                }
                is Resource.Success -> {
                    binding.btnUpdate.isEnabled = true
                    Toast.makeText(requireContext(), "Paquete actualizado", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    binding.btnUpdate.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.publishState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Paquete publicado", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        viewModel.deleteState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Paquete eliminado", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun renderPackage(pkg: com.fitapp.appfit.feature.marketplace.model.response.PackageResponse) {
        with(binding) {
            etName.setText(pkg.name)
            etDescription.setText(pkg.description)
            etPrice.setText(pkg.price?.toString() ?: "")
            etCurrency.setText(pkg.currency ?: "USD")
            etTags.setText(pkg.tags)
            etChangelog.setText(pkg.changelog)
            cbIsFree.isChecked = pkg.isFree

            tvStatus.text = "Estado: ${pkg.status}"
            tvVersion.text = "v${pkg.version}"

            // Mostrar botones según estado
            when (pkg.status) {
                "DRAFT" -> {
                    btnPublish.visibility = View.VISIBLE
                    btnDelete.visibility = View.VISIBLE
                }
                "PUBLISHED" -> {
                    btnPublish.visibility = View.GONE
                    btnDelete.visibility = View.GONE
                }
                else -> {
                    btnPublish.visibility = View.GONE
                    btnDelete.visibility = View.GONE
                }
            }

            updatePriceVisibility()
        }
    }

    private fun setupListeners() {
        binding.apply {
            // Configura el listener del ícono de navegación del toolbar
            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            btnUpdate.setOnClickListener {
                updatePackage()
            }

            btnPublish.setOnClickListener {
                viewModel.publishPackage(packageId)
            }

            btnDelete.setOnClickListener {
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar paquete")
                    .setMessage("¿Estás seguro? Solo se pueden eliminar packages en estado DRAFT")
                    .setPositiveButton("Sí") { _, _ ->
                        viewModel.deletePackage(packageId)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }

            cbIsFree.setOnCheckedChangeListener { _, _ ->
                updatePriceVisibility()
            }
        }
    }

    private fun updatePriceVisibility() {
        binding.layoutPrice.visibility =
            if (binding.cbIsFree.isChecked) View.GONE else View.VISIBLE
    }

    private fun updatePackage() {
        val request = UpdatePackageRequest(
            name = binding.etName.text.toString().ifBlank { null },
            description = binding.etDescription.text.toString().ifBlank { null },
            isFree = binding.cbIsFree.isChecked,
            price = if (binding.cbIsFree.isChecked) null
            else binding.etPrice.text.toString().toDoubleOrNull(),
            currency = binding.etCurrency.text.toString().ifBlank { null },
            requiresSubscription = null,
            thumbnailUrl = null,
            tags = binding.etTags.text.toString().ifBlank { null },
            changelog = binding.etChangelog.text.toString().ifBlank { null }
        )
        viewModel.updatePackage(packageId, request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}