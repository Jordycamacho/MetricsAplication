package com.fitapp.appfit.feature.exercise.ui.list

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.databinding.ItemExerciseBinding
import com.fitapp.appfit.feature.exercise.model.exercise.response.ExerciseResponse
import com.fitapp.appfit.feature.exercise.util.ExerciseValidation

class ExerciseAdapter(
    private val onItemClick: (ExerciseResponse) -> Unit,
    private val isAdminMode: Boolean = false
) : ListAdapter<ExerciseResponse, ExerciseAdapter.ExerciseViewHolder>(ExerciseDiffCallback()) {

    private val internalList = mutableListOf<ExerciseResponse>()

    fun setExercises(exercises: List<ExerciseResponse>) {
        internalList.clear()
        internalList.addAll(exercises)
        submitList(internalList.toList())
    }

    fun addExercises(newExercises: List<ExerciseResponse>) {
        internalList.addAll(newExercises)
        submitList(internalList.toList())
    }

    fun getExercises(): List<ExerciseResponse> = internalList.toList()

    fun clearExercises() {
        internalList.clear()
        submitList(emptyList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val binding = ItemExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExerciseViewHolder(private val binding: ItemExerciseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(exercise: ExerciseResponse) {
            binding.tvExerciseName.text = exercise.name

            val typeLabel = exercise.exerciseType?.let { type ->
                ExerciseValidation.ExerciseTypeInfo.fromType(type)?.label ?: type.name
            } ?: "SIN TIPO"
            binding.tvExerciseType.text = typeLabel

            val sportsText = exercise.sportsDisplayName()
            binding.tvExerciseSports.text = if (sportsText.isNotEmpty()) sportsText else "—"
            binding.tvExerciseSports.visibility = View.VISIBLE

            binding.tvExerciseRating.text = ExerciseValidation.formatRating(exercise.rating, exercise.ratingCount)
            binding.tvExerciseRating.visibility = if (exercise.rating != null && exercise.rating > 0) View.VISIBLE else View.GONE

            if (exercise.usageCount != null && exercise.usageCount > 0) {
                binding.tvExerciseUsage.text = "${exercise.usageCount} usos"
                binding.tvExerciseUsage.visibility = View.VISIBLE
            } else {
                binding.tvExerciseUsage.visibility = View.GONE
            }

            val isSystemExercise = exercise.isPublic == true && exercise.createdById == null
            val visibilityLabel = ExerciseValidation.getVisibilityLabel(exercise.isPublic ?: false, isSystemExercise)
            binding.tvExerciseVisibility.text = visibilityLabel

            val visibilityColor = when {
                isSystemExercise -> Color.parseColor("#666666")
                exercise.isPublic == true -> Color.parseColor("#78703F")
                else -> Color.parseColor("#B3B3B3")
            }
            binding.tvExerciseVisibility.setTextColor(visibilityColor)

            val isActive = exercise.isActive == true
            binding.root.alpha = if (isActive) 1.0f else 0.6f

            binding.root.setOnClickListener { onItemClick(exercise) }
        }
    }

    class ExerciseDiffCallback : DiffUtil.ItemCallback<ExerciseResponse>() {
        override fun areItemsTheSame(old: ExerciseResponse, new: ExerciseResponse): Boolean = old.id == new.id
        override fun areContentsTheSame(old: ExerciseResponse, new: ExerciseResponse): Boolean = old == new
    }
}