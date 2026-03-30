package com.fitapp.appfit.feature.marketplace.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitapp.appfit.R
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentMarketplaceBinding
import com.fitapp.appfit.feature.marketplace.ui.MarketplaceViewModel

class MarketplaceFragment : Fragment() {

    private var _binding: FragmentMarketplaceBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MarketplaceViewModel
    private lateinit var adapter: PackageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarketplaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(MarketplaceViewModel::class.java)

        setupRecyclerView()
        observeViewModel()
        setupListeners()

        viewModel.searchMarketplace()
    }

    private fun setupRecyclerView() {
        adapter = PackageAdapter(
            onPackageClick = { pkg ->
                val bundle = Bundle().apply {
                    putLong("packageId", pkg.id)
                }
                findNavController().navigate(
                    R.id.action_navigation_marketplace_to_package_detail,
                    bundle
                )
            },
            onDownloadClick = { pkg ->
                viewModel.downloadPackage(pkg.id)
            }
        )
        binding.rvPackages.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MarketplaceFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.searchState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvPackages.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvPackages.visibility = View.VISIBLE
                    state.data?.content?.let { adapter.submitList(it) }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
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
    }

    private fun setupListeners() {
        // Filtros
        binding.apply {
            btnFilterFree.setOnClickListener {
                viewModel.searchMarketplace(isFree = true)
            }
            btnFilterPremium.setOnClickListener {
                viewModel.searchMarketplace(isFree = false)
            }
            btnFilterTrending.setOnClickListener {
                viewModel.getTrendingPackages()
            }
            btnFilterTopRated.setOnClickListener {
                viewModel.getTopRatedPackages()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}