package com.pubsbarsfinder.app.presenters

import com.pubsbarsfinder.app.models.Amenity
import com.pubsbarsfinder.app.models.PubModel
import com.pubsbarsfinder.app.models.PubStore
import timber.log.Timber

enum class EmptyState { NONE, NO_PUBS, NO_MATCHES }

interface PubListView {
    fun showPubs(pubs: List<PubModel>)
    fun showEmptyState(state: EmptyState)
    fun showActiveFilters(favouritesOnly: Boolean, amenities: Set<Amenity>)
    fun showFilterDialog(favouritesOnly: Boolean, amenities: Set<Amenity>)
    fun showDeleteAllConfirmation(count: Int)
    fun showNoPubsToDelete()
    fun openPubDetails(pubId: Long)
    fun openNewPubForm()
    fun openPubsMap()
}

class PubListPresenter(
    private val view: PubListView,
    private val store: PubStore
) {
    private var query = ""
    private var favouritesOnly = false
    private var selectedAmenities: Set<Amenity> = emptySet()

    fun onResume() = refresh()

    fun onSearchChanged(text: String) {
        query = text.trim()
        refresh()
    }

    fun onFilterClicked() = view.showFilterDialog(favouritesOnly, selectedAmenities)

    fun onFiltersApplied(favouritesOnly: Boolean, amenities: Set<Amenity>) {
        this.favouritesOnly = favouritesOnly
        selectedAmenities = amenities
        Timber.i("Filters applied: favouritesOnly=$favouritesOnly, amenities=$amenities")
        refresh()
    }

    fun onFiltersCleared() {
        favouritesOnly = false
        selectedAmenities = emptySet()
        Timber.i("Filters cleared")
        refresh()
    }

    fun onDeleteAllClicked() {
        val count = store.findAll().size
        if (count == 0) view.showNoPubsToDelete() else view.showDeleteAllConfirmation(count)
    }

    fun onDeleteAllConfirmed() {
        store.deleteAll()
        Timber.i("Deleted all pubs")
        refresh()
    }

    fun onPubClicked(pub: PubModel) = view.openPubDetails(pub.id)

    fun onFavouriteClicked(pub: PubModel) {
        store.update(pub.copy(isFavourite = !pub.isFavourite))
        Timber.i("Toggled favourite for pub ${pub.id}: ${!pub.isFavourite}")
        refresh()
    }

    fun onAddPubClicked() = view.openNewPubForm()

    fun onShowMapClicked() = view.openPubsMap()

    private fun refresh() {
        val allPubs = store.findAll()
        val pubs = allPubs.filter {
            it.title.contains(query, ignoreCase = true) &&
                    (!favouritesOnly || it.isFavourite) &&
                    it.amenities.containsAll(selectedAmenities)
        }
        view.showPubs(pubs)
        view.showActiveFilters(favouritesOnly, selectedAmenities)
        view.showEmptyState(
            when {
                pubs.isNotEmpty() -> EmptyState.NONE
                allPubs.isEmpty() -> EmptyState.NO_PUBS
                else -> EmptyState.NO_MATCHES
            }
        )
    }
}