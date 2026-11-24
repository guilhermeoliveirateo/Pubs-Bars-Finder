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

class PubAdapter(
    private var pubs: List<PubModel>,
    private val listener: PubListener
) : RecyclerView.Adapter<PubAdapter.PubViewHolder>() {

    class PubViewHolder(
        private val binding: ItemPubBinding,
        private val listener: PubListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pub: PubModel) {
            binding.bindPub(pub, listener)
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

fun ItemPubBinding.bindPub(pub: PubModel, listener: PubListener) {
    val context = root.context

    titleText.text = pub.title
    descriptionText.text = pub.description
        .lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString(" ")
    descriptionText.isVisible = pub.description.isNotEmpty()

    amenitiesText.text = pub.formattedAmenities(context)
    amenitiesText.isVisible = pub.amenities.isNotEmpty()

    coordinatesText.text = pub.formattedCoordinates(context)

    val favouriteIcon = if (pub.isFavourite) R.drawable.ic_favourite else R.drawable.ic_favourite_border
    val favouriteDescription =
        if (pub.isFavourite) R.string.content_description_remove_favourite else R.string.content_description_add_favourite
    favouriteButton.setImageResource(favouriteIcon)
    favouriteButton.contentDescription = context.getString(favouriteDescription)
    favouriteButton.setOnClickListener { listener.onFavouriteClick(pub) }

    val imageUri = pub.imageUri
    if (imageUri != null) {
        photoImage.setImageURI(imageUri.toUri())
        photoImage.scaleType = ImageView.ScaleType.CENTER_CROP
    }
    if (imageUri == null || photoImage.drawable == null) {
        photoImage.setImageResource(R.drawable.ic_photo_placeholder)
        photoImage.scaleType = ImageView.ScaleType.CENTER
    }

    root.setOnClickListener { listener.onPubClick(pub) }
}
