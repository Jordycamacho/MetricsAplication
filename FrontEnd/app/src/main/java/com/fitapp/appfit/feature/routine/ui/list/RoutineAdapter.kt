package com.fitapp.appfit.feature.routine.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineSummaryResponse
import com.google.android.material.button.MaterialButton
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class RoutineAdapter(
    private val onItemClick: (RoutineSummaryResponse) -> Unit = {},
    private val onEditClick: (RoutineSummaryResponse) -> Unit = {},
    private val onAddExercisesClick: (RoutineSummaryResponse) -> Unit = {},
    private val onStartClick: (RoutineSummaryResponse) -> Unit = {}
) : ListAdapter<RoutineSummaryResponse, RoutineAdapter.RoutineViewHolder>(DiffCallback()) {

    inner class RoutineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_routine_name)
        private val tvLastUsed: TextView = itemView.findViewById(R.id.tv_last_used)
        private val tvExerciseCount: TextView = itemView.findViewById(R.id.tv_exercise_count)
        private val tvSportName: TextView = itemView.findViewById(R.id.tv_sport_name)
        val btnEdit: MaterialButton = itemView.findViewById(R.id.btn_edit)
        val btnStart: MaterialButton = itemView.findViewById(R.id.btn_start_workout)
        val btnAddExercises: MaterialButton = itemView.findViewById(R.id.btn_add_exercises)

        fun bind(routine: RoutineSummaryResponse) {
            tvName.text = routine.name
            tvExerciseCount.text = "${routine.exerciseCount} ejercicios"

            val sport = routine.sportName
            if (!sport.isNullOrEmpty()) {
                tvSportName.text = sport
                tvSportName.visibility = View.VISIBLE
            } else {
                tvSportName.visibility = View.GONE
            }

            routine.lastUsedAt?.let { lastUsedStr ->
                try {
                    val lastUsed = LocalDateTime.parse(lastUsedStr.toString(), DateTimeFormatter.ISO_DATE_TIME)
                    val daysAgo = ChronoUnit.DAYS.between(lastUsed.toLocalDate(), LocalDate.now())
                    tvLastUsed.text = when {
                        daysAgo == 0L -> "Hoy"
                        daysAgo == 1L -> "Ayer"
                        daysAgo < 7 -> "Hace ${daysAgo}d"
                        else -> lastUsed.format(DateTimeFormatter.ofPattern("dd/MM"))
                    }
                    tvLastUsed.visibility = View.VISIBLE
                } catch (e: Exception) {
                    tvLastUsed.visibility = View.GONE
                }
            } ?: run { tvLastUsed.visibility = View.GONE }

            itemView.alpha = if (routine.isActive) 1f else 0.5f
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_routine, parent, false)
        return RoutineViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        val routine = getItem(position)
        holder.bind(routine)
        holder.itemView.setOnClickListener { onItemClick(routine) }
        holder.btnEdit.setOnClickListener { onEditClick(routine) }
        holder.btnStart.setOnClickListener { onStartClick(routine) }
        holder.btnAddExercises.setOnClickListener { onAddExercisesClick(routine) }
    }

    fun updateRoutines(newList: List<RoutineSummaryResponse>) = submitList(newList)

    private class DiffCallback : DiffUtil.ItemCallback<RoutineSummaryResponse>() {
        override fun areItemsTheSame(o: RoutineSummaryResponse, n: RoutineSummaryResponse) = o.id == n.id
        override fun areContentsTheSame(o: RoutineSummaryResponse, n: RoutineSummaryResponse) = o == n
    }
}