package com.fitapp.appfit.ui.routines.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.databinding.ItemRoutineExerciseBinding
import com.fitapp.appfit.response.routine.response.RoutineExerciseResponse

class RoutineExerciseAdapter(
    private val onEditClick: (RoutineExerciseResponse) -> Unit,
    private val onDeleteClick: (RoutineExerciseResponse) -> Unit,
    private val onAddSetClick: (RoutineExerciseResponse) -> Unit
) : RecyclerView.Adapter<RoutineExerciseAdapter.ViewHolder>() {

    private var exercises = listOf<RoutineExerciseResponse>()

    fun submitList(list: List<RoutineExerciseResponse>) {
        exercises = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRoutineExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.bind(exercise)
        holder.itemView.setOnClickListener { /* Podría abrir detalle */ }
        holder.binding.btnEdit.setOnClickListener { onEditClick(exercise) }
        holder.binding.btnDelete.setOnClickListener { onDeleteClick(exercise) }
        holder.binding.btnAddSet.setOnClickListener { onAddSetClick(exercise) }
    }

    override fun getItemCount() = exercises.size

    inner class ViewHolder(val binding: ItemRoutineExerciseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(exercise: RoutineExerciseResponse) {
            binding.tvExerciseName.text = exercise.exerciseName ?: "Ejercicio"
            binding.tvExerciseDetails.text = buildString {
                exercise.sessionNumber?.let { append("Sesión $it · ") }
                exercise.dayOfWeek?.let { append("${it} · ") }
                append("Orden: ${exercise.sessionOrder ?: 1}")
            }
            binding.tvSetsInfo.text = "${exercise.sets ?: 0} sets"
        }
    }
}