package io.github.damir.denis.tudor.ktor.service

import io.github.damir.denis.tudor.ktor.service.discoverer.Config
import io.github.damir.denis.tudor.ktor.service.discoverer.Discoverer
import io.ktor.server.application.*
import io.ktor.util.*

val DiscovererKey = AttributeKey<Discoverer>("DiscovererKey")

val Discoverer = createApplicationPlugin(
    name = "Discoverer",
    configurationPath = "ktor.discoverer",
    createConfiguration = ::Config
) {
    with(Discoverer()) {
        application.attributes.put(DiscovererKey, this)
    }
}