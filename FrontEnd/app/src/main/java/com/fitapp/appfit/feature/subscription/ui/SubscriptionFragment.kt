package com.fitapp.appfit.feature.subscription.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fitapp.appfit.R
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentSubscriptionBinding
import com.fitapp.appfit.feature.subscription.model.response.SubscriptionResponse
import com.fitapp.appfit.feature.subscription.SubscriptionViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class SubscriptionFragment : Fragment() {

    private var _binding: FragmentSubscriptionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SubscriptionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupListeners()
        viewModel.loadSubscription()
    }

    private fun setupObservers() {
        viewModel.subscriptionState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading(true)
                is Resource.Success -> {
                    showLoading(false)
                    resource.data?.let { populateSubscription(it) }
                }
                is Resource.Error -> {
                    showLoading(false)
                    showSnackbar(resource.message ?: "Error al cargar suscripción")
                }
            }
        }

        viewModel.cancelState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    showSnackbar("Suscripción cancelada. Seguirás con acceso hasta que expire.")
                    viewModel.loadSubscription()
                }
                is Resource.Error -> showSnackbar(resource.message ?: "Error al cancelar")
                else -> {}
            }
        }
    }

    private fun setupListeners() {
        binding.btnCancelSubscription.setOnClickListener {
            confirmCancel()
        }

        binding.btnViewPlans.setOnClickListener {
            findNavController().navigate(R.id.navigation_plans)
        }
    }

    private fun populateSubscription(sub: SubscriptionResponse) {
        binding.tvCurrentPlan.text = sub.type
        binding.tvPlanStatus.text = sub.status ?: "ACTIVE"

        binding.tvStartDate.text = sub.startDate ?: "-"
        binding.tvEndDate.text = sub.endDate ?: "-"

        binding.tvMaxRoutines.text = if (sub.maxRoutines == null) "∞" else sub.maxRoutines.toString()

        setFeature(binding.tvFeatureAnalytics, "Analytics avanzado", sub.advancedAnalytics)
        setFeature(binding.tvFeatureExport, "Exportar rutinas", sub.canExportRoutines)
        setFeature(binding.tvFeatureMarketplace, "Marketplace", sub.canAccessMarketplace)

        val planColor = when (sub.type) {
            "PREMIUM" -> ContextCompat.getColor(requireContext(), R.color.gold_primary)
            "STANDARD" -> ContextCompat.getColor(requireContext(), R.color.blue_500)
            else -> ContextCompat.getColor(requireContext(), R.color.text_secondary_dark)
        }
        binding.tvCurrentPlan.setTextColor(planColor)

        binding.btnCancelSubscription.visibility =
            if (sub.type == "FREE") View.GONE else View.VISIBLE

        highlightCurrentPlan(sub.type)
    }

    private fun setFeature(view: TextView, label: String, enabled: Boolean) {
        view.text = if (enabled) "✓  $label" else "✗  $label"
        view.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (enabled) R.color.gold_primary else R.color.text_secondary_dark
            )
        )
    }

    private fun highlightCurrentPlan(currentType: String) {
        val freeBorder = if (currentType == "FREE") R.drawable.bg_plan_card_active else R.drawable.bg_plan_card
        val stdBorder  = if (currentType == "STANDARD") R.drawable.bg_plan_card_active else R.drawable.bg_plan_card
        val premBorder = if (currentType == "PREMIUM") R.drawable.bg_plan_card_active else R.drawable.bg_plan_card

        binding.cardFree.setBackgroundResource(freeBorder)
        binding.cardStandard.setBackgroundResource(stdBorder)
        binding.cardPremium.setBackgroundResource(premBorder)

        binding.tvBadgeFree.visibility     = if (currentType == "FREE")     View.VISIBLE else View.GONE
        binding.tvBadgeStandard.visibility = if (currentType == "STANDARD") View.VISIBLE else View.GONE
        binding.tvBadgePremium.visibility  = if (currentType == "PREMIUM")  View.VISIBLE else View.GONE

        binding.btnUpgradeFree.visibility     = if (currentType == "FREE")     View.GONE else View.VISIBLE
        binding.btnUpgradeStandard.visibility = if (currentType == "STANDARD") View.GONE else View.VISIBLE
        binding.btnUpgradePremium.visibility  = if (currentType == "PREMIUM")  View.GONE else View.VISIBLE
    }

    private fun confirmCancel() {
        MaterialAlertDialogBuilder(requireContext(), R.style.DarkAlertDialog)
            .setTitle("Cancelar suscripción")
            .setMessage("Tu plan seguirá activo hasta la fecha de vencimiento. ¿Deseas continuar?")
            .setPositiveButton("Cancelar suscripción") { _, _ ->
                viewModel.cancelSubscription("Cancelación solicitada por el usuario")
            }
            .setNegativeButton("Volver", null)
            .show()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.scrollContent.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}