package com.fitapp.appfit.ui.exercises.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.databinding.ItemExerciseBinding
import com.fitapp.appfit.response.exercise.response.ExerciseResponse
import com.fitapp.appfit.utils.DateUtils

class ExerciseAdapter(
    private val onItemClick: (ExerciseResponse) -> Unit,
    private val onEditClick: (ExerciseResponse) -> Unit,
    private val onDeleteClick: (ExerciseResponse) -> Unit,
    private val onToggleStatusClick: (ExerciseResponse) -> Unit,
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
            binding.tvExerciseType.text = exercise.exerciseType?.name ?: "SIN TIPO"
            binding.tvExerciseDescription.text = exercise.description ?: "Sin descripción"
            binding.tvExerciseSport.text = exercise.sports.values.joinToString(", ").ifEmpty { "—" }
            binding.tvExerciseCategories.text = if (exercise.categoryNames.isEmpty()) "Sin categorías"
            else exercise.categoryNames.joinToString(", ")

            // Parámetros
            binding.tvExerciseParameters.text = if (exercise.supportedParameterNames.isEmpty()) "Sin parámetros"
            else "${exercise.supportedParameterNames.size} parámetros"

            binding.tvExerciseVisibility.text = if (exercise.isPublic == true) "Público" else "Personal"

            val isActive = exercise.isActive == true
            binding.tvExerciseStatus.text = if (isActive) "Activo" else "Inactivo"
            binding.btnToggleStatus.text = if (isActive) "Pausar" else "Activar"

            binding.tvExerciseUsageCount.text = "${exercise.usageCount ?: 0} usos"
            binding.tvExerciseRating.text = String.format("%.1f", exercise.rating ?: 0.0)

            binding.tvExerciseLastUsed.text = if (!exercise.lastUsedAt.isNullOrEmpty())
                "Último uso: ${DateUtils.formatForDisplay(exercise.lastUsedAt)}"
            else "Nunca usado"

            val canEdit = exercise.isPublic == false || isAdminMode
            binding.btnEditExercise.visibility = if (canEdit) View.VISIBLE else View.GONE
            binding.btnDeleteExercise.visibility = if (canEdit) View.VISIBLE else View.GONE
            binding.btnMakePublic.visibility =
                if (isAdminMode && exercise.isPublic == false) View.VISIBLE else View.GONE

            binding.root.setOnClickListener { onItemClick(exercise) }
            binding.btnEditExercise.setOnClickListener { onEditClick(exercise) }
            binding.btnDeleteExercise.setOnClickListener { onDeleteClick(exercise) }
            binding.btnToggleStatus.setOnClickListener { onToggleStatusClick(exercise) }
        }
    }

    class ExerciseDiffCallback : DiffUtil.ItemCallback<ExerciseResponse>() {
        override fun areItemsTheSame(old: ExerciseResponse, new: ExerciseResponse) = old.id == new.id
        override fun areContentsTheSame(old: ExerciseResponse, new: ExerciseResponse) = old == new
    }
}