package io.github.damir.denis.tudor.ktor.discoverable.service

import io.github.damir.denis.tudor.ktor.discoverable.plugin.DiscovererConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import kotlinx.io.IOException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.concurrent.fixedRateTimer

@OptIn(InternalAPI::class)
class Discoverable(
    private val port: Int,
    private val hostname: String,
    private val discovererConfig: DiscovererConfig
) {
    private val logger = KtorSimpleLogger(this.javaClass.name)

    private val httpClient = HttpClient(CIO)
    private val registryAddress = "http://${discovererConfig.serviceRegistryHostname}:" +
                "${discovererConfig.serviceRegistryPort}"

    init {
        fixedRateTimer(
            name = "heartbeat",
            initialDelay = 1_000,
            period = discovererConfig.heartbeatInterval * 1_000
        ) {
            val service = Service(
                pattern = discovererConfig.servicePattern,
                identity = discovererConfig.serviceIdentity,
                rootAddress = "http://$hostname:$port",
                timeToLive = discovererConfig.timeToLiveInterval,
                metadata = discovererConfig.serviceMetadata
            )

            logger.debug("Perform heartbeat discovery: service={}", service)

            CoroutineScope(Dispatchers.IO).launch {
                flow {
                    emit(
                        httpClient.request("$registryAddress/register") {
                            method = HttpMethod.Post
                            contentType(ContentType.Application.Json)
                            body = Json.encodeToString(service)
                        }
                    )
                }.retry { e ->
                    (e is IOException)
                        .also { logger.warn("Exception during heartbeat discovery: {}", e.message) }
                        .also { logger.warn(e.stackTraceToString()) }
                        .also { if (it) delay(discovererConfig.serviceRegistryRetryInterval * 1_000) }
                }.collect()
            }
        }
    }
}