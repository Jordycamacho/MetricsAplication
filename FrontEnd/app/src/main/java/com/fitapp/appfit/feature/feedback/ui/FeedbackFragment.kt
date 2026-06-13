package com.fitapp.appfit.feature.feedback.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fitapp.appfit.R
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.databinding.FragmentFeedbackBinding
import com.fitapp.appfit.feature.feedback.FeedbackViewModel
import com.fitapp.appfit.feature.feedback.model.request.CreateFeedbackRequest
import com.fitapp.appfit.feature.feedback.util.FeedbackTechnicalContext
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FeedbackFragment : Fragment() {

    private var _binding: FragmentFeedbackBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FeedbackViewModel by viewModels()

    private val categoryChipIds = mapOf(
        R.id.chip_cat_workout to "WORKOUT",
        R.id.chip_cat_sync to "SYNC",
        R.id.chip_cat_routines to "ROUTINES",
        R.id.chip_cat_subscription to "SUBSCRIPTION",
        R.id.chip_cat_metrics to "METRICS",
        R.id.chip_cat_other to "OTHER"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupForm()
        setupObservers()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupForm() {
        binding.chipGroupType.check(R.id.chip_type_bug)
        updateStepsVisibility(isBug = true)

        binding.chipGroupType.setOnCheckedStateChangeListener { _, checkedIds ->
            val isBug = checkedIds.contains(R.id.chip_type_bug)
            updateStepsVisibility(isBug)
        }

        binding.btnSubmit.setOnClickListener { submitFeedback() }
    }

    private fun updateStepsVisibility(isBug: Boolean) {
        binding.tilSteps.isVisible = isBug
    }

    private fun setupObservers() {
        viewModel.submitState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.btnSubmit.isEnabled = false
                    binding.btnSubmit.text = getString(R.string.feedback_submitting)
                }
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    binding.btnSubmit.isEnabled = true
                    binding.btnSubmit.text = getString(R.string.feedback_submit_uppercase)
                    showSuccessDialog(resource.data!!.id)
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    binding.btnSubmit.isEnabled = true
                    binding.btnSubmit.text = getString(R.string.feedback_submit_uppercase)
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.feedback_error_title)
                        .setMessage(resource.message ?: getString(R.string.feedback_error_generic))
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                }
            }
        }
    }

    private fun submitFeedback() {
        val isBug = binding.chipGroupType.checkedChipId == R.id.chip_type_bug
        val message = binding.etMessage.text?.toString()?.trim().orEmpty()
        val title = binding.etTitle.text?.toString()?.trim().orEmpty()
        val steps = binding.etSteps.text?.toString()?.trim().orEmpty()

        binding.tilMessage.error = null

        if (message.length < 10) {
            binding.tilMessage.error = getString(R.string.feedback_message_min_length)
            return
        }

        val category = categoryChipIds[binding.chipGroupCategory.checkedChipId] ?: "OTHER"
        val includeTechnical = binding.switchTechnical.isChecked

        val request = CreateFeedbackRequest(
            type = if (isBug) "BUG" else "SUGGESTION",
            category = category,
            title = title.ifBlank { null },
            message = message,
            stepsToReproduce = if (isBug && steps.isNotBlank()) steps else null,
            includeTechnicalContext = includeTechnical,
            technicalContext = if (includeTechnical) {
                FeedbackTechnicalContext.collect(requireContext())
            } else {
                null
            }
        )

        viewModel.submitFeedback(request)
    }

    private fun showSuccessDialog(feedbackId: Long) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.feedback_success_title)
            .setMessage(getString(R.string.feedback_success_message, feedbackId))
            .setPositiveButton(android.R.string.ok) { _, _ ->
                findNavController().navigateUp()
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
