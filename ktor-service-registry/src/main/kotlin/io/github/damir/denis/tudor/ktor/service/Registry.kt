package io.github.damir.denis.tudor.ktor.service

import io.github.damir.denis.tudor.ktor.service.gossip.Gossiper
import io.github.damir.denis.tudor.ktor.service.registry.Config
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.util.*

val GossiperKey = AttributeKey<Gossiper>("RegistryService")

val Registry = createApplicationPlugin(
    name = "Registry",
    configurationPath = "registry",
    createConfiguration = ::Config
) {
    with(
        Gossiper(config = pluginConfig)
    ) {
        application.attributes.put(GossiperKey, this)
    }
}
