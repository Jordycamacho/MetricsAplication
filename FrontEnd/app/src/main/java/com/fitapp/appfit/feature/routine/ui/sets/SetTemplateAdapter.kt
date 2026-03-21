package com.fitapp.appfit.feature.routine.ui.sets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.databinding.ItemSetTemplateBinding
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse
import kotlin.collections.get

class SetTemplateAdapter(
    private val onEditClick: (RoutineSetTemplateResponse) -> Unit,
    private val onDeleteClick: (RoutineSetTemplateResponse) -> Unit
) : ListAdapter<RoutineSetTemplateResponse, SetTemplateAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<RoutineSetTemplateResponse>() {
        override fun areItemsTheSame(old: RoutineSetTemplateResponse, new: RoutineSetTemplateResponse) =
            old.id == new.id
        override fun areContentsTheSame(old: RoutineSetTemplateResponse, new: RoutineSetTemplateResponse) =
            old == new
    }

    private val setTypeLabels = mapOf(
        "NORMAL" to "Normal", "WARM_UP" to "Calentamiento", "DROP_SET" to "Drop Set",
        "SUPER_SET" to "Super Set", "GIANT_SET" to "Giant Set", "PYRAMID" to "Pirámide",
        "REVERSE_PYRAMID" to "Pirámide inversa", "CLUSTER" to "Cluster",
        "REST_PAUSE" to "Rest-Pause", "ECCENTRIC" to "Excéntrico", "ISOMETRIC" to "Isométrico"
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSetTemplateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(val binding: ItemSetTemplateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(set: RoutineSetTemplateResponse) {
            val label = setTypeLabels[set.setType] ?: set.setType ?: "Normal"
            binding.tvSetTitle.text = "Set ${set.position}  ·  $label"

            binding.tvRestAfter.text = set.restAfterSet?.let { "${it}s" } ?: ""

            binding.tvSetParameters.text = if (set.parameters.isNullOrEmpty()) {
                "Sin parámetros"
            } else {
                set.parameters!!.joinToString("  ·  ") { param ->
                    val name = param.parameterName ?: "Param"
                    val value = param.numericValue?.toString()
                        ?: param.integerValue?.toString()
                        ?: param.durationValue?.let { formatDuration(it) }
                        ?: "-"
                    val unit = param.unit?.let { " $it" } ?: ""
                    val reps = param.repetitions?.let { " × $it" } ?: ""
                    "$name: $value$unit$reps"
                }
            }

            binding.btnEdit.setOnClickListener { onEditClick(set) }
            binding.btnDelete.setOnClickListener { onDeleteClick(set) }
        }

        private fun formatDuration(seconds: Long): String {
            val m = seconds / 60
            val s = seconds % 60
            return if (m > 0) "${m}m ${s}s" else "${s}s"
        }
    }
}