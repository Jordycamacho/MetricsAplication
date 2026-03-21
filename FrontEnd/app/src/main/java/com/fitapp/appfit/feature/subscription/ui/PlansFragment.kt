package com.fitapp.appfit.feature.subscription.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fitapp.appfit.R
import com.fitapp.appfit.databinding.FragmentPlansBinding
import com.fitapp.appfit.databinding.ItemPlanFeatureBinding
import com.fitapp.appfit.feature.subscription.SubscriptionViewModel
import com.fitapp.appfit.feature.subscription.model.response.SubscriptionResponse
import com.fitapp.appfit.core.util.Resource

class PlansFragment : Fragment() {

    private var _binding: FragmentPlansBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SubscriptionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlansBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupFeatures()
        setupObservers()
        viewModel.loadSubscription()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {
        viewModel.subscriptionState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                resource.data?.let { highlightCurrentPlan(it) }
            }
        }
    }

    private fun setupFeatures() {
        addFeatures(
            binding.containerFeaturesFree,
            listOf(
                "2 rutinas de entrenamiento",
                "30 días de historial",
                "Analytics básico",
                "Ejercicios predefinidos"
            )
        )

        addFeatures(
            binding.containerFeaturesStandard,
            listOf(
                "10 rutinas de entrenamiento",
                "180 días de historial",
                "Exportar e importar rutinas",
                "Acceso al marketplace",
                "Contenido personalizado ampliado"
            )
        )

        addFeatures(
            binding.containerFeaturesPremium,
            listOf(
                "Rutinas ilimitadas",
                "Historial ilimitado",
                "Analytics avanzado",
                "Marketplace completo + vender",
                "Todo el contenido personalizado",
                "Acceso prioritario a novedades"
            )
        )
    }

    private fun addFeatures(container: LinearLayout, features: List<String>) {
        container.removeAllViews()
        features.forEach { text ->
            val featureBinding = ItemPlanFeatureBinding.inflate(
                layoutInflater, container, false
            )
            featureBinding.tvFeatureText.text = text
            featureBinding.ivFeatureIcon.setImageResource(R.drawable.ic_check)
            featureBinding.ivFeatureIcon.imageTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.gold_primary)
            featureBinding.tvFeatureText.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.text_primary_dark)
            )
            container.addView(featureBinding.root)
        }
    }

    private fun highlightCurrentPlan(sub: SubscriptionResponse) {
        when (sub.type) {
            "FREE" -> {
                binding.cardFree.setBackgroundResource(R.drawable.bg_plan_card_active)
                binding.tvBadgeFree.visibility = View.VISIBLE
            }
            "STANDARD" -> {
                binding.cardStandard.setBackgroundResource(R.drawable.bg_plan_card_active)
                binding.tvBadgeStandard.visibility = View.VISIBLE
                binding.btnStandard.visibility = View.GONE
            }
            "PREMIUM" -> {
                binding.tvBadgePremium.visibility = View.VISIBLE
                binding.btnPremium.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}