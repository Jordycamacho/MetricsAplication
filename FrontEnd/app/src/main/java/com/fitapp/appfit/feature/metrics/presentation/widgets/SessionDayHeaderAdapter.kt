package com.fitapp.appfit.feature.metrics.presentation.widgets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R

class SessionDayHeaderAdapter : RecyclerView.Adapter<SessionDayHeaderAdapter.HeaderViewHolder>() {

    private var headers: List<String> = emptyList()

    fun submitHeaders(newHeaders: List<String>) {
        headers = newHeaders.filter { it != "Sesión completa" }
        notifyDataSetChanged()
    }

    override fun getItemCount() = headers.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_metrics_day_header, parent, false)
        return HeaderViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.bind(headers[position])
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHeader: TextView = itemView.findViewById(R.id.tv_day_header)
        fun bind(label: String) {
            tvHeader.text = label
        }
    }
}
