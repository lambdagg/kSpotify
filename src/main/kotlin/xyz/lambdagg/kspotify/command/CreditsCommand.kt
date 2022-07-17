package xyz.lambdagg.kspotify.command

class CreditsCommand : AbstractSubCommand(
    help = "Prints out the program credits",
    refreshToken = false,
) {
    override fun run() {
        super.run()

        echo("kSpotify made with <3 by @lambdagg - lambdagg.xyz")
        echo("Use `kspotify -h` for help.")
    }
}
