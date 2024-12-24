package io.github.damir.denis.tudor.ktor.service

import io.github.damir.denis.tudor.ktor.service.discoverable.Config
import io.github.damir.denis.tudor.ktor.service.discoverable.Discoverable
import io.ktor.server.application.*
import io.ktor.util.*

val DiscoverableKey = AttributeKey<Discoverable>("DiscoverableKey")

val Discoverable = createApplicationPlugin(
    name = "Registry",
    configurationPath = "ktor.discoverable",
    createConfiguration = ::Config
) {
    with(Discoverable(config = pluginConfig)) {
        application.attributes.put(DiscoverableKey, this)
    }
}