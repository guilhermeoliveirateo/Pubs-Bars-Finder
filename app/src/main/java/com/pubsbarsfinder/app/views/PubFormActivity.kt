package com.pubsbarsfinder.app.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.pubsbarsfinder.app.MainApp
import com.pubsbarsfinder.app.R
import com.pubsbarsfinder.app.databinding.ActivityPubFormBinding
import com.pubsbarsfinder.app.models.Amenity
import com.pubsbarsfinder.app.models.PubModel
import com.pubsbarsfinder.app.presenters.PubFormPresenter
import com.pubsbarsfinder.app.presenters.PubFormView
import timber.log.Timber
import java.util.Locale

class PubFormActivity : AppCompatActivity(), PubFormView {

    private lateinit var binding: ActivityPubFormBinding
    private lateinit var presenter: PubFormPresenter

    private val amenityCheckBoxes: Map<Amenity, CheckBox> by lazy {
        mapOf(
            Amenity.PET_FRIENDLY to binding.amenityPetFriendly,
            Amenity.SPORTS_ON_TV to binding.amenitySportsOnTv,
            Amenity.WHEELCHAIR_ACCESSIBLE to binding.amenityWheelchairAccessible,
            Amenity.LIVE_MUSIC to binding.amenityLiveMusic,
            Amenity.BEER_GARDEN to binding.amenityBeerGarden,
            Amenity.FOOD_SERVED to binding.amenityFoodServed,
        )
    }

    private val imagePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
                Timber.w("Could not keep access to photo: $uri")
            }
            presenter.onPhotoChosen(uri.toString())
        }
    }

    private val mapPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == RESULT_OK && data != null) {
            presenter.onLocationChosen(
                data.getDoubleExtra(MapPickerActivity.EXTRA_LATITUDE, 0.0),
                data.getDoubleExtra(MapPickerActivity.EXTRA_LONGITUDE, 0.0)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPubFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val pubId = intent.getLongExtra(EXTRA_PUB_ID, -1L).takeIf { it != -1L }
        presenter = PubFormPresenter(this, (application as MainApp).pubs, pubId)
        presenter.onCreate()

        binding.cancelButton.setOnClickListener { presenter.onCancelClicked() }
        binding.saveButton.setOnClickListener {
            presenter.onSaveClicked(
                titleText(),
                binding.descriptionInput.text.toString(),
                latitudeText(),
                longitudeText(),
                amenityCheckBoxes.filterValues { it.isChecked }.keys,
                binding.favouriteCheckBox.isChecked
            )
        }

        binding.titleInput.doAfterTextChanged { onInputChanged() }
        binding.latitudeInput.doAfterTextChanged { onInputChanged() }
        binding.longitudeInput.doAfterTextChanged { onInputChanged() }

        binding.choosePhotoButton.setOnClickListener { presenter.onChoosePhotoClicked() }
        binding.removePhotoButton.setOnClickListener { presenter.onRemovePhotoClicked() }
        binding.chooseOnMapButton.setOnClickListener {
            presenter.onChooseOnMapClicked(titleText(), latitudeText(), longitudeText())
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun showPub(pub: PubModel) {
        binding.titleInput.setText(pub.title)
        binding.descriptionInput.setText(pub.description)
        binding.latitudeInput.setText(pub.latitude.toString())
        binding.longitudeInput.setText(pub.longitude.toString())
        binding.favouriteCheckBox.isChecked = pub.isFavourite
        amenityCheckBoxes.forEach { (amenity, checkBox) ->
            checkBox.isChecked = amenity in pub.amenities
        }
    }

    override fun setEditMode(isEditing: Boolean) {
        title = getString(if (isEditing) R.string.screen_edit_pub else R.string.screen_new_pub)
        binding.saveButton.setText(if (isEditing) R.string.button_save_changes else R.string.button_save_pub)
    }

    override fun showPhoto(imageUri: String?) {
        val uri = imageUri?.toUri()
        if (uri != null) {
            binding.photoPreview.setImageURI(uri)
            binding.photoPreview.scaleType = ImageView.ScaleType.CENTER_CROP
            binding.choosePhotoButton.setText(R.string.button_change_photo)
        } else {
            binding.photoPreview.setImageResource(R.drawable.ic_photo_placeholder)
            binding.photoPreview.scaleType = ImageView.ScaleType.CENTER
            binding.choosePhotoButton.setText(R.string.button_choose_photo)
        }
        if (binding.photoPreview.drawable == null) {
            binding.photoPreview.setImageResource(R.drawable.ic_photo_placeholder)
            binding.photoPreview.scaleType = ImageView.ScaleType.CENTER
        }
        binding.removePhotoButton.isVisible = uri != null
    }

    override fun showFieldErrors(titleError: Boolean, latitudeError: Boolean, longitudeError: Boolean) {
        showFieldError(binding.titleInput, binding.titleLabel, binding.titleError, titleError)
        showFieldError(binding.latitudeInput, binding.latitudeLabel, binding.latitudeError, latitudeError)
        showFieldError(binding.longitudeInput, binding.longitudeLabel, binding.longitudeError, longitudeError)
    }

    override fun openImagePicker() {
        imagePicker.launch(arrayOf("image/*"))
    }

    override fun openMapPicker(latitude: Double?, longitude: Double?, title: String) {
        val intent = Intent(this, MapPickerActivity::class.java)
        if (latitude != null && longitude != null) {
            intent.putExtra(MapPickerActivity.EXTRA_LATITUDE, latitude)
            intent.putExtra(MapPickerActivity.EXTRA_LONGITUDE, longitude)
        }
        if (title.isNotEmpty()) {
            intent.putExtra(MapPickerActivity.EXTRA_TITLE, title)
        }
        mapPicker.launch(intent)
    }

    override fun showCoordinates(latitude: Double, longitude: Double) {
        binding.latitudeInput.setText(formatCoordinate(latitude))
        binding.longitudeInput.setText(formatCoordinate(longitude))
    }

    override fun close() = finish()

    private fun onInputChanged() {
        presenter.onInputChanged(titleText(), latitudeText(), longitudeText())
    }

    private fun titleText() = binding.titleInput.text.toString()

    private fun latitudeText() = binding.latitudeInput.text.toString()

    private fun longitudeText() = binding.longitudeInput.text.toString()

    private fun showFieldError(input: EditText, label: TextView, error: TextView, hasError: Boolean) {
        error.visibility = if (hasError) View.VISIBLE else View.GONE
        input.setBackgroundResource(if (hasError) R.drawable.bg_field_error else R.drawable.bg_field)
        label.setTextColor(ContextCompat.getColor(this, if (hasError) R.color.error else R.color.text_label))
    }

    private fun formatCoordinate(value: Double): String =
        String.format(Locale.ROOT, "%.6f", value)

    companion object {
        const val EXTRA_PUB_ID = "pub_id"
    }
}