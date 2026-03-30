package com.fitapp.appfit.feature.marketplace.ui.detail

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
import com.fitapp.appfit.databinding.FragmentPackageDetailBinding
import com.fitapp.appfit.feature.marketplace.ui.MarketplaceViewModel

class PackageDetailFragment : Fragment() {

    private var _binding: FragmentPackageDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MarketplaceViewModel
    private lateinit var itemAdapter: PackageItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPackageDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(MarketplaceViewModel::class.java)

        setupRecyclerView()
        observeViewModel()

        val packageId = arguments?.getLong("packageId") ?: return
        viewModel.getPackageById(packageId)
        viewModel.getStatistics(packageId)
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

        viewModel.statisticsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> {
                    state.data?.let { stats ->
                        binding.tvDownloadCount.text = "${stats.totalDownloads} descargas"
                        binding.tvRatingCount.text = "${stats.totalRatings} valoraciones"
                    }
                }
                else -> {}
            }
        }

        viewModel.downloadState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Paquete descargado", Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        viewModel.rateState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Valoración registrada", Toast.LENGTH_SHORT).show()
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
            // Header
            tvPackageName.text = pkg.name
            tvDescription.text = pkg.description ?: "Sin descripción"
            tvCreator.text = "Por: ${pkg.createdBy?.username ?: "Oficial"}"

            // Status y version
            tvStatus.text = "Estado: ${pkg.status}"
            tvVersion.text = "v${pkg.version}"

            // Precio
            if (pkg.isFree) {
                tvPrice.text = "GRATIS"
                tvPrice.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
            } else {
                tvPrice.text = "${pkg.price} ${pkg.currency ?: "USD"}"
                tvPrice.setTextColor(requireContext().getColor(android.R.color.holo_orange_dark))
            }

            // Rating
            ratingBar.rating = (pkg.rating ?: 0.0).toFloat()

            // Items
            tvItemsCount.text = "${pkg.items?.size ?: 0} items"
            pkg.items?.let { itemAdapter.submitList(it) }

            // Botones
            btnDownload.setOnClickListener {
                viewModel.downloadPackage(pkg.id)
            }

            btnRate.setOnClickListener {
                ratePackage(pkg.id)
            }
        }
    }

    private fun ratePackage(packageId: Long) {
        val rating = binding.ratingBar.rating.toDouble()
        if (rating > 0) {
            viewModel.ratePackage(packageId, rating)
        } else {
            Toast.makeText(requireContext(), "Selecciona una calificación", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}