package com.pubsbarsfinder.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.pubsbarsfinder.app.databinding.ActivityPubsMapBinding
import com.pubsbarsfinder.app.models.PubModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class PubsMapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPubsMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
        showPubMarkers()
    }

    override fun onPause() {
        binding.map.onPause()
        super.onPause()
    }

    private fun showPubMarkers() {
        val pubs = PubData.store.findAll()
        binding.map.overlays.clear()

        pubs.forEach { pub ->
            val marker = Marker(binding.map).apply {
                position = GeoPoint(pub.latitude, pub.longitude)
                title = pub.title
                subDescription = getString(R.string.marker_tap_again_to_edit)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setOnMarkerClickListener { clickedMarker, _ ->
                    if (clickedMarker.isInfoWindowShown) {
                        openPub(pub)
                    } else {
                        clickedMarker.showInfoWindow()
                    }
                    true
                }
            }
            binding.map.overlays.add(marker)
        }

        zoomToPubs(pubs)
        binding.map.invalidate()
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

    private fun openPub(pub: PubModel) {
        val intent = Intent(this, PubFormActivity::class.java)
            .putExtra(PubFormActivity.EXTRA_PUB_ID, pub.id)
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}