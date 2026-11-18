package com.pubsbarsfinder.app

import android.app.Application
import com.pubsbarsfinder.app.models.PubJSONStore
import com.pubsbarsfinder.app.models.PubStore
import timber.log.Timber
import java.io.File

class MainApp : Application() {

    lateinit var pubs: PubStore

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        pubs = PubJSONStore(File(filesDir, "pubs.json"))
        Timber.i("Pubs & Bars Finder started")
    }
}