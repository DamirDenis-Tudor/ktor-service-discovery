package io.github.damir.denis.tudor.ktor.registry.gossip

import io.github.damir.denis.tudor.ktor.registry.plugin.RegistryConfig
import io.ktor.util.logging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.InetAddress
import kotlin.math.ceil
import kotlin.math.ln

class PeerRegistry {
    private val logger = KtorSimpleLogger(this.javaClass.name)

    private val peersMutex = Mutex()

    private var convergence: Long = 0L
    private var mutablePeers: Set<String> = emptySet()

    val peers: Set<String>
        get() = mutablePeers.toSet()

    val convergenceCycles: Long
        get() = convergence

    companion object {
        lateinit var host: String
        lateinit var registryConfig: RegistryConfig
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(registryConfig.peersInitialDelay * 1_000)
                updatePeers()
                delay(registryConfig.peersDiscoveryInterval * 1_000)
            }
        }
    }

    private suspend fun updatePeers() {
        peersMutex.withLock {
            mutablePeers = resolvePeers()
            convergence = calculateConvergenceRate()
            logger.trace("Expected convergence in $convergence cycles - fanout: ${registryConfig.gossipFanout}, peers: ${mutablePeers.count()}.")
        }
    }

    private suspend fun resolvePeers() = withContext(Dispatchers.IO) {
        InetAddress.getByName(host)
            .let { address ->
                runCatching {
                    InetAddress.getAllByName(registryConfig.registryDnsPattern)
                        .map { it.hostAddress }
                        .filter { it != address.hostAddress }
                        .toMutableSet()
                }.getOrElse {
                    logger.error("Error resolving peers: ${it.message}")
                    mutableSetOf()
                }.apply {
                    logger.debug("Peers discovered: {}", this)
                }
            }
    }

    private fun calculateConvergenceRate(): Long {
        if (mutablePeers.isEmpty()) return 0
        else if(mutablePeers.size <= registryConfig.gossipFanout) return 1

        return ceil(ln(mutablePeers.size.toDouble()) / ln(registryConfig.gossipFanout.toDouble())).toLong()
    }
}
