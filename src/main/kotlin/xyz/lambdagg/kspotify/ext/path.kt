package xyz.lambdagg.kspotify.ext

import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.writeText

internal fun Path.ensureExists(data: String = "") = apply {
    parent.toFile().mkdirs()
    if (!this.exists())
        writeText(data)
}
