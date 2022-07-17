package xyz.lambdagg.kspotify

import java.lang.Exception
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.Date
import kotlin.io.path.*

import com.squareup.moshi.*

import org.http4k.client.Java8HttpClient
import org.http4k.core.*
import org.http4k.core.body.form
import org.http4k.filter.ClientFilters.CustomBasicAuth.withBasicAuth

import xyz.lambdagg.kspotify.ext.ensureExists
import xyz.lambdagg.kspotify.vendor.*

/**
 * The context to be shared across all commands through the
 * [AbstractCommand.context][xyz.lambdagg.kspotify.command.AbstractCommand.context]
 * property.
 *
 * @property verbose whether the program runs in verbose mode.
 * @property basePath the basePath from which resolve all other configuration
 *                    files.
 */
@OptIn(ExperimentalStdlibApi::class)
class KSpotifyContext(
    var verbose: Boolean = false,
    var basePath: Path = Path(System.getProperty("user.home")).resolve(".spotifycli"),
) {
//    /**
//     * The path pointing to the deviceId.txt configuration file.
//     */
//    val deviceIdPath: Path
//        get() = basePath.resolve("deviceId.txt")

    /**
     * The path pointing to the credentials cache file, containing sensitive
     * information like client secret along with auth and refresh tokens.
     */
    val credentialsCachePath: Path
        get() = basePath.resolve("credentials.json")

//    var deviceId: String = deviceIdPath.ensureExists("#Auto-generated. Do not modify\n")
//        .readLines(Charset.defaultCharset())
//        .firstOrNull { !it.trim().startsWith("#") } ?: ""
//        set(value) {
//            deviceIdPath.writeText(
//                """#Auto-generated. Do not modify
//                |$value""".trimMargin()
//            )
//
//            field = value
//        }

    /**
     * A [SpotifyCredentials] object which will be written and read using the
     * [readCredentials] and [writeCredentials] methods.
     */
    private lateinit var credentials: SpotifyCredentials

    private val httpClient by lazy { Java8HttpClient() /* retro-compatibility baby */ }

    init {
        // Initialize the credentials from the file
        readCredentials()
    }

    /**
     * Reads the credentials file and fills in the [credentials] property.
     *
     * @return this instance.
     */
    fun readCredentials(): KSpotifyContext = apply {
        this.credentials = SpotifyCredentials.ADAPTER.fromJson(
            this.credentialsCachePath.ensureExists(
                SpotifyCredentials.DEFAULT_JSON
            )
                .readText(Charset.defaultCharset())
        )!!
    }

    /**
     * Reads the credentials property and fills in the [credentialsCachePath]
     * file. If a [credentials] value is given, replace the current
     * [KSpotifyContext.credentials] property.
     *
     * @param credentials the potential new [KSpotifyContext.credentials] value.
     *
     * @return this instance.
     */
    fun writeCredentials(
        credentials: SpotifyCredentials? = null,
    ): KSpotifyContext = apply {
        this.credentials = credentials ?: this.credentials

        credentialsCachePath.ensureExists().writeText(
            SpotifyCredentials.ADAPTER.toJson(this.credentials)
        )
    }

    /**
     * Refreshes the access token. Will not run if
     * [SpotifyCredentials.expiresAt] is not a passed date on the [credentials]
     * property.
     *
     * @return this instance.
     */
    fun refreshAccessToken(): KSpotifyContext = apply {
        if (!credentials.expired()) {
            return this
        }

        println("Refreshing access token!")

        val response = httpClient(
            Request(Method.POST, "https://accounts.spotify.com/api/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .withBasicAuth(Credentials(credentials.clientId, credentials.clientSecret), "Authorization")
                .form("grant_type", "refresh_token")
                .form("refresh_token", credentials.refreshToken)
        )

        if (!response.status.successful) {
            throw Exception(
                "Refresh token request wasn't successful (${response.status.description}). Did you forget to run subcommand `init`?"
            )
        }

        credentials.accessToken = MOSHI.adapter<SpotifyAccessTokenResponse>()
            .fromJson(response.bodyString())!!
            .accessToken

        credentials.refreshExpiresAt()

        writeCredentials()
    }

    /**
     * Fetches the current [SpotifyPlaybackState] for the authenticated app.
     *
     * @return the playback state value, or null if the API returned 204.
     * @throws Exception if there was a problem fetching the API.
     */
    fun fetchPlaybackState(): SpotifyPlaybackState? =
        authenticatedRequest(Method.GET, "me/player")

    /**
     * Pauses the authenticated app's user's playback.
     *
     * @throws Exception if there was a problem fetching the API.
     */
    fun pausePlayback(): Unit =
        authenticatedRequest(Method.PUT, "me/player/pause")!!

    /**
     * Pauses the authenticated app's user's playback.
     *
     * @throws Exception if there was a problem fetching the API.
     */
    fun resumePlayback(): Unit =
        authenticatedRequest(Method.PUT, "me/player/play")!!

    private inline fun <reified T> authenticatedRequest(
        method: Method,
        route: String,
        body: String? = if (method == Method.PUT) "{}" else null,
        requestBlock: Request.() -> Unit = { },
        responseBlock: Response.() -> Unit = { },
        vararg queryPairs: Pair<String, String>,
    ): T? {
        refreshAccessToken()

        val req = Request(method, "https://api.spotify.com/v1/$route")
            .header("Authorization", "Bearer ${credentials.accessToken}")
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("Content-Length", (body ?: "").length.toString())
            .body(body ?: "")
            .apply {
                queryPairs.forEach { query(it.first, it.second) }
            }
            .apply(requestBlock)

        val res = httpClient(req).apply(responseBlock)

        if (!res.status.successful) {
            throw Exception("$req ${res.status.description}")
        }

        return if (res.status.code == 204) null  // No content status code (or <Unit>) should return a null object.
        else if (T::class === Unit::class) Unit as T
        else MOSHI.adapter<T>().fromJson(res.bodyString())!!
    }
}

@JsonClass(generateAdapter = true)
data class SpotifyCredentials(
    @Json(name = "access_token")
    var accessToken: String,

    @Json(name = "expires_in")
    var timeout: Int,

    @Json(name = "refresh_token")
    var refreshToken: String,

    @Json(name = "expires_at")
    var expiresAt: Long = 0,
) {
    @Json(name = "client_id")
    var clientId: String = ""

    @Json(name = "client_secret")
    var clientSecret: String = ""

    internal val expiresAtDate by lazy { Date(expiresAt) }

    @OptIn(ExperimentalStdlibApi::class)
    companion object {
        val ADAPTER by lazy { MOSHI.adapter<SpotifyCredentials>() }

        val DEFAULT_JSON: String by lazy {
            ADAPTER.toJson(
                SpotifyCredentials(
                    "",
                    0,
                    "",
                ),
            )
        }

        fun fromSpotifyJson(
            clientId: String,
            clientSecret: String,
            json: String,
        ): SpotifyCredentials =
            ADAPTER.fromJson(json)!!.apply {
                this.clientId = clientId
                this.clientSecret = clientSecret
            }
    }

    init {
        refreshExpiresAt()
    }

    fun expired() = Date().after(expiresAtDate)

    fun refreshExpiresAt() {
        expiresAt = System.currentTimeMillis() + (timeout - 1) * 1000
    }
}
