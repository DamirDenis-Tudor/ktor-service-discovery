package io.github.damir.denis.tudor.ktor.registry.gossip

import io.github.damir.denis.tudor.ktor.registry.plugin.Config
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
import kotlin.properties.Delegates

class ActionHandler {
    private val logger = KtorSimpleLogger(this.javaClass.name)

    private val receiver = Channel<Action>()

    private val peerRegistry = PeerRegistry()
    private val httpClient = HttpClient(CIO)

    private val actionCycleCounts = mutableMapOf<Action, Int>()

    private val mutex = Mutex()

    companion object {
        var port: Int by Delegates.notNull()
        lateinit var config: Config
    }

    init {
        CoroutineScope(Dispatchers.Default).launch {
            receiver.consumeEach { action ->
                handleAction(action)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                mutex.withLock {
                    actionCycleCounts.filter { (action, _) ->
                        action.isExpired(config.gossipActionTimeout)
                    }.keys.forEach { action ->
                        actionCycleCounts.remove(action)
                        logger.debug("Removed expired action <{}>.", action)
                    }
                }
            }
        }
    }

    suspend fun publishAction(action: Action) = receiver.send(action)

    private suspend fun handleAction(action: Action) {
        val currentCycle = mutex.withLock { actionCycleCounts.getOrDefault(action, 0) }

        if (currentCycle >= peerRegistry.convergenceCycles) {
            logger.debug(
                "Action <{}> has exceeded the convergence cycles of {}.",
                action,
                peerRegistry.convergenceCycles
            )
            return
        }

        when (action) {
            is Action.RegisterService -> handleRegisterService(action)
            is Action.UnregisterService -> handleUnregisterService(action)
        }

        mutex.withLock { actionCycleCounts[action] = currentCycle + 1 }

        logger.debug("Action <{}> processed, cycle count is now {}.", action, currentCycle + 1)
    }

    @OptIn(InternalAPI::class)
    private suspend fun handleRegisterService(action: Action.RegisterService) {
        peerRegistry.peers.shuffled().take(config.gossipFanout).forEach { peer ->
            runCatching {
                httpClient.request("http://$peer:$port/register") {
                    method = HttpMethod.Post
                    contentType(ContentType.Application.Json)
                    body = Json.encodeToString(action.service)
                }
            }.onSuccess {
                logger.debug("Successfully sent RegisterService action to peer <{}>.", peer)
            }.onFailure { e ->
                logger.error("Error sending RegisterService action to peer <{}>: ${e.message}", peer)
            }
        }
    }

    @OptIn(InternalAPI::class)
    private suspend fun handleUnregisterService(action: Action.UnregisterService) {
        peerRegistry.peers.shuffled().take(config.gossipFanout).forEach { peer ->
            runCatching {
                httpClient.request("http://$peer:$port/unregister") {
                    method = HttpMethod.Post
                    contentType(ContentType.Application.Json)
                    body = Json.encodeToString(action.service)
                }
            }.onSuccess {
                logger.debug("Successfully sent UnregisterService action to peer <{}>.", peer)
            }.onFailure { e ->
                logger.error("Error sending UnregisterService action to peer <{}>: ${e.message}", peer)
            }
        }
    }
}
