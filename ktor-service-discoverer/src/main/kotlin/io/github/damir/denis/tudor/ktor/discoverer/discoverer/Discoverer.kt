package io.github.damir.denis.tudor.ktor.discoverer.discoverer

import io.github.damir.denis.tudor.ktor.discoverer.plugin.DiscovererConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.logging.*
import io.ktor.utils.io.InternalAPI
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Discoverer(private val discovererConfig: DiscovererConfig) {
    private val logger = KtorSimpleLogger(this.javaClass.name)

    private val httpClient = HttpClient(CIO)

    private val cacheInvalidation: MutableMap<String, Long> = mutableMapOf()
    private val servicesCache: MutableMap<String, List<Service>> = mutableMapOf()

    private val registryAddress = "http://${discovererConfig.serviceRegistryHostname}:" +
            "${discovererConfig.serviceRegistryPort}"

    private fun Long.hasExpired(): Boolean {
        return (System.currentTimeMillis() - this) > (discovererConfig.servicesInvalidationInterval * 1_000)
    }

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getServices(pattern: String): List<Service> {
        if (cacheInvalidation[pattern]?.hasExpired() != false || servicesCache[pattern].isNullOrEmpty()) {
            logger.debug("Cache invalidation for services <$pattern>.")

            val response = runCatching {
                httpClient.request("$registryAddress/services/$pattern") {
                    method = HttpMethod.Get
                    contentType(ContentType.Application.Json)
                }.bodyAsText()
            }

            if (response.isFailure) {
                logger.warn("Service registry unavailable: ${response.exceptionOrNull()?.message}")
            }

            servicesCache[pattern] = if (response.getOrNull().isNullOrEmpty()) emptyList() else json.decodeFromString(response.getOrNull()!!)

            cacheInvalidation[pattern] = System.currentTimeMillis()
        }

        return servicesCache[pattern] ?: emptyList()
    }

    @OptIn(InternalAPI::class)
    suspend fun unregisterService(service: Service) {
        logger.warn("Unregister faulty service for <$service>.")

        servicesCache[service.pattern] = servicesCache[service.pattern]!!.filter { it.identity != service.identity }

        httpClient.request("$registryAddress/unregister/${service.pattern}/${service.identity}") {
            method = HttpMethod.Post
            contentType(ContentType.Application.Json)
            body = Json.encodeToString(service)
        }
    }
}