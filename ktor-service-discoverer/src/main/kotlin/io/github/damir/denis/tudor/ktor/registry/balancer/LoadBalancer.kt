package io.github.damir.denis.tudor.ktor.registry.balancer

import io.github.damir.denis.tudor.ktor.registry.discoverer.Discoverer
import io.github.damir.denis.tudor.ktor.registry.discoverer.Service
import io.github.damir.denis.tudor.ktor.registry.plugin.DiscovererConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.logging.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

class LoadBalancer(private val discovererConfig: DiscovererConfig) {
    private val logger = KtorSimpleLogger(this.javaClass.name)

    private val httpClient = HttpClient(CIO)
    private val mutex = Mutex()
    private val serviceDiscoverer = Discoverer(discovererConfig)

    private val roundRobinMetadata: MutableMap<String, RoundRobinMetadata> = mutableMapOf()
    private val leastConnMetadata: MutableMap<String, MutableMap<Service, LeastConnectionsMetadata>> = mutableMapOf()
    private val lowestLatencyMetadata: MutableMap<String, MutableMap<Service, LowestLatencyMetadata>> = mutableMapOf()

    suspend fun request(
        endpoint: String,
        serviceName: String,
        loadBalanceMethod: LoadBalanceMethod = LoadBalanceMethod.RoundRobin,
        block: HttpRequestBuilder.() -> Unit = {}
    ): Result<HttpResponse> = runCatching {
        return@runCatching withContext(Dispatchers.IO) {
            flow {
                run {
                    val services = serviceDiscoverer.getServices(serviceName)

                    if (services.isEmpty()) {
                        logger.warn("Service <$serviceName> is unavailable")
                        error("Service <$serviceName> is unavailable")
                    }

                    return@run when (loadBalanceMethod) {
                        LoadBalanceMethod.RoundRobin -> {
                            handleRoundRobin(serviceName, services, endpoint, block)
                        }

                        LoadBalanceMethod.LeastConnections -> {
                            handleLeastConnections(serviceName, services, endpoint, block)
                        }

                        LoadBalanceMethod.LowestLatency -> {
                            handleLowestLatency(serviceName, services, endpoint, block)
                        }

                        LoadBalanceMethod.WeightBased -> {
                            TODO()
                        }
                    }
                }.also { response ->
                    logger.debug("Calling url <{}>.", response.request.url)
                }.apply { emit(this@apply) }
            }
        }.retry { e ->
            e !is IllegalStateException
        }.first()
    }.also { response ->

    }

    private suspend fun handleRoundRobin(
        serviceName: String,
        services: List<Service>,
        endpoint: String,
        block: HttpRequestBuilder.() -> Unit
    ): HttpResponse {
        val currentMetadata = mutex.withLock {
            roundRobinMetadata.getOrPut(serviceName) { RoundRobinMetadata() }
        }
        currentMetadata.currentIndex = (currentMetadata.currentIndex + 1) % services.size

        val service = services[currentMetadata.currentIndex]
        try {
            return httpClient.request(urlString = "${service.rootAddress}${endpoint}", block = block)
        } catch (e: Exception) {
            serviceDiscoverer.unregisterService(service)
            roundRobinMetadata.remove(serviceName)
            throw e
        }
    }

    private suspend fun handleLeastConnections(
        serviceName: String,
        services: List<Service>,
        endpoint: String,
        block: HttpRequestBuilder.() -> Unit
    ): HttpResponse {
        val service = mutex.withLock {
            leastConnMetadata.getOrPut(serviceName) { mutableMapOf() }
            services.forEach { leastConnMetadata[serviceName]!!.getOrPut(it) { LeastConnectionsMetadata() } }

            leastConnMetadata[serviceName]!!.minBy { it.value.connections }.key
        }

        try {
            mutex.withLock { leastConnMetadata[serviceName]!![service]!!.connections += 1 }
            return httpClient.request(urlString = "${service.rootAddress}${endpoint}", block = block)
        } catch (e: Exception) {
            serviceDiscoverer.unregisterService(service)
            leastConnMetadata.remove(serviceName)
            throw e
        } finally {
            mutex.withLock { leastConnMetadata[serviceName]!![service]!!.connections -= 1 }
        }
    }

    private suspend fun handleLowestLatency(
        serviceName: String,
        services: List<Service>,
        endpoint: String,
        block: HttpRequestBuilder.() -> Unit
    ): HttpResponse {
        val service = mutex.withLock {
            lowestLatencyMetadata.getOrPut(serviceName) { mutableMapOf() }

            services.forEach { lowestLatencyMetadata[serviceName]!!.getOrPut(it) { LowestLatencyMetadata() } }
            lowestLatencyMetadata[serviceName]!!.minBy { it.value.latency }.key
        }

        try {
            val latency = measureTimeMillis { httpClient.get("${service.rootAddress}/ping") }
            mutex.withLock { lowestLatencyMetadata[serviceName]?.let { it[service]?.latency = latency } }

            return httpClient.request(urlString = "${service.rootAddress}${endpoint}", block = block)
        } catch (e: Exception) {
            serviceDiscoverer.unregisterService(service)
            lowestLatencyMetadata.remove(serviceName)
            throw e
        }
    }
}

