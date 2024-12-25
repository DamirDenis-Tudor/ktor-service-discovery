package io.github.damir.denis.tudor.ktor.service.discoverable

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(InternalAPI::class)
class Discoverable(private val config: Config) {
    private val httpClient = HttpClient(CIO)

    private fun Service.encodeToString() = Json.encodeToString(this)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(config.heartbeatInterval / 2 * 1000)
                httpClient.request(config.serviceRegistryAddress) {
                    method = HttpMethod.Post
                    contentType(ContentType.Application.Json)
                    body = Service(
                        pattern = config.servicePattern,
                        identity = config.serviceIdentity,
                        rootAddress = config.serviceRootUrl,
                        timeToLive = config.timeToLiveInterval,
                    ).encodeToString()
                }
                delay(config.heartbeatInterval / 2 * 1000)
            }
        }
    }
}