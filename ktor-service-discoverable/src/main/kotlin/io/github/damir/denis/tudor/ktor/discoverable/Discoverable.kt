package io.github.damir.denis.tudor.ktor.discoverable

import io.github.damir.denis.tudor.ktor.discoverable.plugin.Discoverable
import io.ktor.server.application.*
import io.ktor.server.routing.*

internal fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

internal fun Application.module() {
    install(Discoverable)

    routing {

    }
}