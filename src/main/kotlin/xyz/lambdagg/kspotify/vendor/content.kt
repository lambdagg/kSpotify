package xyz.lambdagg.kspotify.vendor

import com.serjltt.moshi.adapters.FallbackEnum
import com.squareup.moshi.*

@FallbackEnum(name = "UNKNOWN")
enum class SpotifyContentType {
    @Json(name = "track")
    TRACK,

    @Json(name = "episode")
    EPISODE,

    @Json(name = "ad")
    AD,

    @Json(name = "unknown")
    UNKNOWN,
}

@JsonClass(generateAdapter = true)
data class SpotifyContentItem(
    val name: String,
    val artists: Array<SpotifyArtist>,
    val album: SpotifyAlbum,

    @Json(name = "duration_ms")
    val length: Int,

    val type: SpotifyContentType,
)
