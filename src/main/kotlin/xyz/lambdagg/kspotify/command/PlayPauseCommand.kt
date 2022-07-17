package xyz.lambdagg.kspotify.command

class PlayPauseCommand : AbstractSubCommand(
    name = "pp",
    help = "Stops or resumes the playback"
) {
    override fun run() {
        super.run()

        val playbackState = context.fetchPlaybackState()

        if (playbackState == null) {
            TODO("Handle null playback state")
        } else if (!playbackState.isPlaying) {
            context.resumePlayback()
        } else {
            context.pausePlayback()
        }
    }
}
