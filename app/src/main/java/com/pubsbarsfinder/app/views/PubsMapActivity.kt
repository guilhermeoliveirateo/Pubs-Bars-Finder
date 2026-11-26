package com.pubsbarsfinder.app.views

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.pubsbarsfinder.app.MainApp
import com.pubsbarsfinder.app.R
import com.pubsbarsfinder.app.adapters.PubListener
import com.pubsbarsfinder.app.adapters.bindPub
import com.pubsbarsfinder.app.databinding.ActivityPubsMapBinding
import com.pubsbarsfinder.app.models.PubModel
import com.pubsbarsfinder.app.presenters.PubsMapPresenter
import com.pubsbarsfinder.app.presenters.PubsMapView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker

class PubsMapActivity : AppCompatActivity(), PubsMapView, PubListener {

    private lateinit var binding: ActivityPubsMapBinding
    private lateinit var presenter: PubsMapPresenter

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

        presenter = PubsMapPresenter(this, (application as MainApp).pubs)
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
        presenter.onResume()
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

    // PubListener: taps on the selected pub's card

    override fun onPubClick(pub: PubModel) = presenter.onCardClicked(pub)

    override fun onFavouriteClick(pub: PubModel) = presenter.onFavouriteClicked(pub)

    // PubsMapView: what the presenter asks the screen to show or do

    override fun showMarkers(pubs: List<PubModel>) {
        binding.map.overlays.clear()

        pubs.forEach { pub ->
            val marker = Marker(binding.map).apply {
                position = GeoPoint(pub.latitude, pub.longitude)
                title = pub.title
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setOnMarkerClickListener { _, _ ->
                    presenter.onMarkerClicked(pub)
                    true
                }
            }
            binding.map.overlays.add(marker)
        }
        binding.map.overlays.add(CopyrightOverlay(this))
        binding.map.invalidate()
    }

    override fun zoomToPubs(pubs: List<PubModel>) {
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

    override fun moveToPub(pub: PubModel) {
        binding.map.controller.animateTo(GeoPoint(pub.latitude, pub.longitude))
    }

    override fun showSelectedPub(pub: PubModel?) {
        if (pub != null) {
            binding.selectedPubCard.bindPub(pub, this)
        }
        binding.selectedPubCard.root.isVisible = pub != null
        binding.selectPubHintText.isVisible = pub == null
    }

    override fun openPubDetails(pubId: Long) {
        val intent = Intent(this, PubDetailsActivity::class.java)
            .putExtra(PubFormActivity.EXTRA_PUB_ID, pubId)
        startActivity(intent)
    }
}