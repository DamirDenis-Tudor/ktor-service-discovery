package io.github.damir.denis.tudor.ktor.service

import io.github.damir.denis.tudor.ktor.service.gossip.Action
import io.github.damir.denis.tudor.ktor.service.registry.Registry
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Routing.registry() {
    var registry = Registry()
    val actionHandler = application.attributes[ActionHandlerKey]

    route("/v1/registry") {
        get("/services/{pattern}") {
            call.respond(
                HttpStatusCode.OK,
                Json.encodeToString(registry[call.parameters["pattern"] ?: ""])
            )
        }

        post("/register") {
            call.respond(HttpStatusCode.OK)
            call.receiveText()
                .let { Json.decodeFromString<Action.RegisterService>(it) }
                .apply { actionHandler.publishAction(this) }
                .apply { registry += this.service }
        }

        post("/unregister") {
            call.respond(HttpStatusCode.OK)
            call.receiveText()
                .let { Json.decodeFromString<Action.UnregisterService>(it) }
                .apply { actionHandler.publishAction(this) }
                .apply { registry -= this.service }
        }
    }
}