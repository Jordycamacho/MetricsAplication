package com.fitapp.appfit.feature.marketplace.ui.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.feature.marketplace.model.response.PackageItemResponse
import com.fitapp.appfit.databinding.ItemPackageItemBinding

class PackageItemAdapter : RecyclerView.Adapter<PackageItemAdapter.ItemViewHolder>() {

    private val items = mutableListOf<PackageItemResponse>()

    fun submitList(newItems: List<PackageItemResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemPackageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ItemViewHolder(private val binding: ItemPackageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PackageItemResponse) {
            with(binding) {
                tvItemType.text = item.itemType
                tvDisplayOrder.text = "#${item.displayOrder}"
                tvNotes.text = item.notes ?: "Sin notas"

                // Mostrar contenido según tipo
                when (item.itemType) {
                    "ROUTINE" -> {
                        item.routine?.let {
                            tvContentName.text = it.name
                            tvContentDetail.text = "Deporte: ${it.sportName ?: "—"}"
                        }
                    }
                    "EXERCISE" -> {
                        item.exercise?.let {
                            tvContentName.text = it.name
                            tvContentDetail.text = it.description ?: "—"
                        }
                    }
                    "SPORT" -> {
                        item.sport?.let {
                            tvContentName.text = it.name
                            tvContentDetail.text = "ID: ${it.id}"
                        }
                    }
                    "PARAMETER" -> {
                        item.parameter?.let {
                            tvContentName.text = it.name
                            tvContentDetail.text = "${it.parameterType} (${it.unit ?: "—"})"
                        }
                    }
                }
            }
        }
    }
}