package com.pubsbarsfinder.app.presenters

import com.pubsbarsfinder.app.models.PubModel
import com.pubsbarsfinder.app.models.PubStore
import timber.log.Timber

interface PubDetailsView {
    fun showPub(pub: PubModel)
    fun openEditForm(pubId: Long)
    fun sharePub(pub: PubModel)
    fun showDeleteConfirmation(pub: PubModel)
    fun close()
}

class PubDetailsPresenter(
    private val view: PubDetailsView,
    private val store: PubStore,
    private val pubId: Long
) {
    fun onResume() {
        val pub = store.findOne(pubId)
        if (pub == null) {
            Timber.i("Pub $pubId not found, closing details")
            view.close()
        } else {
            view.showPub(pub)
        }
    }

    fun onFavouriteClicked() {
        val pub = store.findOne(pubId) ?: return
        val updatedPub = pub.copy(isFavourite = !pub.isFavourite)
        store.update(updatedPub)
        Timber.i("Toggled favourite for pub $pubId: ${updatedPub.isFavourite}")
        view.showPub(updatedPub)
    }

    fun onEditClicked() = view.openEditForm(pubId)

    fun onShareClicked() {
        val pub = store.findOne(pubId) ?: return
        Timber.i("Sharing pub: ${pub.title}")
        view.sharePub(pub)
    }

    fun onDeleteClicked() {
        val pub = store.findOne(pubId) ?: return
        view.showDeleteConfirmation(pub)
    }

    fun onDeleteConfirmed() {
        store.delete(pubId)
        Timber.i("Deleted pub $pubId")
        view.close()
    }
}