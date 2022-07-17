@file:JvmName("Main")

package xyz.lambdagg.kspotify

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import xyz.lambdagg.kspotify.command.*

/**
 * Application entry point.
 *
 * @param args the command line arguments to pass to the [KSpotify] Clikt
 * command.
 */
fun main(args: Array<String>): Unit =
    KSpotify()
        .subcommands(
            CreditsCommand(),
            InitCommand(),
            NowPlayingCommand(),
            PlayPauseCommand(),
        )
        .main(args)
