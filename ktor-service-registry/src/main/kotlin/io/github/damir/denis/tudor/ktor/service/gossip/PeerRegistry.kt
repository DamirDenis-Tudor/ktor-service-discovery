package io.github.damir.denis.tudor.ktor.service.gossip

import io.github.damir.denis.tudor.ktor.service.registry.Config
import io.ktor.util.logging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.InetAddress
import kotlin.math.ceil
import kotlin.math.ln

class PeerRegistry(private val config: Config) {
    private val logger = KtorSimpleLogger(this.javaClass.name)

    private val peersMutex = Mutex()

    private var convergence: Long = 0L
    private var mutablePeers: Set<String> = emptySet()

    val peers: Set<String>
        get() = mutablePeers.toSet()

    val convergenceCycles: Long
        get() = convergence

    init {
        CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(config.peersInitialDelay * 1_000)
                updatePeers()
                delay(config.peersDiscoveryInterval * 1_000)
            }
        }
    }

    private suspend fun updatePeers() {
        peersMutex.withLock {
            mutablePeers = resolvePeers()
            convergence = calculateConvergenceRate()
            logger.trace("Expected convergence in $convergence cycles - fanout: ${config.gossipFanout}, peers: ${mutablePeers.count()}.")
        }
    }

    private suspend fun resolvePeers() = withContext(Dispatchers.IO) {
        InetAddress.getByName(config.registryHostname)
            .let { address ->
                runCatching {
                    InetAddress.getAllByName(config.registryDnsPattern)
                        .map { it.hostAddress }
                        .filter { it != address.hostAddress }
                        .toMutableSet()
                }.getOrElse {
                    logger.error("Error resolving peers: ${it.message}")
                    mutableSetOf()
                }.apply {
                    logger.trace("Peers discovered: {}", this)
                }
            }
    }

    private fun calculateConvergenceRate(): Long {
        if (mutablePeers.isEmpty()) return 0
        else if(mutablePeers.size <= config.gossipFanout) return 1

        return ceil(ln(mutablePeers.size.toDouble()) / ln(config.gossipFanout.toDouble())).toLong()
    }
}
