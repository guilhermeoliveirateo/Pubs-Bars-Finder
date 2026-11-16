package com.pubsbarsfinder.app

import android.app.Application
import com.pubsbarsfinder.app.models.PubMemStore
import com.pubsbarsfinder.app.models.PubStore
import timber.log.Timber

class MainApp : Application() {

    lateinit var pubs: PubStore

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        pubs = PubMemStore()
        Timber.i("Pubs & Bars Finder started")
    }
}