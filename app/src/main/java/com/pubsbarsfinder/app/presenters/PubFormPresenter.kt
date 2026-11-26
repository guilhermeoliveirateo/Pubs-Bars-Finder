package com.pubsbarsfinder.app.presenters

import com.pubsbarsfinder.app.models.Amenity
import com.pubsbarsfinder.app.models.PubModel
import com.pubsbarsfinder.app.models.PubStore
import timber.log.Timber

interface PubFormView {
    fun showPub(pub: PubModel)
    fun setEditMode(isEditing: Boolean)
    fun showPhoto(imageUri: String?)
    fun showFieldErrors(titleError: Boolean, latitudeError: Boolean, longitudeError: Boolean)
    fun openImagePicker()
    fun openMapPicker(latitude: Double?, longitude: Double?, title: String)
    fun showCoordinates(latitude: Double, longitude: Double)
    fun close()
}

class PubFormPresenter(
    private val view: PubFormView,
    private val store: PubStore,
    pubId: Long?
) {
    private val existingPub: PubModel? = pubId?.let { store.findOne(it) }
    private var imageUri: String? = existingPub?.imageUri
    private var hasTriedToSave = false

    fun onCreate() {
        existingPub?.let { view.showPub(it) }
        view.setEditMode(existingPub != null)
        view.showPhoto(imageUri)
    }

    fun onInputChanged(title: String, latitudeText: String, longitudeText: String) {
        if (hasTriedToSave) {
            view.showFieldErrors(
                title.isBlank(),
                parseCoordinate(latitudeText, MAX_LATITUDE) == null,
                parseCoordinate(longitudeText, MAX_LONGITUDE) == null
            )
        }
    }

    fun onSaveClicked(
        title: String,
        description: String,
        latitudeText: String,
        longitudeText: String,
        amenities: Set<Amenity>,
        isFavourite: Boolean
    ) {
        hasTriedToSave = true
        val cleanTitle = title.trim()
        val latitude = parseCoordinate(latitudeText, MAX_LATITUDE)
        val longitude = parseCoordinate(longitudeText, MAX_LONGITUDE)

        view.showFieldErrors(cleanTitle.isEmpty(), latitude == null, longitude == null)
        if (cleanTitle.isEmpty() || latitude == null || longitude == null) return

        val pub = existingPub
        if (pub == null) {
            val newPub = store.create(
                PubModel(
                    title = cleanTitle,
                    description = description.trim(),
                    latitude = latitude,
                    longitude = longitude,
                    amenities = amenities,
                    isFavourite = isFavourite,
                    imageUri = imageUri
                )
            )
            Timber.i("Created pub: $newPub")
        } else {
            val updatedPub = pub.copy(
                title = cleanTitle,
                description = description.trim(),
                latitude = latitude,
                longitude = longitude,
                amenities = amenities,
                isFavourite = isFavourite,
                imageUri = imageUri
            )
            store.update(updatedPub)
            Timber.i("Updated pub: $updatedPub")
        }
        view.close()
    }

    fun onCancelClicked() = view.close()

    fun onChoosePhotoClicked() = view.openImagePicker()

    fun onPhotoChosen(uri: String) {
        imageUri = uri
        Timber.i("Photo chosen: $uri")
        view.showPhoto(imageUri)
    }

    fun onRemovePhotoClicked() {
        imageUri = null
        Timber.i("Photo removed")
        view.showPhoto(null)
    }

    fun onChooseOnMapClicked(title: String, latitudeText: String, longitudeText: String) {
        val latitude = parseCoordinate(latitudeText, MAX_LATITUDE)
        val longitude = parseCoordinate(longitudeText, MAX_LONGITUDE)
        val bothValid = latitude != null && longitude != null
        view.openMapPicker(
            if (bothValid) latitude else null,
            if (bothValid) longitude else null,
            title.trim()
        )
    }

    fun onLocationChosen(latitude: Double, longitude: Double) {
        Timber.i("Location chosen on map: $latitude, $longitude")
        view.showCoordinates(latitude, longitude)
    }

    private fun parseCoordinate(text: String, limit: Double): Double? =
        text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it in -limit..limit }

    companion object {
        private const val MAX_LATITUDE = 90.0
        private const val MAX_LONGITUDE = 180.0
    }
}