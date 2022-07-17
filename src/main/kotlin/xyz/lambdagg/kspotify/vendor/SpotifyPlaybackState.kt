package xyz.lambdagg.kspotify.vendor

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SpotifyPlaybackState(
    val device: SpotifyDevice?,

    /**
     * Whether the player is currently playing something.
     */
    @Json(name = "is_playing")
    val isPlaying: Boolean,

    /**
     * Progress of currently playing track in ms.
     */
    @Json(name = "progress_ms")
    val progress: Int?,

    /**
     * The current playing item.
     */
    val item: SpotifyContentItem?,
)
