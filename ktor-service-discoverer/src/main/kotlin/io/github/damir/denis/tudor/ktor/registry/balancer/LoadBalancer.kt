package io.github.damir.denis.tudor.ktor.registry.balancer

import io.github.damir.denis.tudor.ktor.registry.plugin.Config
import io.github.damir.denis.tudor.ktor.registry.discoverer.Discoverer
import io.github.damir.denis.tudor.ktor.registry.discoverer.Service
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.system.measureTimeMillis

class LoadBalancer(private val config: Config) {
    private val httpClient = HttpClient(CIO)

    private val mutex = Mutex()
    private val serviceDiscoverer = Discoverer(config)

    private val roundRobinMetadata: MutableMap<String, RoundRobinMetadata> = mutableMapOf()
    private val leastConnMetadata: MutableMap<String, MutableMap<Service, LeastConnectionsMetadata>> = mutableMapOf()
    private val lowestLatencyMetadata: MutableMap<String, MutableMap<Service, LowestLatencyMetadata>> = mutableMapOf()

    suspend fun request(
        endpoint: String,
        serviceName: String,
        loadBalanceMethod: LoadBalanceMethod = LoadBalanceMethod.RoundRobin,
        block: HttpRequestBuilder.() -> Unit = {}
    ): HttpResponse {
        val services = serviceDiscoverer.getServices(serviceName)

        if (services.isEmpty()) {
            throw IllegalArgumentException("No services available for service name: $serviceName")
        }

        return when (loadBalanceMethod) {
            LoadBalanceMethod.RoundRobin -> {
                val currentMetadata = mutex.withLock {
                    roundRobinMetadata.getOrPut(serviceName) { RoundRobinMetadata() }
                }
                currentMetadata.currentIndex = (currentMetadata.currentIndex + 1) % services.size
                println(roundRobinMetadata)
                val service = services[currentMetadata.currentIndex]
                httpClient.request(urlString = "${service.rootAddress}${endpoint}", block = block)
            }

            LoadBalanceMethod.LeastConnections -> {
                val service = mutex.withLock {
                    leastConnMetadata.getOrPut(serviceName) { mutableMapOf() }
                    services.forEach { leastConnMetadata[serviceName]!!.getOrPut(it) { LeastConnectionsMetadata() } }

                    leastConnMetadata[serviceName]!!.minBy { it.value.connections }.key
                }

                try {
                    mutex.withLock { leastConnMetadata[serviceName]!![service]!!.connections += 1 }
                    println(leastConnMetadata)
                    httpClient.request(urlString = "${service.rootAddress}${endpoint}", block = block)
                } finally {
                    mutex.withLock { leastConnMetadata[serviceName]!![service]!!.connections -= 1 }
                    println(leastConnMetadata)
                }

            }

            LoadBalanceMethod.LowestLatency -> {
                val service = mutex.withLock {
                    lowestLatencyMetadata.getOrPut(serviceName) { mutableMapOf() }

                    services.forEach { lowestLatencyMetadata[serviceName]!!.getOrPut(it) { LowestLatencyMetadata() } }
                    lowestLatencyMetadata[serviceName]!!.minBy { it.value.latency }.key
                }
                lowestLatencyMetadata[serviceName]!![service]!!.latency = measureTimeMillis { httpClient.get("${service.rootAddress}/ping") }

                println(lowestLatencyMetadata)

                httpClient.request(urlString = "${service.rootAddress}${endpoint}", block = block)
            }

            LoadBalanceMethod.WeightBased -> TODO()
        }
    }
}