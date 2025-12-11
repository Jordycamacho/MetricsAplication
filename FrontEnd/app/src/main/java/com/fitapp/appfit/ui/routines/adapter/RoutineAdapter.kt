package com.fitapp.appfit.ui.routines.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.response.routine.response.RoutineSummaryResponse

class RoutineAdapter(
    private var routines: List<RoutineSummaryResponse> = emptyList(),
    private val onItemClick: (RoutineSummaryResponse) -> Unit = {},
    private val onEditClick: (RoutineSummaryResponse) -> Unit = {},
    private val onStartClick: (RoutineSummaryResponse) -> Unit = {}
) : RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_routine, parent, false)
        return RoutineViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        val routine = routines[position]
        holder.bind(routine)

        // Click en toda la tarjeta
        holder.itemView.setOnClickListener {
            onItemClick(routine)
        }

        // Click en botón editar
        holder.btnEdit.setOnClickListener {
            onEditClick(routine)
        }

        // Click en botón iniciar
        holder.btnStart.setOnClickListener {
            onStartClick(routine)
        }
    }

    override fun getItemCount(): Int = routines.size

    fun updateRoutines(newRoutines: List<RoutineSummaryResponse>) {
        routines = newRoutines
        notifyDataSetChanged()
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
}