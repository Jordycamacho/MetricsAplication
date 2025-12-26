package com.fitapp.appfit.ui.sports.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.response.sport.response.SportResponse

class SportAdapter(
    private var sports: List<SportResponse> = emptyList(),
    private val onItemClick: (SportResponse) -> Unit,
    private val onDeleteClick: (SportResponse) -> Unit
) : RecyclerView.Adapter<SportAdapter.SportViewHolder>() {

    inner class SportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_sport_name)
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_sport_category)
        private val tvType: TextView = itemView.findViewById(R.id.tv_sport_type)
        private val btnDelete: TextView = itemView.findViewById(R.id.btn_delete_sport)

        fun bind(sport: SportResponse) {
            // Nombre
            tvName.text = sport.name

            // Categoría
            tvCategory.text = if (sport.category.isNullOrEmpty()) {
                "Sin categoría"
            } else {
                "Categoría: ${sport.category}"
            }

            // Tipo (badge)
            if (sport.isPredefined) {
                tvType.text = "PREDEFINIDO"
                tvType.background = ContextCompat.getDrawable(itemView.context, R.drawable.badge_predefined)
                tvType.visibility = View.VISIBLE
                btnDelete.visibility = View.GONE // Ocultar botón eliminar
            } else {
                tvType.text = "PERSONALIZADO"
                tvType.background = ContextCompat.getDrawable(itemView.context, R.drawable.badge_custom)
                tvType.visibility = View.VISIBLE
                btnDelete.visibility = View.VISIBLE // Mostrar botón eliminar
            }

            // Click en el botón de eliminar
            btnDelete.setOnClickListener {
                onDeleteClick(sport)
            }

            // Click en toda la tarjeta - Ahora solo muestra opciones de acción
            itemView.setOnClickListener {
                showActionOptions(sport)
            }

            // Click largo para eliminar (opcional)
            itemView.setOnLongClickListener {
                if (!sport.isPredefined) {
                    onDeleteClick(sport)
                }
                true
            }
        }

        private fun showActionOptions(sport: SportResponse) {
            // Aquí podrías mostrar un menú de opciones si necesitas
            // Por ahora solo navegamos al detalle o hacemos algo útil
            if (sport.isPredefined) {
                // Para predefinidos, mostrar información
                onItemClick(sport)
            } else {
                // Para personalizados, mostrar opciones (editar, eliminar, etc.)
                onItemClick(sport)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sport, parent, false)
        return SportViewHolder(view)
    }

    override fun onBindViewHolder(holder: SportViewHolder, position: Int) {
        holder.bind(sports[position])
    }

    override fun getItemCount(): Int = sports.size

    fun updateList(newSports: List<SportResponse>) {
        sports = newSports
        notifyDataSetChanged()
    }
}