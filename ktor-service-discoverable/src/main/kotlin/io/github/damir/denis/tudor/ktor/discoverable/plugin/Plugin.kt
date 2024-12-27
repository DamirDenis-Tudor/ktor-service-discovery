package io.github.damir.denis.tudor.ktor.discoverable.plugin

import io.github.damir.denis.tudor.ktor.discoverable.service.Discoverable
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*

val DiscoverableKey = AttributeKey<Discoverable>("DiscoverableKey")

val Discoverable = createApplicationPlugin(
    name = "Discoverable",
    configurationPath = "ktor.discoverable",
    createConfiguration = ::Config
) {
    pluginConfig.validate()

    application.attributes.put(
        DiscoverableKey,
        Discoverable(
            port = applicationConfig.port,
            hostname = applicationConfig.host,
            config = pluginConfig
        )
    )

    application.routing {
        get("ping") {
            call.respond(HttpStatusCode.OK)
        }
    }
}