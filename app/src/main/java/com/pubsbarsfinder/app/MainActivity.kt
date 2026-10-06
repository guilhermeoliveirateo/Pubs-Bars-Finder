package com.pubsbarsfinder.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.pubsbarsfinder.app.adapters.PubAdapter
import com.pubsbarsfinder.app.adapters.PubListener
import com.pubsbarsfinder.app.databinding.ActivityMainBinding
import com.pubsbarsfinder.app.models.PubModel

class MainActivity : AppCompatActivity(), PubListener {

    private lateinit var binding: ActivityMainBinding
    private val pubAdapter = PubAdapter(this    )

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

        binding.pubList.layoutManager = LinearLayoutManager(this)
        binding.pubList.adapter = pubAdapter
    }

    override fun onResume() {
        super.onResume()
        val pubs = PubData.store.findAll()
        pubAdapter.submitList(pubs)
        binding.emptyStateText.isVisible = pubs.isEmpty()
    }

    override fun onPubClick(pub: PubModel) {
        val intent = Intent(this, PubFormActivity::class.java)
        intent.putExtra(PubFormActivity.EXTRA_PUB_ID, pub.id)
        startActivity(intent)
    }
}