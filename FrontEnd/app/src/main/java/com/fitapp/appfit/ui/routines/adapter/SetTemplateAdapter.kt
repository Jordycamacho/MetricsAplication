package com.fitapp.appfit.ui.routines.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.databinding.ItemSetTemplateBinding
import com.fitapp.appfit.response.routine.response.RoutineSetTemplateResponse

class SetTemplateAdapter(
    private val onEditClick: (RoutineSetTemplateResponse) -> Unit,
    private val onDeleteClick: (RoutineSetTemplateResponse) -> Unit
) : RecyclerView.Adapter<SetTemplateAdapter.ViewHolder>() {

    private var sets = listOf<RoutineSetTemplateResponse>()

    fun submitList(list: List<RoutineSetTemplateResponse>) {
        sets = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSetTemplateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val set = sets[position]
        holder.bind(set)
        holder.binding.btnEdit.setOnClickListener { onEditClick(set) }
        holder.binding.btnDelete.setOnClickListener { onDeleteClick(set) }
    }

    override fun getItemCount() = sets.size

    inner class ViewHolder(val binding: ItemSetTemplateBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(set: RoutineSetTemplateResponse) {
            binding.tvSetTitle.text = "Set ${set.position} - ${set.setType}"
            val paramsText = if (set.parameters.isNullOrEmpty()) {
                "Sin parámetros"
            } else {
                set.parameters!!.joinToString(", ") { param ->
                    "${param.parameterName ?: "Parámetro"}: ${param.numericValue ?: param.integerValue ?: param.durationValue ?: "-"}"
                }
            }
            binding.tvSetParameters.text = paramsText
            binding.tvRestAfter.text = set.restAfterSet?.let { "Descanso: ${it}s" } ?: ""
        }
    }
}