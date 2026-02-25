package com.fitapp.appfit.ui.sports

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.response.sport.response.SportResponse

class SportAdapter(
    private val onItemClick: (SportResponse) -> Unit,
    private val onDeleteClick: (SportResponse) -> Unit
) : ListAdapter<SportResponse, SportAdapter.SportViewHolder>(SportDiffCallback()) {

    inner class SportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_sport_name)
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_sport_category)
        private val tvType: TextView = itemView.findViewById(R.id.tv_sport_type)
        private val btnDelete: TextView = itemView.findViewById(R.id.btn_delete_sport)

        fun bind(sport: SportResponse) {
            tvName.text = sport.name

            tvCategory.text = if (sport.category.isNullOrEmpty()) "Sin categoría"
            else sport.category

            tvType.text = if (sport.isPredefined) "PRE" else "MÍO"

            btnDelete.visibility = if (sport.isPredefined) View.GONE else View.VISIBLE

            btnDelete.setOnClickListener { onDeleteClick(sport) }

            itemView.setOnClickListener { onItemClick(sport) }

            itemView.setOnLongClickListener {
                if (!sport.isPredefined) onDeleteClick(sport)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sport, parent, false)
        return SportViewHolder(view)
    }

    override fun onBindViewHolder(holder: SportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // Mantener compatibilidad con código existente que usa updateList()
    fun updateList(newSports: List<SportResponse>) {
        submitList(newSports)
    }

    class SportDiffCallback : DiffUtil.ItemCallback<SportResponse>() {
        override fun areItemsTheSame(old: SportResponse, new: SportResponse) = old.id == new.id
        override fun areContentsTheSame(old: SportResponse, new: SportResponse) = old == new
    }
}