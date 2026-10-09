package com.pubsbarsfinder.app.models

import androidx.annotation.StringRes
import com.pubsbarsfinder.app.R

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

@StringRes
fun VenueType.labelRes(): Int = when (this) {
    VenueType.PUB -> R.string.venue_type_pub
    VenueType.BAR -> R.string.venue_type_bar
    VenueType.SPORTS_BAR -> R.string.venue_type_sports_bar
    VenueType.COCKTAIL_BAR -> R.string.venue_type_cocktail_bar
    VenueType.CRAFT_BEER_BAR -> R.string.venue_type_craft_beer_bar
}
enum class Amenity {
    LIVE_MUSIC,
    BEER_GARDEN,
    FOOD_SERVED,
    SPORTS_ON_TV,
    WHEELCHAIR_ACCESSIBLE,
    PET_FRIENDLY,
}

@StringRes
fun Amenity.labelRes(): Int = when (this) {
    Amenity.PET_FRIENDLY -> R.string.amenity_pet_friendly
    Amenity.WHEELCHAIR_ACCESSIBLE -> R.string.amenity_wheelchair_accessible
    Amenity.FOOD_SERVED -> R.string.amenity_food_served
    Amenity.SPORTS_ON_TV -> R.string.amenity_sports_on_tv
    Amenity.BEER_GARDEN -> R.string.amenity_beer_garden
    Amenity.LIVE_MUSIC -> R.string.amenity_live_music
}
