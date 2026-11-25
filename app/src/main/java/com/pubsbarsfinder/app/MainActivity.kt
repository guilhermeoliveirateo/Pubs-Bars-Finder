package com.pubsbarsfinder.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
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
import com.pubsbarsfinder.app.presenters.EmptyState
import com.pubsbarsfinder.app.presenters.PubListPresenter
import com.pubsbarsfinder.app.presenters.PubListView

class MainActivity : AppCompatActivity(), PubListView, PubListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var presenter: PubListPresenter

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

        presenter = PubListPresenter(this, (application as MainApp).pubs)

        binding.pubList.layoutManager = LinearLayoutManager(this)
        binding.pubList.adapter = pubAdapter

        binding.addPubButton.setOnClickListener { presenter.onAddPubClicked() }
        binding.searchInput.doAfterTextChanged { presenter.onSearchChanged(it.toString()) }
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                presenter.onFilterClicked()
                true
            }
            R.id.action_delete_all -> {
                presenter.onDeleteAllClicked()
                true
            }
            R.id.action_show_map -> {
                presenter.onShowMapClicked()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPubClick(pub: PubModel) = presenter.onPubClicked(pub)

    override fun onFavouriteClick(pub: PubModel) = presenter.onFavouriteClicked(pub)

    override fun showPubs(pubs: List<PubModel>) = pubAdapter.updatePubs(pubs)

    override fun showEmptyState(state: EmptyState) {
        binding.emptyStateText.isVisible = state != EmptyState.NONE
        when (state) {
            EmptyState.NO_PUBS -> binding.emptyStateText.setText(R.string.empty_list_state_text)
            EmptyState.NO_MATCHES -> binding.emptyStateText.setText(R.string.empty_state_no_search_results)
            EmptyState.NONE -> Unit
        }
    }

    override fun showActiveFilters(favouritesOnly: Boolean, amenities: Set<Amenity>) {
        val activeFilters = buildList {
            if (favouritesOnly) add(getString(R.string.filter_favourites_only))
            amenities.forEach { add(getString(it.labelRes())) }
        }
        binding.activeFiltersText.text = getString(
            R.string.format_filter,
            activeFilters.joinToString(getString(R.string.separator_amenities))
        )
        binding.activeFiltersText.isVisible = activeFilters.isNotEmpty()
    }

    override fun showFilterDialog(favouritesOnly: Boolean, amenities: Set<Amenity>) {
        val allAmenities = Amenity.entries

        val labels = (listOf(getString(R.string.filter_favourites_only)) +
                allAmenities.map { getString(it.labelRes()) }).toTypedArray()

        val checked = (listOf(favouritesOnly) +
                allAmenities.map { it in amenities }).toBooleanArray()

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.action_filter)
            .setMultiChoiceItems(labels, checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            .setPositiveButton(R.string.button_apply) { _, _ ->
                presenter.onFiltersApplied(
                    checked[0],
                    allAmenities.filterIndexed { index, _ -> checked[index + 1] }.toSet()
                )
            }
            .setNeutralButton(R.string.button_clear) { _, _ -> presenter.onFiltersCleared() }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }

    override fun showDeleteAllConfirmation(count: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_delete_all_title)
            .setMessage(getString(R.string.dialog_delete_all_message, count))
            .setNegativeButton(R.string.button_cancel, null)
            .setPositiveButton(R.string.button_delete_all) { _, _ -> presenter.onDeleteAllConfirmed() }
            .show()
    }

    override fun showNoPubsToDelete() {
        Toast.makeText(this, R.string.empty_list_state_delete_all_text, Toast.LENGTH_SHORT).show()
    }

    override fun openPubDetails(pubId: Long) {
        val intent = Intent(this, PubDetailsActivity::class.java)
            .putExtra(PubFormActivity.EXTRA_PUB_ID, pubId)
        startActivity(intent)
    }

    override fun openNewPubForm() {
        startActivity(Intent(this, PubFormActivity::class.java))
    }

    override fun openPubsMap() {
        startActivity(Intent(this, PubsMapActivity::class.java))
    }
}