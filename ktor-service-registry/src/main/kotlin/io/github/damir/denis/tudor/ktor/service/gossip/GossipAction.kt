package io.github.damir.denis.tudor.ktor.service.gossip

import io.github.damir.denis.tudor.ktor.service.registry.Service

import kotlinx.serialization.Serializable

sealed class GossipAction {
    @Serializable
    data class RegisterService(val service: Service) : GossipAction()

    @Serializable
    data class UnregisterService(val service: Service) : GossipAction()
}