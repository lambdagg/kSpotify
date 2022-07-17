package xyz.lambdagg.kspotify.vendor

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SpotifyDevice(
    val id: String,

    @Json(name = "is_active")
    val isActive: Boolean,

    val volume_percent: Int,
)
