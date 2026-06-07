package com.fitapp.appfit.feature.metrics.presentation.records

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.R
import com.fitapp.appfit.feature.metrics.domain.model.PersonalRecordItem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PersonalRecordsAdapter : RecyclerView.Adapter<PersonalRecordsAdapter.VH>() {
    private var items: List<PersonalRecordItem> = emptyList()

    fun submitList(list: List<PersonalRecordItem>) {
        items = list
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_metrics_personal_record, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvExercise: TextView = itemView.findViewById(R.id.tv_exercise)
        private val tvParameter: TextView = itemView.findViewById(R.id.tv_parameter)
        private val tvValue: TextView = itemView.findViewById(R.id.tv_value)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)

        fun bind(item: PersonalRecordItem) {
            tvExercise.text = item.exerciseName
            item.parameterName?.takeIf { it.isNotBlank() }?.let { param ->
                tvParameter.isVisible = true
                tvParameter.text = param
            } ?: run { tvParameter.isVisible = false }
            tvValue.text = item.valueLabel
            tvDate.text = "Logrado el ${formatDate(item.achievedAt)}"
        }

        private fun formatDate(iso: String) = try {
            LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"))
        } catch (_: Exception) { iso }
    }
}
