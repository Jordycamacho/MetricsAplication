package com.fitapp.appfit.ui.routines.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.databinding.ItemRoutineExerciseBinding
import com.fitapp.appfit.response.routine.response.RoutineExerciseResponse

class RoutineExerciseAdapter(
    private val onEditClick: (RoutineExerciseResponse) -> Unit,
    private val onDeleteClick: (RoutineExerciseResponse) -> Unit,
    private val onAddSetClick: (RoutineExerciseResponse) -> Unit
) : ListAdapter<RoutineExerciseResponse, RoutineExerciseAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<RoutineExerciseResponse>() {
        override fun areItemsTheSame(old: RoutineExerciseResponse, new: RoutineExerciseResponse) =
            old.id == new.id

        override fun areContentsTheSame(old: RoutineExerciseResponse, new: RoutineExerciseResponse) =
            old == new
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRoutineExerciseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(val binding: ItemRoutineExerciseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(exercise: RoutineExerciseResponse) {
            binding.tvExerciseName.text = exercise.exerciseName ?: "Ejercicio"

            binding.tvExerciseDetails.text = buildString {
                val dayMap = mapOf(
                    "MONDAY" to "Lun", "TUESDAY" to "Mar", "WEDNESDAY" to "Mié",
                    "THURSDAY" to "Jue", "FRIDAY" to "Vie", "SATURDAY" to "Sáb", "SUNDAY" to "Dom"
                )
                exercise.sessionNumber?.let { append("Sesión $it") }
                exercise.dayOfWeek?.let {
                    if (isNotEmpty()) append(" · ")
                    append(dayMap[it.toString()] ?: it.toString())
                }
                if (exercise.sessionOrder != null) {
                    if (isNotEmpty()) append(" · ")
                    append("Orden ${exercise.sessionOrder}")
                }
                if (exercise.restAfterExercise != null) {
                    if (isNotEmpty()) append(" · ")
                    append("${exercise.restAfterExercise}s descanso")
                }
            }

            val setsCount = exercise.sets ?: 0
            binding.tvSetsInfo.text = if (setsCount == 0) "Sin sets" else "$setsCount set${if (setsCount != 1) "s" else ""}"

            binding.btnEdit.setOnClickListener { onEditClick(exercise) }
            binding.btnDelete.setOnClickListener { onDeleteClick(exercise) }
            binding.btnAddSet.setOnClickListener { onAddSetClick(exercise) }
        }
    }
}