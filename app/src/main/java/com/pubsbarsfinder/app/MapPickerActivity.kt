package com.pubsbarsfinder.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.pubsbarsfinder.app.databinding.ActivityMapPickerBinding
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

class MapPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapPickerBinding
    private lateinit var selectedLocation: GeoPoint
    private var marker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName

        enableEdgeToEdge()
        binding = ActivityMapPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        title = getString(R.string.screen_choose_location)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        selectedLocation = GeoPoint(
            intent.getDoubleExtra(EXTRA_LATITUDE, DEFAULT_LATITUDE),
            intent.getDoubleExtra(EXTRA_LONGITUDE, DEFAULT_LONGITUDE)
        )

        setUpMap()
        binding.confirmLocationButton.setOnClickListener { returnLocation() }
    }

    private fun setUpMap() {
        binding.map.setTileSource(TileSourceFactory.MAPNIK)
        binding.map.setMultiTouchControls(true)
        binding.map.controller.setZoom(DEFAULT_ZOOM)
        binding.map.controller.setCenter(selectedLocation)
        setSelectedLocation(selectedLocation)

        val tapReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                setSelectedLocation(point)
                return true
            }

            override fun longPressHelper(point: GeoPoint): Boolean = false
        }
        binding.map.overlays.add(0, MapEventsOverlay(tapReceiver))
    }

    private fun setSelectedLocation(location: GeoPoint) {
        selectedLocation = location
        val currentMarker = marker ?: Marker(binding.map).apply {
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = intent.getStringExtra(EXTRA_TITLE) ?: getString(R.string.marker_selected_location)
            isDraggable = true
            setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                override fun onMarkerDragStart(marker: Marker) {}
                override fun onMarkerDrag(marker: Marker) {}
                override fun onMarkerDragEnd(marker: Marker) {
                    selectedLocation = marker.position
                }
            })
            binding.map.overlays.add(this)
            marker = this
        }
        currentMarker.position = location
        binding.map.invalidate()
    }

    private fun returnLocation() {
        val result = Intent()
            .putExtra(EXTRA_LATITUDE, selectedLocation.latitude)
            .putExtra(EXTRA_LONGITUDE, selectedLocation.longitude)
        setResult(RESULT_OK, result)
        finish()
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        binding.map.onPause()
        super.onPause()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        const val EXTRA_LATITUDE = "latitude"
        const val EXTRA_LONGITUDE = "longitude"
        const val EXTRA_TITLE = "title"

        private const val DEFAULT_LATITUDE = 52.258330
        private const val DEFAULT_LONGITUDE = -7.111940
        private const val DEFAULT_ZOOM = 16.0
    }
}