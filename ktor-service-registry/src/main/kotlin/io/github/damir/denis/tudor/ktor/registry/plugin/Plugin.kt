package io.github.damir.denis.tudor.ktor.registry.plugin

import io.github.damir.denis.tudor.ktor.registry.gossip.ActionHandler
import io.github.damir.denis.tudor.ktor.registry.gossip.PeerRegistry
import io.github.damir.denis.tudor.ktor.registry.service.Service
import io.github.damir.denis.tudor.ktor.registry.service.ServiceRegistry
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val ServiceRegistryKey = AttributeKey<ServiceRegistry>("ServiceRegistry")

val Registry = createApplicationPlugin(
    name = "Registry",
    configurationPath = "ktor.registry",
    createConfiguration = ::Config
) {
    pluginConfig.validate()

    ActionHandler.config = pluginConfig
    ActionHandler.port = applicationConfig.port

    PeerRegistry.config = pluginConfig
    PeerRegistry.host = applicationConfig.host

    ServiceRegistry.config = pluginConfig

    application.attributes.put(ServiceRegistryKey, ServiceRegistry())

    application.routing {
        val registry = application.attributes[ServiceRegistryKey]

        get("/services/{pattern}") {
            call.respond(
                HttpStatusCode.OK,
                Json.encodeToString(registry.getServices(call.parameters["pattern"] ?: ""))
            )
        }

        post("/register") {
            call.respond(HttpStatusCode.OK)
            call.receiveText()
                .let { Json.decodeFromString<Service>(it) }
                .apply { registry.addService(this) }
        }

        post("/unregister") {
            call.respond(HttpStatusCode.OK)
            call.receiveText()
                .let { Json.decodeFromString<Service>(it) }
                .apply { registry.removeService(this) }
        }
    }
}