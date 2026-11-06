package com.pubsbarsfinder.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pubsbarsfinder.app.adapters.PubAdapter
import com.pubsbarsfinder.app.adapters.PubListener
import com.pubsbarsfinder.app.databinding.ActivityMainBinding
import com.pubsbarsfinder.app.models.Amenity
import com.pubsbarsfinder.app.models.PubModel
import com.pubsbarsfinder.app.models.labelRes

class MainActivity : AppCompatActivity(), PubListener {

    private lateinit var binding: ActivityMainBinding

    private var favouritesOnly: Boolean = false
    private var selectedAmenities: Set<Amenity> = emptySet()

    private val pubAdapter = PubAdapter(emptyList(), this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.addPubButton.setOnClickListener {
            startActivity(Intent(this, PubFormActivity::class.java))
        }

        binding.searchInput.doAfterTextChanged {
            showPubs()
        }

        binding.pubList.layoutManager = LinearLayoutManager(this)
        binding.pubList.adapter = pubAdapter
    }

    override fun onResume() {
        super.onResume()
        showPubs()
    }

    override fun onPubClick(pub: PubModel) {
        val intent = Intent(this, PubFormActivity::class.java)
        intent.putExtra(PubFormActivity.EXTRA_PUB_ID, pub.id)
        startActivity(intent)
    }

    override fun onFavouriteClick(pub: PubModel) {
        PubData.store.update(pub.copy(isFavourite = !pub.isFavourite))
        showPubs()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                showFilterDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showFilterDialog() {
        val amenities = Amenity.entries

        val labels = (listOf(getString(R.string.filter_favourites_only)) +
                amenities.map { getString(it.labelRes()) }).toTypedArray()

        val checked = (listOf(favouritesOnly) +
                amenities.map { it in selectedAmenities }).toBooleanArray()

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.action_filter)
            .setMultiChoiceItems(labels, checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            .setPositiveButton(R.string.button_apply) { _, _ ->
                favouritesOnly = checked[0]
                selectedAmenities = amenities.filterIndexed { index, _ -> checked[index + 1] }.toSet()
                showPubs()
            }
            .setNeutralButton(R.string.button_clear) { _, _ ->
                favouritesOnly = false
                selectedAmenities = emptySet()
                showPubs()
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }

    private fun showPubs() {
        val query = binding.searchInput.text.toString().trim()
        val allPubs = PubData.store.findAll()
        val pubs = allPubs.filter { it.title.contains(query, ignoreCase = true) && (!favouritesOnly || it.isFavourite) && it.amenities.containsAll(selectedAmenities) }

        pubAdapter.updatePubs(pubs)

        val activeFilters = buildList {
            if (favouritesOnly) add(getString(R.string.filter_favourites_only))
            selectedAmenities.forEach { add(getString(it.labelRes())) }
        }

        binding.activeFiltersText.text = getString(
            R.string.format_filter,
            activeFilters.joinToString(getString(R.string.separator_amenities))
        )

        binding.activeFiltersText.isVisible = activeFilters.isNotEmpty()

        binding.emptyStateText.isVisible = pubs.isEmpty()
        binding.emptyStateText.setText(
            if (allPubs.isEmpty()) R.string.empty_list_state_text else R.string.empty_state_no_search_results
        )
    }
}