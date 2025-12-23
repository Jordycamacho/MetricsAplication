package com.fitapp.appfit.ui.routines.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.response.routine.response.RoutineSummaryResponse
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class RoutineAdapter(
    private val onItemClick: (RoutineSummaryResponse) -> Unit = {},
    private val onEditClick: (RoutineSummaryResponse) -> Unit = {},
    private val onStartClick: (RoutineSummaryResponse) -> Unit = {}
) :ListAdapter<RoutineSummaryResponse, RoutineAdapter.RoutineViewHolder>(RoutineDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_routine, parent, false)
        return RoutineViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        val routine = getItem(position)
        holder.bind(routine)

        holder.itemView.setOnClickListener {
            onItemClick(routine)
        }

        holder.btnEdit.setOnClickListener {
            onEditClick(routine)
        }

        holder.btnStart.setOnClickListener {
            onStartClick(routine)
        }
    }

    fun updateRoutines(newRoutines: List<RoutineSummaryResponse>) {
        submitList(newRoutines)
    }

    inner class RoutineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_routine_name)
        val tvDuration: TextView = itemView.findViewById(R.id.tv_routine_duration)
        val tvExerciseCount: TextView = itemView.findViewById(R.id.tv_exercise_count)
        val tvSportName: TextView = itemView.findViewById(R.id.tv_sport_name)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_routine_description)
        val btnEdit: TextView = itemView.findViewById(R.id.btn_edit)
        val btnStart: TextView = itemView.findViewById(R.id.btn_start_workout)

        fun bind(routine: RoutineSummaryResponse) {
            tvName.text = routine.name
            tvDescription.text = routine.description ?: "Sin descripción"
            tvSportName.text = routine.sportName ?: "Sin deporte"
            tvExerciseCount.text = "${routine.exerciseCount} ejercicios"

            // Calcular duración aproximada (ejemplo: 60 min por rutina)
            val estimatedMinutes = routine.exerciseCount * 10 // 10 min por ejercicio
            tvDuration.text = "$estimatedMinutes min"

            // Cambiar color si está inactiva
            if (!routine.isActive) {
                itemView.alpha = 0.6f
            } else {
                itemView.alpha = 1f
            }
        }
    }

    class RoutineDiffCallback : DiffUtil.ItemCallback<RoutineSummaryResponse>() {
        override fun areItemsTheSame(oldItem: RoutineSummaryResponse, newItem: RoutineSummaryResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RoutineSummaryResponse, newItem: RoutineSummaryResponse): Boolean {
            return oldItem == newItem
        }
    }
}