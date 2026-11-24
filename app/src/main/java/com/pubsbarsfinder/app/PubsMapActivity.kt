package com.pubsbarsfinder.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.pubsbarsfinder.app.adapters.PubListener
import com.pubsbarsfinder.app.adapters.bindPub
import com.pubsbarsfinder.app.databinding.ActivityPubsMapBinding
import com.pubsbarsfinder.app.models.PubModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import timber.log.Timber

class PubsMapActivity : AppCompatActivity(), PubListener {

    private lateinit var binding: ActivityPubsMapBinding
    private lateinit var app: MainApp
    private var selectedPubId: Long? = null
    private var hasZoomedToPubs = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as MainApp
        Configuration.getInstance().userAgentValue = packageName

        enableEdgeToEdge()
        binding = ActivityPubsMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        title = getString(R.string.screen_pubs_map)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.map.setTileSource(TileSourceFactory.MAPNIK)
        binding.map.setMultiTouchControls(true)
    }

    private fun showPubMarkers() {
        val pubs = app.pubs.findAll()
        binding.map.overlays.clear()

        pubs.forEach { pub ->
            val marker = Marker(binding.map).apply {
                position = GeoPoint(pub.latitude, pub.longitude)
                title = pub.title
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setOnMarkerClickListener { _, _ ->
                    selectPub(pub)
                    true
                }
            }
            binding.map.overlays.add(marker)
        }
        binding.map.overlays.add(CopyrightOverlay(this))

        if (!hasZoomedToPubs) {
            zoomToPubs(pubs)
            hasZoomedToPubs = true
        }
        binding.map.invalidate()
        Timber.i("Showing ${pubs.size} pubs on the map")
    }

    private fun zoomToPubs(pubs: List<PubModel>) {
        val points = pubs.map { GeoPoint(it.latitude, it.longitude) }
        val controller = binding.map.controller

        when (points.size) {
            0 -> {
                controller.setZoom(MapPickerActivity.DEFAULT_ZOOM)
                controller.setCenter(GeoPoint(MapPickerActivity.DEFAULT_LATITUDE, MapPickerActivity.DEFAULT_LONGITUDE))
                Toast.makeText(this, R.string.message_no_pubs_on_map, Toast.LENGTH_SHORT).show()
            }
            1 -> {
                controller.setZoom(MapPickerActivity.DEFAULT_ZOOM)
                controller.setCenter(points.first())
            }
            else -> binding.map.post {
                binding.map.zoomToBoundingBox(BoundingBox.fromGeoPoints(points), false, 100)
            }
        }
    }

    private fun selectPub(pub: PubModel) {
        selectedPubId = pub.id
        showSelectedPub()
        binding.map.controller.animateTo(GeoPoint(pub.latitude, pub.longitude))
        Timber.i("Selected pub on map: ${pub.title}")
    }

    private fun showSelectedPub() {
        val pub = selectedPubId?.let { app.pubs.findOne(it) }
        if (pub == null) {
            selectedPubId = null
        } else {
            binding.selectedPubCard.bindPub(pub, this)
        }
        binding.selectedPubCard.root.isVisible = pub != null
        binding.selectPubHintText.isVisible = pub == null
    }

    override fun onPubClick(pub: PubModel) {
        val intent = Intent(this, PubDetailsActivity::class.java)
            .putExtra(PubFormActivity.EXTRA_PUB_ID, pub.id)
        startActivity(intent)
    }

    override fun onFavouriteClick(pub: PubModel) {
        app.pubs.update(pub.copy(isFavourite = !pub.isFavourite))
        Timber.i("Toggled favourite for pub ${pub.id}: ${!pub.isFavourite}")
        showSelectedPub()
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
        showPubMarkers()
        showSelectedPub()
    }

    override fun onPause() {
        binding.map.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        binding.map.onDetach()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.map.tileProvider.clearTileCache()
        binding.map.invalidate()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}