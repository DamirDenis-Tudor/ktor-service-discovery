package io.github.damir.denis.tudor.ktor.service

import io.github.damir.denis.tudor.ktor.service.gossip.ActionHandler
import io.github.damir.denis.tudor.ktor.service.registry.Config
import io.ktor.server.application.*
import io.ktor.util.*

val ActionHandlerKey = AttributeKey<ActionHandler>("RegistryService")

val Registry = createApplicationPlugin(
    name = "Registry",
    configurationPath = "ktor.registry",
    createConfiguration = ::Config
) {
    with(ActionHandler(config = pluginConfig)) {
        application.attributes.put(ActionHandlerKey, this)
    }
}
