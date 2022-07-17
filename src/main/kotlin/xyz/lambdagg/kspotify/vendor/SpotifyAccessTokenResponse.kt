package xyz.lambdagg.kspotify.vendor

import com.squareup.moshi.*

@JsonClass(generateAdapter = true)
data class SpotifyAccessTokenResponse(
    @Json(name = "access_token")
    val accessToken: String,
)
