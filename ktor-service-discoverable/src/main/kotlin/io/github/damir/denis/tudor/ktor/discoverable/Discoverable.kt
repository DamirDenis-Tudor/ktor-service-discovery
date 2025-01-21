package io.github.damir.denis.tudor.ktor.discoverable

import io.github.damir.denis.tudor.ktor.discoverable.plugin.Discoverable
import io.ktor.server.application.*
import io.ktor.server.routing.*

internal fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

internal fun Application.module() {
    install(Discoverable){
        heartbeatInterval = 10
        timeToLiveInterval = 20

        serviceRegistryHostname = "registry hostname"
        serviceRegistryPort = 7000
        serviceRegistryRetryInterval = 10

        servicePattern = "service-pattern"
        serviceIdentity = "unique-identifier"

        serviceMetadata = mapOf(
            "data1" to "value1",
        )
    }
}