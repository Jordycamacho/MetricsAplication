package com.fitapp.appfit.ui.exercises.params

import android.graphics.Color
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
        private val tvVisibility: TextView = itemView.findViewById(R.id.tv_parameter_visibility)
        private val btnEdit: View = itemView.findViewById(R.id.btn_edit_parameter)
        private val btnDelete: View = itemView.findViewById(R.id.btn_delete_parameter)

        fun bind(parameter: CustomParameterResponse) {
            tvName.text = parameter.name
            tvType.text = getTypeLabel(parameter.parameterType)

            if (!parameter.unit.isNullOrEmpty()) {
                tvUnit.text = parameter.unit
                tvUnit.visibility = View.VISIBLE
            } else {
                tvUnit.visibility = View.GONE
            }

            when {
                parameter.isGlobal && parameter.ownerId == null -> {
                    tvVisibility.text = "Sistema"
                    tvVisibility.setTextColor(Color.parseColor("#666666"))
                }
                parameter.isGlobal -> {
                    tvVisibility.text = "Global"
                    tvVisibility.setTextColor(Color.parseColor("#78703F"))
                }
                else -> {
                    tvVisibility.text = "Personal"
                    tvVisibility.setTextColor(Color.parseColor("#B3B3B3"))
                }
            }

            val isEditable = showActions && !parameter.isGlobal
            btnEdit.visibility = if (isEditable) View.VISIBLE else View.GONE
            btnDelete.visibility = if (isEditable) View.VISIBLE else View.GONE

            if (isEditable) {
                btnEdit.setOnClickListener { onEditClick(parameter) }
                btnDelete.setOnClickListener { onDeleteClick(parameter) }
            }

            itemView.setOnClickListener { onItemClick(parameter) }
        }

        private fun getTypeLabel(type: String): String = when (type.uppercase()) {
            "NUMBER" -> "Número"
            "INTEGER" -> "Entero"
            "TEXT" -> "Texto"
            "BOOLEAN" -> "Sí/No"
            "DURATION" -> "Tiempo"
            "DISTANCE" -> "Distancia"
            "PERCENTAGE" -> "Porcentaje"
            else -> type
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParameterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_parameter, parent, false)
        return ParameterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParameterViewHolder, position: Int) = holder.bind(parameters[position])

    override fun getItemCount(): Int = parameters.size

    fun updateList(newParameters: List<CustomParameterResponse>) {
        parameters = newParameters
        notifyDataSetChanged()
    }
}