package io.github.damir.denis.tudor.ktor.service

import io.ktor.server.application.*

fun Application.module() {
    install(Discoverable) {
        heartbeatInterval = 15L
        timeToLiveInterval = 30L

        serviceRegistryAddress = "http://localhost:8080/v1/registry/register"

        servicePattern = "worker"
        serviceIdentity = "1"
        serviceRootUrl = "http://0.0.0.0:8081/"
    }
}

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}