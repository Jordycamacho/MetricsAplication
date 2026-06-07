package com.fitapp.appfit.feature.metrics.presentation.weekly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.feature.metrics.domain.model.WeeklyVolumePoint

class WeeklyVolumeAdapter : RecyclerView.Adapter<WeeklyVolumeAdapter.VH>() {

    private var items: List<WeeklyVolumePoint> = emptyList()

    fun submitList(list: List<WeeklyVolumePoint>) {
        items = list
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_metrics_weekly_volume, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvWeek: TextView = itemView.findViewById(R.id.tv_week_label)
        private val tvSessions: TextView = itemView.findViewById(R.id.tv_sessions)
        private val tvVolume: TextView = itemView.findViewById(R.id.tv_volume)
        private val layoutDelta: LinearLayout = itemView.findViewById(R.id.layout_delta)
        private val tvDelta: TextView = itemView.findViewById(R.id.tv_delta)

        fun bind(point: WeeklyVolumePoint) {
            tvWeek.text = point.weekLabel
            tvSessions.text = when (point.sessionsCount) {
                1 -> "1 sesión"
                else -> "${point.sessionsCount} sesiones"
            }
            tvVolume.text = String.format("%,.0f kg", point.volumeKg)
            point.deltaVolumeKg?.let { delta ->
                layoutDelta.isVisible = true
                val deltaText = when {
                    delta > 0 -> "▲ +${String.format("%,.0f", delta)} kg"
                    delta < 0 -> "▼ ${String.format("%,.0f", delta)} kg"
                    else -> "= sin cambio"
                }
                tvDelta.text = deltaText
                val colorRes = when {
                    delta > 0 -> R.color.set_completed_green
                    delta < 0 -> R.color.red
                    else -> R.color.text_secondary_dark
                }
                tvDelta.setTextColor(ContextCompat.getColor(itemView.context, colorRes))
            } ?: run { layoutDelta.isVisible = false }
        }
    }
}
