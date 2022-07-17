package xyz.lambdagg.kspotify.vendor

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SpotifyAlbum(
    val name: String,
)
