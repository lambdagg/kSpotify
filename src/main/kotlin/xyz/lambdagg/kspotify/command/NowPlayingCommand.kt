package xyz.lambdagg.kspotify.command

class NowPlayingCommand : AbstractSubCommand(
    name = "np",
    help = "Prints out the currently playing song"
) {
    override fun run() {
        super.run()

        val playbackItem = context.fetchPlaybackState()?.item ?: return

        // Parse the artist string.
        // If only one artist is playing, display '{artist}', if more than one
        // it should look like '{artist1} & {artist2}' or
        // '{artist1}, {artist2} & {artist3}'.
        val artistString = when (playbackItem.artists.size) {
            0 -> null  // shouldn't happen
            1 -> playbackItem.artists.first().name
            else -> {
                playbackItem.artists
                    .dropLast(1)
                    .joinToString(", ") { it.name } +
                        " & " +
                        playbackItem.artists.last().name
            }
        }

        echo("${playbackItem.name}${artistString?.let { " by $it" } ?: ""}")
    }
}
