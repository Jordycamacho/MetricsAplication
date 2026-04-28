package com.fitapp.appfit.feature.workout.presentation.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionSummaryResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class WorkoutHistoryAdapter(
    private val onItemClick: (WorkoutSessionSummaryResponse) -> Unit,
    private val onDeleteClick: (WorkoutSessionSummaryResponse) -> Unit
) : ListAdapter<WorkoutSessionSummaryResponse, WorkoutHistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_session, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvRoutineName: TextView = itemView.findViewById(R.id.tv_routine_name)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        private val tvDuration: TextView = itemView.findViewById(R.id.tv_duration)
        private val tvExercises: TextView = itemView.findViewById(R.id.tv_exercises)
        private val tvSets: TextView = itemView.findViewById(R.id.tv_sets)
        private val tvVolume: TextView = itemView.findViewById(R.id.tv_volume)
        private val tvPerformance: TextView = itemView.findViewById(R.id.tv_performance)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)

        fun bind(session: WorkoutSessionSummaryResponse) {
            tvRoutineName.text = session.routineName

            tvDate.text = formatRelativeDate(session.startTime)

            tvDuration.text = formatDuration(session.durationSeconds)

            tvExercises.text = "${session.exerciseCount} ejercicios"
            tvSets.text = "${session.setCount} sets"

            if (session.totalVolume != null && session.totalVolume > 0) {
                tvVolume.visibility = View.VISIBLE
                tvVolume.text = "Vol: ${String.format("%.1f", session.totalVolume)} kg"
            } else {
                tvVolume.visibility = View.VISIBLE
                tvVolume.text = "Vol: 0.0 kg"
            }

            if (session.performanceScore != null) {
                tvPerformance.visibility = View.VISIBLE
                tvPerformance.text = "${session.performanceScore}/10"

                val color = when {
                    session.performanceScore >= 8 -> itemView.context.getColor(R.color.set_completed_green)
                    session.performanceScore >= 6 -> itemView.context.getColor(R.color.gold_primary)
                    else -> itemView.context.getColor(R.color.text_secondary_dark)
                }
                tvPerformance.setTextColor(color)
            } else {
                tvPerformance.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onItemClick(session)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(session)
            }
        }

        private fun formatRelativeDate(isoDate: String): String {
            return try {
                val dateTime = LocalDateTime.parse(isoDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val now = LocalDateTime.now()
                val daysDiff = ChronoUnit.DAYS.between(dateTime.toLocalDate(), now.toLocalDate())

                when {
                    daysDiff == 0L -> "Hoy, ${dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                    daysDiff == 1L -> "Ayer, ${dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                    daysDiff < 7L -> "${daysDiff} días atrás"
                    else -> dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                }
            } catch (e: Exception) {
                isoDate
            }
        }

        private fun formatDuration(seconds: Long?): String {
            if (seconds == null || seconds <= 0) return "0m"

            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60

            return when {
                hours > 0 -> "${hours}h ${minutes}m"
                minutes > 0 -> "${minutes}m"
                else -> "< 1m"
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<WorkoutSessionSummaryResponse>() {
        override fun areItemsTheSame(
            oldItem: WorkoutSessionSummaryResponse,
            newItem: WorkoutSessionSummaryResponse
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: WorkoutSessionSummaryResponse,
            newItem: WorkoutSessionSummaryResponse
        ): Boolean = oldItem == newItem
    }
}