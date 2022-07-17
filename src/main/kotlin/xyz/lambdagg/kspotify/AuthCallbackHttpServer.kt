package xyz.lambdagg.kspotify

import java.lang.Exception

import org.http4k.client.Java8HttpClient
import org.http4k.core.*
import org.http4k.core.Method.*
import org.http4k.core.body.form
import org.http4k.filter.ClientFilters.CustomBasicAuth.withBasicAuth
import org.http4k.routing.*
import org.http4k.server.*
import org.http4k.urlEncoded

import xyz.lambdagg.kspotify.ext.*

/**
 * The HTTP4k server that handles OAuth.
 *
 * @param clientId the Spotify application client ID.
 * @param clientSecret the Spotify application client secret.
 * @param targetPort the ideal port on which the server should listen on. Note
 *                   that it effectively checks if this port is free, and if not
 *                   will bind to a random free port. Use the `port` property to
 *                   get the actual port.
 * @param authCallback the callback to invoke when a user successfully
 *                     authenticates. Cancels the process if returns false.
 */
class AuthCallbackHttpServer(
    private val clientId: String,
    private val clientSecret: String,
    targetPort: Int = DEFAULT_PORT,
    private val authCallback: AuthCallbackHttpServer.(token: SpotifyCredentials?) -> Boolean = { _ -> true }
) {
    val port = if (isPortInUse(targetPort)) randomFreePort() else targetPort

    val authUri by lazy { "http://localhost:$port$AUTH_ROUTE" }

    val callbackUri by lazy { "http://localhost:$port$CALLBACK_ROUTE" }

    var shouldStopWorking = false

    companion object {
        const val DEFAULT_PORT = 8080

        const val AUTH_ROUTE = "/auth"

        const val CALLBACK_ROUTE = "/callback"
    }

    // TODO we may want to make this a little cleaner
    private val server: Http4kServer = routes(
        AUTH_ROUTE bind GET to {
            Response(Status.MOVED_PERMANENTLY)
                .header(
                    "Location",
                    "https://accounts.spotify.com/authorize?response_type=code&client_id=$clientId&scope=user-modify-playback-state%20user-read-playback-state&redirect_uri=${callbackUri.urlEncoded()}",
                )
        },

        CALLBACK_ROUTE bind GET to x@{ req ->
            req.query("error")?.let {
                return@x Response(Status.BAD_REQUEST)
                    .body("Spotify returned '$it'.")
            }

            val code = req.query("code") ?: return@x Response(Status.BAD_REQUEST).body("No code supplied.")

            try {
                println("Requesting access token.")

                val accessTokenResponse = Java8HttpClient()(
                    Request(POST, "https://accounts.spotify.com/api/token")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .withBasicAuth(Credentials(clientId, clientSecret), "Authorization")
                        .form("client_id", clientId)
                        .form("client_secret", clientSecret)
                        .form("grant_type", "authorization_code")
                        .form("code", code)
                        .form("redirect_uri", callbackUri)
                )

                if (!accessTokenResponse.status.successful) {
                    throw Exception(accessTokenResponse.status.description)
                }

                if (
                    !authCallback(
                        SpotifyCredentials.fromSpotifyJson(
                            clientId,
                            clientSecret,
                            accessTokenResponse.bodyString(),
                        )
                    )
                ) {
                    throw Exception("Callback returned false")
                }

                shouldStopWorking = true

                Response(Status.OK)
                    .header("Content-Type", "text/html;charset=utf-8")
                    .body("You can now close this window.<script>window.open('','_self').close()</script>")
            } catch (t: Throwable) {
                t.printStackTrace()
                Response(Status.INTERNAL_SERVER_ERROR)
            }
        }
    ).asServer(SunHttp(port))

    var running = false
        private set

    fun start() = apply {
        if (running) {
            throw Exception("Already running")
        }

        server.start()
        running = true
    }

    fun stop() = apply {
        if (!running) {
            throw Exception("Not running")
        }

        server.stop()
        running = false
    }
}
