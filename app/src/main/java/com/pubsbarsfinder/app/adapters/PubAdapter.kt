package com.pubsbarsfinder.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.pubsbarsfinder.app.R
import com.pubsbarsfinder.app.databinding.ItemPubBinding
import com.pubsbarsfinder.app.models.PubModel
import kotlin.math.abs

class PubAdapter(
    private var pubs: List<PubModel>,
    private val listener: PubListener
) : RecyclerView.Adapter<PubAdapter.PubViewHolder>() {

    class PubViewHolder(
        private val binding: ItemPubBinding,
        private val listener: PubListener
    ) : RecyclerView.ViewHolder(binding.root) {

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

            binding.root.setOnClickListener { listener.onPubClick(pub) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PubViewHolder {
        val binding = ItemPubBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PubViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: PubViewHolder, position: Int) {
        holder.bind(pubs[position])
    }

    override fun getItemCount(): Int = pubs.size

    fun updatePubs(newPubs: List<PubModel>) {
        pubs = newPubs
        notifyDataSetChanged()
    }
}