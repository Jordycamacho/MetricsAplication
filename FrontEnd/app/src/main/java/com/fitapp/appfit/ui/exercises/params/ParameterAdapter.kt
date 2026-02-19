package com.fitapp.appfit.ui.exercises.params

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.response.parameter.response.CustomParameterResponse

class ParameterAdapter(
    private var parameters: List<CustomParameterResponse> = emptyList(),
    private val onItemClick: (CustomParameterResponse) -> Unit,
    private val onEditClick: (CustomParameterResponse) -> Unit = {},
    private val onDeleteClick: (CustomParameterResponse) -> Unit = {},
    private val showActions: Boolean = true
) : RecyclerView.Adapter<ParameterAdapter.ParameterViewHolder>() {

    inner class ParameterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_parameter_name)
        private val tvType: TextView = itemView.findViewById(R.id.tv_parameter_type)
        private val tvUnit: TextView = itemView.findViewById(R.id.tv_parameter_unit)
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_parameter_category)
        private val tvVisibility: TextView = itemView.findViewById(R.id.tv_parameter_visibility)
        private val btnEdit: View = itemView.findViewById(R.id.btn_edit_parameter)
        private val btnDelete: View = itemView.findViewById(R.id.btn_delete_parameter)

        fun bind(parameter: CustomParameterResponse) {
            // Nombre
            tvName.text = parameter.displayName ?: parameter.name

            // Tipo
            tvType.text = parameter.parameterType.replaceFirstChar { it.uppercase() }

            // Unidad
            if (!parameter.unit.isNullOrEmpty()) {
                tvUnit.text = "(${parameter.unit})"
                tvUnit.visibility = View.VISIBLE
            } else {
                tvUnit.visibility = View.GONE
            }

            // Categoría
            tvCategory.text = parameter.category ?: "Sin categoría"

            // Visibilidad
            if (parameter.isGlobal && parameter.ownerId == null) {
                tvVisibility.text = "Global (Sistema)"
            } else if (parameter.isGlobal) {
                tvVisibility.text = "Global"
            } else {
                tvVisibility.text = "Personal"
            }

            // Controlar visibilidad de botones
            if (showActions) {
                if (parameter.isGlobal && parameter.ownerId == null) {
                    btnEdit.visibility = View.GONE
                    btnDelete.visibility = View.GONE
                } else if (parameter.isGlobal) {
                    btnEdit.visibility = View.GONE
                    btnDelete.visibility = View.GONE
                } else {
                    btnEdit.visibility = View.VISIBLE
                    btnDelete.visibility = View.VISIBLE
                    btnEdit.setOnClickListener { onEditClick(parameter) }
                    btnDelete.setOnClickListener { onDeleteClick(parameter) }
                }
            } else {
                // En modo selección, ocultar botones
                btnEdit.visibility = View.GONE
                btnDelete.visibility = View.GONE
            }

            // Click en toda la tarjeta
            itemView.setOnClickListener {
                onItemClick(parameter)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParameterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_parameter, parent, false)
        return ParameterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParameterViewHolder, position: Int) {
        holder.bind(parameters[position])
    }

    override fun getItemCount(): Int = parameters.size

    fun updateList(newParameters: List<CustomParameterResponse>) {
        parameters = newParameters
        notifyDataSetChanged()
    }
}