package io.github.damir.denis.tudor.ktor.registry

import io.github.damir.denis.tudor.ktor.registry.plugin.Registry
import io.ktor.server.application.*

internal fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

internal fun Application.module() {
    install(Registry)
}