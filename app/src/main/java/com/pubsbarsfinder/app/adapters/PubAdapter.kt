package com.pubsbarsfinder.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.pubsbarsfinder.app.R
import com.pubsbarsfinder.app.databinding.ItemPubBinding
import com.pubsbarsfinder.app.models.PubModel
import com.pubsbarsfinder.app.models.formattedAmenities
import com.pubsbarsfinder.app.models.formattedCoordinates
import com.pubsbarsfinder.app.models.labelRes

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
                .lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .joinToString(" ")
            binding.descriptionText.isVisible = pub.description.isNotEmpty()
            binding.amenitiesText.isVisible = pub.amenities.isNotEmpty()

            val context = binding.root.context

            binding.coordinatesText.text = pub.formattedCoordinates(context)

            binding.amenitiesText.text = pub.formattedAmenities(context)

            binding.root.setOnClickListener { listener.onPubClick(pub) }

            val favouriteIcon = if (pub.isFavourite) R.drawable.ic_favourite else R.drawable.ic_favourite_border
            val favouriteDescription = if (pub.isFavourite) R.string.content_description_remove_favourite else R.string.content_description_add_favourite

            binding.favouriteButton.setImageResource(favouriteIcon)
            binding.favouriteButton.contentDescription = context.getString(favouriteDescription)
            binding.favouriteButton.setOnClickListener { listener.onFavouriteClick(pub) }

            val imageUri = pub.imageUri
            if (imageUri != null) {
                binding.photoImage.setImageURI(imageUri.toUri())
                binding.photoImage.scaleType = ImageView.ScaleType.CENTER_CROP
            }
            if (imageUri == null || binding.photoImage.drawable == null) {
                binding.photoImage.setImageResource(R.drawable.ic_photo_placeholder)
                binding.photoImage.scaleType = ImageView.ScaleType.CENTER
            }
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
        val oldSize = pubs.size
        pubs = newPubs
        val newSize = pubs.size

        notifyItemRangeChanged(0, minOf(oldSize, newSize))
        if (newSize > oldSize) {
            notifyItemRangeInserted(oldSize, newSize - oldSize)
        } else if (newSize < oldSize) {
            notifyItemRangeRemoved(newSize, oldSize - newSize)
        }
    }
}