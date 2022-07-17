package xyz.lambdagg.kspotify.command

import com.github.ajalt.clikt.parameters.options.*
import kotlin.io.path.Path

class KSpotify : AbstractCommand(
    help = "KSpotify is a command-line tool that connects to Spotify for basic playback status and control."
) {
    init {
        versionOption("1.0")
    }

    private val basePath: String by option("--path", "-p")
        .help("Set the path pointing to the config folder")
        .default("")

    /**
     * Whether verbose mode is on.
     */
    private val verbose: Boolean by option("--verbose", "-v")
        .help("Enable verbose mode")
        .flag()

    override fun run() {
        context.verbose = this.verbose
        verbose("Enabled verbose mode.")

        if (basePath != "") {
            context.basePath = Path(basePath)
        }
    }
}

