package com.pubsbarsfinder.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pubsbarsfinder.app.databinding.ActivityPubDetailsBinding
import com.pubsbarsfinder.app.models.PubModel
import com.pubsbarsfinder.app.models.formattedAmenities
import com.pubsbarsfinder.app.models.formattedCoordinates
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import timber.log.Timber

class PubDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPubDetailsBinding
    private lateinit var app: MainApp
    private var pubId: Long = -1L

    private var locationMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as MainApp
        enableEdgeToEdge()
        binding = ActivityPubDetailsBinding.inflate(layoutInflater)
        Configuration.getInstance().userAgentValue = packageName
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.screen_pub_details)
        pubId = intent.getLongExtra(PubFormActivity.EXTRA_PUB_ID, -1L)

        setUpLocationMap()
    }

    override fun onResume() {
        super.onResume()
        binding.locationMap.onResume()
        val pub = app.pubs.findOne(pubId)
        if (pub == null) {
            Timber.i("Pub $pubId not found, closing details")
            finish()
        } else {
            showPub(pub)
        }
    }

    override fun onPause() {
        binding.locationMap.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        binding.locationMap.onDetach()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.locationMap.tileProvider.clearTileCache()
        binding.locationMap.invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_pub_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val pub = app.pubs.findOne(pubId)
        return when (item.itemId) {
            R.id.action_edit -> {
                startActivity(Intent(this, PubFormActivity::class.java).putExtra(PubFormActivity.EXTRA_PUB_ID, pubId))
                true
            }
            R.id.action_share -> {
                pub?.let { sharePub(it) }
                true
            }
            R.id.action_delete -> {
                pub?.let { confirmDelete(it) }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun showPub(pub: PubModel) {
        title = pub.title
        binding.titleText.text = pub.title
        binding.descriptionText.text = pub.description.ifEmpty { getString(R.string.details_no_description) }
        binding.amenitiesText.text =
            if (pub.amenities.isEmpty()) getString(R.string.details_no_amenities) else pub.formattedAmenities(this)
        binding.coordinatesText.text = pub.formattedCoordinates(this)

        val imageUri = pub.imageUri
        if (imageUri != null) {
            binding.photoImage.setImageURI(imageUri.toUri())
        } else {
            binding.photoImage.setImageDrawable(null)
        }
        binding.photoImage.isVisible = binding.photoImage.drawable != null

        val favouriteIcon = if (pub.isFavourite) R.drawable.ic_favourite else R.drawable.ic_favourite_border
        val favouriteDescription =
            if (pub.isFavourite) R.string.content_description_remove_favourite else R.string.content_description_add_favourite
        binding.favouriteButton.setImageResource(favouriteIcon)
        binding.favouriteButton.contentDescription = getString(favouriteDescription)
        binding.favouriteButton.setOnClickListener { toggleFavourite(pub) }

        showLocation(pub)
    }

    private fun sharePub(pub: PubModel) {
        val shareText = listOf(
            pub.title,
            pub.description,
            pub.formattedCoordinates(this),
            pub.formattedAmenities(this),
            getString(R.string.format_map_link, pub.latitude.toString(), pub.longitude.toString())
        ).filter { it.isNotEmpty() }.joinToString("\n")

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        Timber.i("Sharing pub: ${pub.title}")
        startActivity(Intent.createChooser(intent, null))
    }

    private fun confirmDelete(pub: PubModel) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(getString(R.string.dialog_delete_message, pub.title))
            .setNegativeButton(R.string.button_cancel, null)
            .setPositiveButton(R.string.button_delete) { _, _ ->
                app.pubs.delete(pub.id)
                Timber.i("Deleted pub: $pub")
                finish()
            }
            .show()
    }

    private fun toggleFavourite(pub: PubModel) {
        val updatedPub = pub.copy(isFavourite = !pub.isFavourite)
        app.pubs.update(updatedPub)
        Timber.i("Toggled favourite for pub ${pub.id}: ${updatedPub.isFavourite}")
        showPub(updatedPub)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpLocationMap() {
        binding.locationMap.setTileSource(TileSourceFactory.MAPNIK)
        binding.locationMap.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        binding.locationMap.setOnTouchListener { _, _ -> true }
        binding.locationMap.overlays.add(CopyrightOverlay(this))
    }

    private fun showLocation(pub: PubModel) {
        val point = GeoPoint(pub.latitude, pub.longitude)
        binding.locationMap.controller.setZoom(LOCATION_ZOOM)
        binding.locationMap.controller.setCenter(point)

        val marker = locationMarker ?: Marker(binding.locationMap).apply {
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            binding.locationMap.overlays.add(this)
            locationMarker = this
        }
        marker.position = point
        marker.title = pub.title
        binding.locationMap.invalidate()
    }

    companion object {
        private const val LOCATION_ZOOM = 18.0
    }
}