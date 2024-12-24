package io.github.damir.denis.tudor.ktor.service.gossip

import io.github.damir.denis.tudor.ktor.service.registry.Config
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

class ActionHandler(private val config: Config, ) {
    private val logger = KtorSimpleLogger(this.javaClass.name)

    private val receiver = Channel<Action>()

    private val peerRegistry = PeerRegistry(config)
    private val httpClient = HttpClient(CIO)

    private val actionCycleCounts = mutableMapOf<Action, Int>()

    init {
        CoroutineScope(Dispatchers.Default).launch {
            receiver.consumeEach { action ->
                handleAction(action)
            }
        }
    }

    fun publishAction(action: Action) = receiver.trySend(action).isSuccess

    private suspend fun handleAction(action: Action) {
        val currentCycle = actionCycleCounts.getOrDefault(action, 0)

        if (currentCycle >= peerRegistry.convergenceCycles) {
            logger.debug("Action <$action> has exceeded the convergence cycles of ${peerRegistry.convergenceCycles}.")
            return
        }

        when (action) {
            is Action.RegisterService -> handleRegisterService(action)
            is Action.UnregisterService -> handleUnregisterService(action)
        }

        actionCycleCounts[action] = currentCycle + 1

        logger.debug("Action <$action> processed, cycle count is now ${currentCycle + 1}.")
    }

    @OptIn(InternalAPI::class)
    private suspend fun handleRegisterService(action: Action.RegisterService) {
        peerRegistry.peers.shuffled().take(config.gossipFanout).forEach { peer ->
            runCatching {
                httpClient.request("http://$peer:${config.registryPort}/v1/registry/register") {
                    method = HttpMethod.Post
                    contentType(ContentType.Application.Json)
                    body = Json.encodeToString(action)
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
                httpClient.request("http://$peer:${config.registryPort}/v1/registry/unregister") {
                    method = HttpMethod.Post
                    contentType(ContentType.Application.Json)
                    body = Json.encodeToString(action)
                }
            }.onSuccess {
                logger.debug("Successfully sent UnregisterService action to peer <{}>.", peer)
            }.onFailure { e ->
                logger.error("Error sending UnregisterService action to peer <{}>: ${e.message}", peer)
            }
        }
    }
}
