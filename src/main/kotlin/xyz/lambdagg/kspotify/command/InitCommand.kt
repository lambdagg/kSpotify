package xyz.lambdagg.kspotify.command

import java.util.Timer
import kotlin.concurrent.schedule

import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import xyz.lambdagg.kspotify.AuthCallbackHttpServer

class InitCommand : AbstractSubCommand(
    help = """Initializes the CLI, completing the authentication process.
             |
             |In order to do so, you need to create a new Spotify application at
             |https://developer.spotify.com/dashboard/applications and edit settings by adding a new redirect URI such
             |as 'http://localhost:${AuthCallbackHttpServer.DEFAULT_PORT}${AuthCallbackHttpServer.CALLBACK_ROUTE}'. The
             |port can be tweaked using '-p'.
             |After doing that, you need to provide both the client id and client secret to this subcommand using
             |respectively '-i' and '-s'.""".trimMargin(),
    refreshToken = false,
) {
    private val clientId: String by option("-i")
        .help("Set the Spotify client ID")
        .required()

    private val clientSecret: String by option("-s")
        .help("Set the Spotify client secret")
        .required()

    private val port: Int by option("-p").int()
        .help("Set the port to bind the webserver to")
        .default(AuthCallbackHttpServer.DEFAULT_PORT)

    override fun run() {
        super.run()
        verbose("Now starting initialization process.")

        val server = AuthCallbackHttpServer(clientId, clientSecret, port) { credentials ->
            if (credentials == null) {
                false
            } else {
                verbose("Got Spotify token object: $credentials")
                echo("Successful authentication, saving data.")

                context.writeCredentials(credentials)

                Timer().schedule(250) {
                    this@AuthCallbackHttpServer.stop()
                    echo(
                        """You should now be able to run the program normally.
                          |If you ever need to log back in, just re-run the init subcommand.""".trimMargin()
                    )
                }

                true
            }
        }.start()

        verbose("Callback: ${server.callbackUri}")

        echo("Open the following link in your browser: ${server.authUri}")
    }
}
