package xyz.lambdagg.kspotify.vendor

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SpotifyArtist(
    val name: String,
)
