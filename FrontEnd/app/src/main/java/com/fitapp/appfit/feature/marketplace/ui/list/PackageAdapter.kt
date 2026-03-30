package com.fitapp.appfit.feature.marketplace.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fitapp.appfit.feature.marketplace.model.response.PackageSummaryResponse
import com.fitapp.appfit.databinding.ItemPackageBinding

class PackageAdapter(
    private val onPackageClick: (PackageSummaryResponse) -> Unit,
    private val onDownloadClick: (PackageSummaryResponse) -> Unit
) : RecyclerView.Adapter<PackageAdapter.PackageViewHolder>() {

    private val items = mutableListOf<PackageSummaryResponse>()

    fun submitList(newItems: List<PackageSummaryResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageViewHolder {
        val binding = ItemPackageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PackageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class PackageViewHolder(private val binding: ItemPackageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pkg: PackageSummaryResponse) {
            with(binding) {
                tvPackageName.text = pkg.name
                tvDescription.text = pkg.description ?: "Sin descripción"
                tvCreator.text = pkg.creatorUsername ?: "Oficial"
                tvItemCount.text = "${pkg.itemCount} items"

                // Precio
                if (pkg.isFree) {
                    tvPrice.text = "Gratis"
                    tvPrice.setTextColor(
                        root.context.getColor(android.R.color.holo_green_dark)
                    )
                } else {
                    tvPrice.text = "${pkg.price} ${pkg.currency ?: "USD"}"
                    tvPrice.setTextColor(
                        root.context.getColor(android.R.color.holo_orange_dark)
                    )
                }

                // Rating
                if (pkg.ratingCount > 0) {
                    ratingBar.rating = (pkg.rating ?: 0.0).toFloat()
                    tvRatingCount.text = "(${pkg.ratingCount})"
                } else {
                    ratingBar.rating = 0f
                    tvRatingCount.text = "(Sin valoraciones)"
                }

                // Downloads
                tvDownloadCount.text = "${pkg.downloadCount} descargas"

                // Type badge
                tvPackageType.text = pkg.packageType

                // Status
                tvStatus.text = pkg.status
                tvStatus.setTextColor(
                    root.context.getColor(
                        when (pkg.status) {
                            "PUBLISHED" -> android.R.color.holo_green_dark
                            "DRAFT" -> android.R.color.holo_orange_dark
                            "DEPRECATED" -> android.R.color.darker_gray
                            "SUSPENDED" -> android.R.color.holo_red_dark
                            else -> android.R.color.darker_gray
                        }
                    )
                )

                // Listeners
                root.setOnClickListener { onPackageClick(pkg) }
                btnDownload.setOnClickListener { onDownloadClick(pkg) }
            }
        }
    }
}