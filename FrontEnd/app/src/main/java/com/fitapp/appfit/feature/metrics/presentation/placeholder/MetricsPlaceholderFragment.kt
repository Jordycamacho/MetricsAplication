package com.fitapp.appfit.feature.metrics.presentation.placeholder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fitapp.appfit.databinding.FragmentMetricsPlaceholderBinding

class MetricsPlaceholderFragment : Fragment() {

    private var _binding: FragmentMetricsPlaceholderBinding? = null
    private val binding get() = _binding!!
    private val args: MetricsPlaceholderFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMetricsPlaceholderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.title = args.title
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.tvMessage.text = args.message
        binding.tvBadge.text = args.badge
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
