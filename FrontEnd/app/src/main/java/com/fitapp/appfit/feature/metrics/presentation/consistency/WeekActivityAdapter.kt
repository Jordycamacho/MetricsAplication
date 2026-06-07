package com.fitapp.appfit.feature.metrics.presentation.consistency

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.fitapp.appfit.feature.metrics.domain.model.WeekActivity

class WeekActivityAdapter : RecyclerView.Adapter<WeekActivityAdapter.VH>() {
    private var items: List<WeekActivity> = emptyList()

    fun submitList(list: List<WeekActivity>) {
        items = list
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_metrics_week_activity, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvWeek: TextView = itemView.findViewById(R.id.tv_week)
        private val tvDays: TextView = itemView.findViewById(R.id.tv_days)
        private val progressDays: LinearProgressIndicator = itemView.findViewById(R.id.progress_days)

        fun bind(item: WeekActivity) {
            tvWeek.text = item.weekLabel
            tvDays.text = "${item.daysTrained}/7 días"
            progressDays.max = 7
            progressDays.progress = item.daysTrained.coerceIn(0, 7)
        }
    }
}
