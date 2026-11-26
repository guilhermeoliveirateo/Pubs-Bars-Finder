package com.pubsbarsfinder.app.presenters

import com.pubsbarsfinder.app.models.PubModel
import com.pubsbarsfinder.app.models.PubStore
import timber.log.Timber

interface PubsMapView {
    fun showMarkers(pubs: List<PubModel>)
    fun zoomToPubs(pubs: List<PubModel>)
    fun moveToPub(pub: PubModel)
    fun showSelectedPub(pub: PubModel?)
    fun openPubDetails(pubId: Long)
}

class PubsMapPresenter(
    private val view: PubsMapView,
    private val store: PubStore
) {
    private var selectedPubId: Long? = null
    private var hasZoomedToPubs = false

    fun onResume() {
        val pubs = store.findAll()
        view.showMarkers(pubs)
        if (!hasZoomedToPubs) {
            view.zoomToPubs(pubs)
            hasZoomedToPubs = true
        }
        showSelectedPub()
        Timber.i("Showing ${pubs.size} pubs on the map")
    }

    fun onMarkerClicked(pub: PubModel) {
        selectedPubId = pub.id
        view.moveToPub(pub)
        showSelectedPub()
        Timber.i("Selected pub on map: ${pub.title}")
    }

    fun onCardClicked(pub: PubModel) = view.openPubDetails(pub.id)

    fun onFavouriteClicked(pub: PubModel) {
        store.update(pub.copy(isFavourite = !pub.isFavourite))
        Timber.i("Toggled favourite for pub ${pub.id}: ${!pub.isFavourite}")
        showSelectedPub()
    }

    private fun showSelectedPub() {
        val pub = selectedPubId?.let { store.findOne(it) }
        if (pub == null) selectedPubId = null
        view.showSelectedPub(pub)
    }
}