package com.pubsbarsfinder.app.models

/** A pub or bar saved by the user. */

data class PubModel(
    val id: Long = 0L,
    val title: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
)
