package xyz.lambdagg.kspotify.ext

import java.net.*

/**
 * @return a random free port on this host.
 */
internal fun randomFreePort(): Int =
    useServerSocket { localPort }

/**
 * @return whether the wanted port is in use.
 */
internal fun isPortInUse(port: Int): Boolean =
    (0..65535).contains(port) &&
            try {
                useServerSocket(port) { false }
            } catch (ignored: Throwable) {
                true
            }

private fun <T> useServerSocket(
    port: Int = 0,
    block: ServerSocket.() -> T,
) =
    ServerSocket().use { server ->
        server.reuseAddress = true
        server.bind(InetSocketAddress("localhost", port))
        block(server)
    }
