package com.pubsbarsfinder.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.pubsbarsfinder.app.databinding.ActivityMapPickerBinding

class MapPickerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapPickerBinding
    private lateinit var map: GoogleMap
    private lateinit var selectedLocation: LatLng
    private var marker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        selectedLocation = LatLng(
            intent.getDoubleExtra(EXTRA_LATITUDE, DEFAULT_LATITUDE),
            intent.getDoubleExtra(EXTRA_LONGITUDE, DEFAULT_LONGITUDE)
        )

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.confirmLocationButton.setOnClickListener { returnLocation() }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, DEFAULT_ZOOM))
        setSelectedLocation(selectedLocation)

        map.setOnMapClickListener { location -> setSelectedLocation(location) }

        map.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {}
            override fun onMarkerDrag(marker: Marker) {}
            override fun onMarkerDragEnd(marker: Marker) {
                selectedLocation = marker.position
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setSelectedLocation(location: LatLng) {
        selectedLocation = location
        val currentMarker = marker
        if (currentMarker == null) {
            marker = map.addMarker(
                MarkerOptions()
                    .position(location)
                    .title(intent.getStringExtra(EXTRA_TITLE) ?: getString(R.string.marker_selected_location))
                    .draggable(true)
            )
        } else {
            currentMarker.position = location
        }
    }

    private fun returnLocation() {
        val result = Intent()
            .putExtra(EXTRA_LATITUDE, selectedLocation.latitude)
            .putExtra(EXTRA_LONGITUDE, selectedLocation.longitude)
        setResult(RESULT_OK, result)
        finish()
    }

    companion object {
        const val EXTRA_LATITUDE = "latitude"
        const val EXTRA_LONGITUDE = "longitude"
        const val EXTRA_TITLE = "title"

        private const val DEFAULT_LATITUDE = 52.25833
        private const val DEFAULT_LONGITUDE = -7.11194
        private const val DEFAULT_ZOOM = 16f
    }
}