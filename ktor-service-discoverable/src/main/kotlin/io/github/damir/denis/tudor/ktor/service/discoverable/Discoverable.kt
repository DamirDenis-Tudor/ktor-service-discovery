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

    private var serviceInfo = Service(
        pattern = config.servicePattern,
        id = config.serviceIdentity,
        name = "",
        address = config.serviceRootUrl,
    )

    init {
        CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                httpClient.request(config.serviceRegistryAddress) {
                    method = HttpMethod.Post
                    contentType(ContentType.Application.Json)
                    body = Json.encodeToString(serviceInfo)
                }

                delay(config.heartbeatInterval * 1000)
            }
        }
    }
}