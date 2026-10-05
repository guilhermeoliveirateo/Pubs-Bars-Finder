package com.pubsbarsfinder.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pubsbarsfinder.app.R
import com.pubsbarsfinder.app.databinding.ItemPubBinding
import com.pubsbarsfinder.app.models.PubModel
import kotlin.math.abs

class PubAdapter : ListAdapter<PubModel, PubAdapter.PubViewHolder>(PubDiffCallback) {

    class PubViewHolder(private val binding: ItemPubBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pub: PubModel) {
            binding.titleText.text = pub.title
            binding.descriptionText.text = pub.description
            binding.descriptionText.isVisible = pub.description.isNotEmpty()

            val context = binding.root.context
            val latitudeDirection = context.getString(if (pub.latitude >= 0) R.string.direction_north else R.string.direction_south)
            val longitudeDirection = context.getString(if (pub.longitude >= 0) R.string.direction_east else R.string.direction_west)

            binding.coordinatesText.text = context.getString(
                R.string.format_coordinates,
                abs(pub.latitude), latitudeDirection,
                abs(pub.longitude), longitudeDirection
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PubViewHolder {
        val binding = ItemPubBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PubViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PubViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object PubDiffCallback : DiffUtil.ItemCallback<PubModel>() {
        override fun areItemsTheSame(oldItem: PubModel, newItem: PubModel) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: PubModel, newItem: PubModel) = oldItem == newItem
    }
}