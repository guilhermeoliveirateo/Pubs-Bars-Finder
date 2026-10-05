package com.pubsbarsfinder.app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.pubsbarsfinder.app.databinding.ActivityPubFormBinding
import com.pubsbarsfinder.app.models.PubModel

class PubFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPubFormBinding
    private var existingPub: PubModel? = null

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

        loadPub()

        binding.cancelButton.setOnClickListener {
            finish()
        }
        binding.saveButton.setOnClickListener {
            savePub()
        }
    }

    private fun loadPub() {
        val id = intent.getLongExtra(EXTRA_PUB_ID, -1L)
        val pub = if (id != -1L) PubData.store.findOne(id) else null
        existingPub = pub

        if (pub != null) {
            binding.titleInput.setText(pub.title)
            binding.descriptionInput.setText(pub.description)
            binding.latitudeInput.setText(pub.latitude.toString())
            binding.longitudeInput.setText(pub.longitude.toString())
            title = getString(R.string.screen_edit_pub)
            binding.saveButton.setText(R.string.button_save_changes)
        } else {
            title = getString(R.string.screen_new_pub)
        }
    }

    private fun savePub() {
        val title = binding.titleInput.text.toString().trim()
        val description = binding.descriptionInput.text.toString().trim()
        val latitude = binding.latitudeInput.text.toString().replace(',', '.').toDoubleOrNull()?.takeIf { it in -90.0..90.0 }
        val longitude = binding.longitudeInput.text.toString().replace(',', '.').toDoubleOrNull()?.takeIf { it in -180.0..180.0 }

        showFieldError(input = binding.titleInput, label = binding.titleLabel, error = binding.titleError, hasError = title.isEmpty())

        showFieldError(input = binding.latitudeInput, label = binding.latitudeLabel, error = binding.latitudeError, hasError = (latitude == null))

        showFieldError(input = binding.longitudeInput, label = binding.longitudeLabel, error = binding.longitudeError, hasError = (longitude == null))

        if (title.isEmpty() || latitude == null || longitude == null) return

        val pub = existingPub
        if (pub == null) {
            val newPub = PubData.store.create(PubModel(title = title, description = description, latitude = latitude, longitude = longitude))
            Log.i(TAG, "Create new pub: $newPub")
        } else {
            val updatedPub = pub.copy(title = title, description = description, latitude = latitude, longitude = longitude)
            PubData.store.update(updatedPub)
            Log.i(TAG, "Updated pub: $updatedPub")
        }
        finish()
    }

    private fun showFieldError(input: EditText, label: TextView, error: TextView, hasError: Boolean) {
        error.visibility = if (hasError) View.VISIBLE else View.GONE
        input.setBackgroundResource(if (hasError) R.drawable.bg_field_error else R.drawable.bg_field)
        label.setTextColor(ContextCompat.getColor(this, if (hasError) R.color.error else R.color.text_label))
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        const val EXTRA_PUB_ID = "pub_id"
        private const val TAG = "PubFormActivity"
    }
}