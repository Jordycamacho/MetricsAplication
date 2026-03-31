package com.fitapp.appfit.feature.marketplace.ui.form

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentEditPackageBinding
import com.fitapp.appfit.feature.marketplace.model.enums.PackageStatus
import com.fitapp.appfit.feature.marketplace.model.request.UpdatePackageRequest
import com.fitapp.appfit.feature.marketplace.model.response.PackageResponse
import com.fitapp.appfit.feature.marketplace.ui.MarketplaceViewModel

class EditPackageFragment : Fragment() {

    private var _binding: FragmentEditPackageBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MarketplaceViewModel
    private var packageId: Long = 0L
    private var currentPackage: PackageResponse? = null

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
        binding.progressBar.visibility = View.VISIBLE
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
                    state.data?.let {
                        currentPackage = it
                        renderPackage(it)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error: ${state.message}", Toast.LENGTH_LONG).show()
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
                    Toast.makeText(requireContext(), "Paquete actualizado correctamente", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    binding.btnUpdate.isEnabled = true
                    Toast.makeText(requireContext(), "Error: ${state.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.publishState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.btnPublish.isEnabled = false
                }
                is Resource.Success -> {
                    binding.btnPublish.isEnabled = true
                    Toast.makeText(requireContext(), "Paquete publicado", Toast.LENGTH_SHORT).show()
                    loadPackage()
                }
                is Resource.Error -> {
                    binding.btnPublish.isEnabled = true
                    Toast.makeText(requireContext(), "Error: ${state.message}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }

        viewModel.deleteState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.btnDelete.isEnabled = false
                }
                is Resource.Success -> {
                    binding.btnDelete.isEnabled = true
                    Toast.makeText(requireContext(), "Paquete eliminado", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    binding.btnDelete.isEnabled = true
                    Toast.makeText(requireContext(), "Error: ${state.message}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun renderPackage(pkg: PackageResponse) {
        with(binding) {
            etName.setText(pkg.name)
            etDescription.setText(pkg.description)
            etPrice.setText(pkg.price?.toString() ?: "")
            etCurrency.setText(pkg.currency ?: "USD")
            etTags.setText(pkg.tags)
            etChangelog.setText(pkg.changelog)
            cbIsFree.isChecked = pkg.isFree

            // Status Badge
            val statusEnum = PackageStatus.fromString(pkg.status)
            tvStatus.text = "Estado: ${statusEnum?.displayName ?: pkg.status}"
            tvStatus.setTextColor(
                android.graphics.Color.parseColor(statusEnum?.color ?: "#FFFFFF")
            )

            tvVersion.text = "v${pkg.version}"

            // Mostrar botones según estado
            when (PackageStatus.fromString(pkg.status)) {
                PackageStatus.DRAFT -> {
                    btnPublish.visibility = View.VISIBLE
                    btnDelete.visibility = View.VISIBLE
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
            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            btnUpdate.setOnClickListener {
                updatePackage()
            }

            btnPublish.setOnClickListener {
                confirmPublish()
            }

            btnDelete.setOnClickListener {
                confirmDelete()
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
            name = binding.etName.text.toString().trim().ifEmpty { null },
            description = binding.etDescription.text.toString().trim().ifEmpty { null },
            isFree = binding.cbIsFree.isChecked,
            price = if (binding.cbIsFree.isChecked) null
            else binding.etPrice.text.toString().toDoubleOrNull(),
            currency = binding.etCurrency.text.toString().trim().ifEmpty { null },
            requiresSubscription = null,
            thumbnailUrl = null,
            tags = binding.etTags.text.toString().trim().ifEmpty { null },
            changelog = binding.etChangelog.text.toString().trim().ifEmpty { null }
        )
        viewModel.updatePackage(packageId, request)
    }

    private fun confirmPublish() {
        AlertDialog.Builder(requireContext())
            .setTitle("Publicar Paquete")
            .setMessage("¿Deseas publicar este paquete? Una vez publicado, estará disponible en el marketplace.")
            .setPositiveButton("Publicar") { _, _ ->
                viewModel.publishPackage(packageId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmDelete() {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Paquete")
            .setMessage("¿Estás seguro? Solo se pueden eliminar paquetes en estado DRAFT. Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deletePackage(packageId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}