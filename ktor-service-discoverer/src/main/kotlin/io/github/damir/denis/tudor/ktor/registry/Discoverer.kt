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
            delay(1000)
            async {
                println("call $it")
                request(
                    endpoint = "/ping",
                    serviceName = "test",
                    loadBalanceMethod = LoadBalanceMethod.LowestLatency
                ) {
                    method = HttpMethod.Get
                }.let { println(it) }
            }

            delay(100)
        }
    }
}