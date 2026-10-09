package com.pubsbarsfinder.app.models

/** A pub or bar saved by the user. */
data class PubModel(
    val id: Long = 0L,
    val title: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val venueType: VenueType = VenueType.PUB,
    val amenities: Set<Amenity> = emptySet(),
)

enum class VenueType {
    PUB,
    BAR,
    SPORTS_BAR,
    COCKTAIL_BAR,
    CRAFT_BEER_BAR,
}
enum class Amenity {
    LIVE_MUSIC,
    BEER_GARDEN,
    FOOD_SERVED,
    SPORTS_ON_TV,
    WHEELCHAIR_ACCESSIBLE,
    PET_FRIENDLY,
}
