package io.github.damir.denis.tudor.ktor.service.gossip

import io.github.damir.denis.tudor.ktor.service.registry.Config
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.InetAddress
import kotlin.math.ceil
import kotlin.math.ln

@OptIn(InternalAPI::class)
class Gossiper(private val config: Config) {
    private val logger = KtorSimpleLogger(this.javaClass.name)

    private val httpClient = HttpClient(CIO)

    private val receiveChannel = Channel<GossipAction>()

    private val seenGossipActions = mutableSetOf<GossipAction>()

    private val peersMutex = Mutex()
    private val peers: MutableSet<String> = mutableSetOf()
    private var maxCycles: Long = 0L

    init {
        println(config.registryDnsPattern)
        startPeerResolutionLoop()
        startActionHandlingLoop()
    }

    private fun startPeerResolutionLoop() {
        CoroutineScope(Dispatchers.Default).launch {
            logger.info("Starting peer resolution loop")
            while (isActive) {
                delay(config.peersInitialDelay * 1_000)
                peersMutex.withLock {
                    peers.addAll(resolvePeers())
                    maxCycles = ceil(ln(peers.size.toDouble()) / ln(config.gossipFanout.toDouble())).toLong()
                    logger.info("Resolved peers: $peers")
                    logger.info("Expected convergence in $maxCycles cycles.")
                }
                delay(config.peersDiscoveryInterval * 1_000)
            }
        }
    }

    private fun resolvePeers(): List<String> =
        InetAddress.getByName(config.registryHostname).let { address ->
            runCatching {
                InetAddress.getAllByName(config.registryDnsPattern)
                    .map { it.hostAddress }
                    .filter { it != address.hostAddress }
            }.getOrElse {
                logger.error("Error resolving peers: ${it.message}")
                emptyList()
            }.apply {
                logger.info("Peers discovered: $this")
            }
        }

    private fun startActionHandlingLoop() {
        CoroutineScope(Dispatchers.Default).launch {
            logger.info("Starting action handling loop")
            receiveChannel.consumeEach { gossipAction ->
                handleAction(gossipAction)
            }
        }
    }

    private suspend fun handleAction(action: GossipAction) {
        if (action in seenGossipActions) {
            logger.info("Action has already been spread: $action")
            return
        }

        peersMutex.withLock {
            peers.shuffled().take(config.gossipFanout).forEach { peer ->
                runCatching {
                    httpClient.request(getUrl(peer, action)) {
                        method = HttpMethod.Post
                        contentType(ContentType.Application.Json)
                        body = Json.encodeToString(action)
                    }
                }.onSuccess {
                    logger.info("Successfully sent action <$action> to peer <$peer>.")
                }.onFailure { e ->
                    logger.error("Error sending action <$action> to $peer: ${e.message}")
                }
            }
        }
    }

    private fun getUrl(peer: String, action: GossipAction) =
        "http://$peer:${config.registryPort}/v1/gossip/" + when (action) {
            is GossipAction.RegisterService -> "register"
            is GossipAction.UnregisterService -> "unregister"
        }

    suspend fun publishAction(gossipAction: GossipAction) = receiveChannel.send(gossipAction)

}