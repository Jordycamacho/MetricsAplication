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
import com.fitapp.appfit.databinding.FragmentMyPackagesBinding
import com.fitapp.appfit.feature.marketplace.ui.MarketplaceViewModel

class MyPackagesFragment : Fragment() {

    private var _binding: FragmentMyPackagesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MarketplaceViewModel
    private lateinit var adapter: PackageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPackagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(MarketplaceViewModel::class.java)

        setupRecyclerView()
        observeViewModel()
        setupListeners()

        // TODO: obtener userId del SessionManager
        val userId = 1L // Placeholder
        viewModel.getUserPackages(userId)
    }

    private fun setupRecyclerView() {
        adapter = PackageAdapter(
            onPackageClick = { pkg ->
                val bundle = Bundle().apply {
                    putLong("packageId", pkg.id)
                }
                findNavController().navigate(
                    R.id.action_my_packages_to_edit_package,
                    bundle
                )
            },
            onDownloadClick = { }
        )
        binding.rvMyPackages.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MyPackagesFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.userPackagesState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvMyPackages.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvMyPackages.visibility = View.VISIBLE
                    state.data?.content?.let { adapter.submitList(it) }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnCreatePackage.setOnClickListener {
            findNavController().navigate(
                R.id.action_my_packages_to_create_package
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}