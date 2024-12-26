package io.github.damir.denis.tudor.ktor.registry.discoverer

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.logging.*
import kotlinx.serialization.json.Json

class Discoverer(private val config: Config) {
    private val logger = KtorSimpleLogger(this.javaClass.name)

    private val httpClient = HttpClient(CIO)

    private val cacheInvalidation: MutableMap<String, Long> = mutableMapOf()
    private val servicesCache: MutableMap<String, List<Service>> = mutableMapOf()

    private fun Long.hasExpired(): Boolean {
        return (System.currentTimeMillis() - this) > (config.servicesInvalidationInterval * 1_000)
    }

    private val json = Json { ignoreUnknownKeys = true }

    private suspend fun fetchServices(pattern: String): List<Service> {
        val response =
            httpClient.request("http://${config.serviceRegistryHostname}:${config.serviceRegistryPort}/services/$pattern") {
                method = HttpMethod.Get
                contentType(ContentType.Application.Json)
            }.bodyAsText()

        if (response.isEmpty())
            return emptyList()

        return json.decodeFromString(response)
    }

    private suspend fun updateServices(pattern: String) {
        servicesCache[pattern] = fetchServices(pattern)
        cacheInvalidation[pattern] = System.currentTimeMillis()
    }

    suspend fun getServices(pattern: String): List<Service> {
        if (cacheInvalidation[pattern]?.hasExpired() != false) {
            logger.debug("Services cache invalidation for $pattern.")
            updateServices(pattern)
        }

        return servicesCache[pattern] ?: emptyList()
    }
}