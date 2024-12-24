package io.github.damir.denis.tudor.ktor.service

import io.github.damir.denis.tudor.ktor.service.gossip.Action
import io.github.damir.denis.tudor.ktor.service.gossip.ActionHandler
import io.github.damir.denis.tudor.ktor.service.registry.Registry
import io.github.damir.denis.tudor.ktor.service.registry.Service
import io.github.damir.denis.tudor.ktor.service.registry.ServiceRegistry
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Routing.registry() {
    val actionHandler = ActionHandler()
    val registry = ServiceRegistry()

    route("/v1/registry") {
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
                .apply { actionHandler.publishAction(Action.RegisterService(this)) }
                .apply { registry.addService(this) }
        }

        post("/unregister") {
            call.respond(HttpStatusCode.OK)
            call.receiveText()
                .let { Json.decodeFromString<Service>(it) }
                .apply { actionHandler.publishAction(Action.UnregisterService(this)) }
                .apply { registry.removeService(this) }
        }
    }
}