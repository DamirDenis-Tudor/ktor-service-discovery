package io.github.damir.denis.tudor.ktor.service.extensions

import io.github.damir.denis.tudor.ktor.service.GossiperKey
import io.github.damir.denis.tudor.ktor.service.gossip.GossipAction
import io.github.damir.denis.tudor.ktor.service.registry.Registry
import io.github.damir.denis.tudor.ktor.service.registry.Service
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.registry(path: String) {
    var registry = Registry()
    val gossiper = application.attributes[GossiperKey]

    route(path) {
        get("/services/{pattern}") {
            call.respond(
                HttpStatusCode.OK,
                registry[call.parameters["pattern"] ?: ""]
            )
        }

        post("/register") {
            call.receive<Service>().let {
                registry += it
                gossiper.publishAction(GossipAction.RegisterService(it))
            }
            call.respond(HttpStatusCode.OK)
        }

        post("/unregister") {
            call.receive<Service>().let {
                registry -= it
                gossiper.publishAction(GossipAction.UnregisterService(it))
            }
            call.respond(HttpStatusCode.OK)
        }
    }
}