package com.pubsbarsfinder.app.adapters

import com.pubsbarsfinder.app.models.PubModel

interface PubListener {
    fun onPubClick(pub: PubModel)
}