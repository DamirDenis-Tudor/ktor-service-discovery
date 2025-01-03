package io.github.damir.denis.tudor.ktor.discoverer

import io.github.damir.denis.tudor.ktor.discoverer.plugin.Discoverer
import io.github.damir.denis.tudor.ktor.discoverer.plugin.request
import io.ktor.http.*
import io.ktor.server.application.*
import kotlinx.coroutines.*

internal fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

internal fun Application.module() {
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