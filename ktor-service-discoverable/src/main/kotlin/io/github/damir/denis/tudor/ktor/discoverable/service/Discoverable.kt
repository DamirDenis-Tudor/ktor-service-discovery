package io.github.damir.denis.tudor.ktor.discoverable.service

import io.github.damir.denis.tudor.ktor.discoverable.plugin.Config
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(InternalAPI::class)
class Discoverable(
    private val port: Int,
    private val hostname: String,
    private val config: Config
) {
    private val logger = KtorSimpleLogger(this.javaClass.name)

    private val httpClient = HttpClient(CIO)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                val service = Service(
                    pattern = config.servicePattern,
                    identity = config.serviceIdentity,
                    rootAddress = "http://$hostname:$port",
                    timeToLive = config.timeToLiveInterval,
                )

                logger.debug("Perform heartbeat discovery: service={}", service)

                httpClient.request("http://${config.serviceRegistryHostname}:${config.serviceRegistryPort}/register") {
                    method = HttpMethod.Post
                    contentType(ContentType.Application.Json)
                    body = Json.encodeToString(service)
                }
                delay(config.heartbeatInterval * 1_000)
            }
        }
    }
}