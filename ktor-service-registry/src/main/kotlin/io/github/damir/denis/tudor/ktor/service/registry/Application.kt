package io.github.damir.denis.tudor.ktor.service.registry

import io.github.damir.denis.tudor.ktor.service.registry.gossip.Gossiper
import io.ktor.server.application.*

fun main(args: Array<String>) {
    Gossiper.start()
}

fun Application.module() {
}