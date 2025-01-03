package io.github.damir.denis.tudor.ktor.registry

import io.github.damir.denis.tudor.ktor.registry.balancer.LoadBalanceMethod
import io.github.damir.denis.tudor.ktor.registry.plugin.Discoverer
import io.github.damir.denis.tudor.ktor.registry.plugin.request
import io.ktor.http.*
import io.ktor.server.application.*
import kotlinx.coroutines.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(Discoverer)

    CoroutineScope(Dispatchers.IO).launch {
        repeat(100) {
            async {
                request(
                    endpoint = "/ping",
                    serviceName = "test"
                ) {
                    method = HttpMethod.Get
                }.fold(
                    onSuccess = { response ->
                        environment.log.info("Request successful: ${response.status}")
                    },
                    onFailure = { exception ->
                    }
                )
            }

            delay(1000)
        }
    }
}