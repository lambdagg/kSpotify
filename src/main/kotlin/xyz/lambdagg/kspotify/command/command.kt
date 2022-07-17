package xyz.lambdagg.kspotify.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import xyz.lambdagg.kspotify.KSpotifyContext

abstract class AbstractCommand(name: String? = null, help: String) : CliktCommand(help, name = name) {
    open val context: KSpotifyContext by lazy {
        currentContext.findOrSetObject { KSpotifyContext() }
    }

    fun verbose(message: Any? = null, err: Boolean = false) {
        if (context.verbose) {
            echo(message ?: "", err = err)
        }
    }

    fun errorVerbose(message: String, throwable: Throwable, fullyVerbose: Boolean = false) {
        var fullMessage = "$message."

        if (context.verbose) {
            fullMessage += " Run with `-v` for more insights."
            throwable.printStackTrace()
        } else {
            echo(fullMessage, err = true)
        }

        if (fullyVerbose) {
            verbose(fullMessage, err = true)
        } else {
            echo(fullMessage, err = true)
        }
    }
}

abstract class AbstractSubCommand(
    name: String? = null,
    help: String,
    private val refreshToken: Boolean = true,
) : AbstractCommand(name, help) {
    override val context: KSpotifyContext by requireObject()

    override fun run() {
        if (refreshToken) {
            context.refreshAccessToken()
        }
    }
}
