package com.fitapp.appfit.ui.categories.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.response.category.response.ExerciseCategoryResponse

class ExerciseCategoryAdapter(
    private var categories: List<ExerciseCategoryResponse> = emptyList(),
    private val onItemClick: (ExerciseCategoryResponse) -> Unit,
    private val onEditClick: (ExerciseCategoryResponse) -> Unit,
    private val onDeleteClick: (ExerciseCategoryResponse) -> Unit
) : RecyclerView.Adapter<ExerciseCategoryAdapter.ExerciseCategoryViewHolder>() {

    inner class ExerciseCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_category_name)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_category_description)
        private val tvType: TextView = itemView.findViewById(R.id.tv_category_type)
        private val tvVisibility: TextView = itemView.findViewById(R.id.tv_category_visibility)
        private val tvUsage: TextView = itemView.findViewById(R.id.tv_category_usage)
        private val btnEdit: View = itemView.findViewById(R.id.btn_edit_category)
        private val btnDelete: View = itemView.findViewById(R.id.btn_delete_category)
        private val layoutActions: View = itemView.findViewById(R.id.layout_actions)

        fun bind(category: ExerciseCategoryResponse) {
            // Nombre
            tvName.text = category.name

            // Descripción
            if (!category.description.isNullOrEmpty()) {
                tvDescription.text = category.description
                tvDescription.visibility = View.VISIBLE
            } else {
                tvDescription.visibility = View.GONE
            }

            // Tipo (Predefinida o Personal) con diferentes colores
            if (category.isPredefined) {
                tvType.text = "PRE-DEFINIDA"
                tvType.setTextColor(ContextCompat.getColor(itemView.context, R.color.gold_primary))
                tvType.setBackgroundResource(R.drawable.badge_predefined)
            } else {
                tvType.text = "PERSONAL"
                tvType.setTextColor(ContextCompat.getColor(itemView.context, R.color.blue_500))
                tvType.setBackgroundResource(R.drawable.badge_custom)
            }

            // Visibilidad (Pública o Privada)
            if (category.isPublic) {
                tvVisibility.text = "🌍 Pública"
                tvVisibility.setTextColor(ContextCompat.getColor(itemView.context, R.color.gold_primary))
            } else {
                tvVisibility.text = "👤 Privada"
                tvVisibility.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary_dark))
            }

            // Contador de usos
            tvUsage.text = "Usada ${category.usageCount} veces"

            // Mostrar/ocultar botones de acción
            if (category.isPredefined) {
                // Categorías predefinidas - NO mostrar botones de edición/eliminación
                layoutActions.visibility = View.GONE

                // También podemos cambiar el estilo de la tarjeta para predefinidas
                itemView.alpha = 0.9f
            } else {
                // Categorías personales - MOSTRAR botones de acción
                layoutActions.visibility = View.VISIBLE
                btnEdit.setOnClickListener { onEditClick(category) }
                btnDelete.setOnClickListener { onDeleteClick(category) }

                itemView.alpha = 1f
            }

            // Click en toda la tarjeta (para ambos tipos)
            itemView.setOnClickListener {
                onItemClick(category)
            }
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
        categories = newCategories
        notifyDataSetChanged()
    }
}