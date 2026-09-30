package com.pubsbarsfinder.app

import com.pubsbarsfinder.app.models.PubMemStore
import com.pubsbarsfinder.app.models.PubStore

/** Shared pub store used by all screens in the app. */
object PubData {
    val store: PubStore = PubMemStore()
}