package com.fitapp.appfit.feature.metrics.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fitapp.appfit.R
import com.fitapp.appfit.databinding.FragmentMetricsBinding

class MetricsFragment : Fragment() {

    private var _binding: FragmentMetricsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMetricsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardWorkoutHistory.setOnClickListener {
            findNavController().navigate(R.id.action_metrics_to_workout_history)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}