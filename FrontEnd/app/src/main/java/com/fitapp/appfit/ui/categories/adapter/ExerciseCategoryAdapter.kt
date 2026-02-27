package com.fitapp.appfit.ui.categories.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.response.category.response.ExerciseCategoryResponse

class ExerciseCategoryAdapter(
    private val onItemClick: (ExerciseCategoryResponse) -> Unit,
    private val onEditClick: (ExerciseCategoryResponse) -> Unit,
    private val onDeleteClick: (ExerciseCategoryResponse) -> Unit
) : RecyclerView.Adapter<ExerciseCategoryAdapter.ExerciseCategoryViewHolder>() {

    private var categories: List<ExerciseCategoryResponse> = emptyList()

    inner class ExerciseCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_category_name)
        private val tvType: TextView = itemView.findViewById(R.id.tv_category_type)
        private val tvSport: TextView = itemView.findViewById(R.id.tv_category_sport)
        private val tvUsage: TextView = itemView.findViewById(R.id.tv_category_usage)
        private val btnEdit: TextView = itemView.findViewById(R.id.btn_edit_category)
        private val btnDelete: TextView = itemView.findViewById(R.id.btn_delete_category)

        fun bind(category: ExerciseCategoryResponse) {
            tvName.text = category.name

            // Badge "SISTEMA" solo para predefinidas
            // Texto #121212 sobre fondo dorado → perfectamente legible
            tvType.visibility = if (category.isPredefined) View.VISIBLE else View.GONE

            // Deporte debajo del nombre (color dorado)
            val sport = category.sportName
            if (!sport.isNullOrEmpty()) {
                tvSport.text = sport
                tvSport.visibility = View.VISIBLE
            } else {
                tvSport.visibility = View.GONE
            }

            // Fila inferior y botones
            if (category.isPredefined) {
                tvUsage.text = "Sistema"
                btnEdit.visibility = View.GONE
                btnDelete.visibility = View.GONE
            } else {
                tvUsage.text = "Personal  ·  ${category.usageCount} usos"
                btnEdit.visibility = View.VISIBLE
                btnDelete.visibility = View.VISIBLE
                // Siempre reasignar listeners para evitar reciclaje incorrecto
                btnEdit.setOnClickListener { onEditClick(category) }
                btnDelete.setOnClickListener { onDeleteClick(category) }
            }

            itemView.setOnClickListener { onItemClick(category) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseCategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise_category, parent, false)
        return ExerciseCategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseCategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    fun updateList(newCategories: List<ExerciseCategoryResponse>) {
        val diff = DiffUtil.calculateDiff(CategoryDiffCallback(categories, newCategories))
        categories = newCategories
        diff.dispatchUpdatesTo(this)
    }

    private class CategoryDiffCallback(
        private val old: List<ExerciseCategoryResponse>,
        private val new: List<ExerciseCategoryResponse>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = old.size
        override fun getNewListSize() = new.size
        override fun areItemsTheSame(o: Int, n: Int) = old[o].id == new[n].id
        override fun areContentsTheSame(o: Int, n: Int) = old[o] == new[n]
    }
}