package io.github.damir.denis.tudor.ktor.discoverer

import io.github.damir.denis.tudor.ktor.discoverer.balancer.LoadBalanceMethod
import io.github.damir.denis.tudor.ktor.discoverer.plugin.Discoverer
import io.github.damir.denis.tudor.ktor.discoverer.plugin.request
import io.github.damir.denis.tudor.ktor.discoverer.plugin.services
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.*

internal fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

@OptIn(InternalAPI::class)
internal fun Application.module() {
    install(Discoverer) {
        serviceRegistryHostname = "localhost"
        serviceRegistryPort = 7000
        servicesInvalidationInterval = 30
    }

    CoroutineScope(Dispatchers.IO).launch {
        repeat(100) {
            async {
                request(
                    endpoint = "/test",
                    serviceName = "service",
                    loadBalanceMethod = LoadBalanceMethod.RoundRobin
                ) {
                    method = HttpMethod.Post
                    body = "pong"
                }.onSuccess { response ->
                    environment.log.info("Request successful: ${response.status}")
                }.onFailure { exception ->
                    environment.log.error("Request failed: ${exception.message}", exception)
                }

                services("service").forEach { service ->
                    service.pattern.let { log.info(it) }
                    service.identity.let { log.info(it) }
                    service.rootAddress.let { log.info(it) }
                    service.metadata.let { log.info(it.toString()) }
                }
            }

            delay(1000)
        }
    }
}