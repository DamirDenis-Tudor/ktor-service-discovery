package io.github.damir.denis.tudor.ktor.service

import io.github.damir.denis.tudor.ktor.service.gossip.ActionHandler
import io.github.damir.denis.tudor.ktor.service.gossip.PeerRegistry
import io.github.damir.denis.tudor.ktor.service.registry.Config
import io.github.damir.denis.tudor.ktor.service.registry.ServiceRegistry
import io.ktor.server.application.*
import io.ktor.util.*

val ServiceRegistryKey = AttributeKey<ServiceRegistry>("ServiceRegistry")

val Registry = createApplicationPlugin(
    name = "Registry",
    configurationPath = "ktor.registry",
    createConfiguration = ::Config
) {
    with(ServiceRegistry()) {
        ActionHandler.config = pluginConfig
        ServiceRegistry.config = pluginConfig
        PeerRegistry.config = pluginConfig

        application.attributes.put(ServiceRegistryKey, this)
    }
}
