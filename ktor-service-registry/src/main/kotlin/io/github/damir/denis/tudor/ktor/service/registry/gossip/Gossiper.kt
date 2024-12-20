package io.github.damir.denis.tudor.ktor.service.registry.gossip

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(InternalAPI::class)
object Gossiper {
    private val httpClient = HttpClient(CIO)

    private var registry = Registry()
    private val receiveChannel = Channel<Action>()
    private val seenActions = mutableSetOf<Action>()

    private val port = 5000

    private var peers = mutableListOf("5000", "5001", "5002", "5003", "5004")

    init {

        peers = peers.filter { it != port.toString() }.toMutableList()

        CoroutineScope(Dispatchers.Default).launch {
            receiveChannel.consumeEach { action: Action ->
                println(action)
                when (action) {
                    is Action.RegisterService -> registry += action.service

                    is Action.UnregisterService -> registry -= action.service

                }
                broadcastMessage(action)
                println(registry)
            }
        }
    }

    fun start() {
        embeddedServer(Netty, port = port) {
            install(ContentNegotiation) {
                json(Json { prettyPrint = true; isLenient = true })
            }

            routing {
                route("/gossip") {
                    post("/register") {
                        receiveChannel.send(call.receive<Action.RegisterService>())
                        call.respond(HttpStatusCode.OK)
                    }

                    post("/unregister") {
                        receiveChannel.send(call.receive<Action.UnregisterService>())
                        call.respond(HttpStatusCode.OK)
                    }

                    get("/services/{pattern}") {
                        call.respond(
                            HttpStatusCode.OK,
                            registry[call.parameters["pattern"] ?: return@get call.respond(HttpStatusCode.BadRequest)]
                        )
                    }
                }
            }
        }.start(wait = true)
    }


    private suspend fun broadcastMessage(action: Action) {
        if (action in seenActions) {
            println("finalllllllllllllllllllll -> $action")
            return
        }

        seenActions.add(action)

        peers.shuffled().take(2).forEach { peer ->
            println("Seen message $action to $peer.")
            runCatching {
                when (action) {
                    is Action.RegisterService -> {
                        httpClient.request("http://localhost:$peer/gossip/register") {
                            method = HttpMethod.Post
                            contentType(ContentType.Application.Json)
                            body = Json.encodeToString(action)
                        }
                    }

                    is Action.UnregisterService -> {
                        httpClient.request("http://localhost:$peer/gossip/unregister") {
                            method = HttpMethod.Post
                            contentType(ContentType.Application.Json)
                            body = Json.encodeToString(action)
                        }
                    }
                }.let { println(it.status) }
            }.getOrElse {
                println("error: $it")
            }
        }
    }
}
