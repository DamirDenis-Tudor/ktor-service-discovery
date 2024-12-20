package io.github.damir.denis.tudor.ktor.service.registry.gossip

import kotlinx.serialization.Serializable

sealed class Action {
    @Serializable
    data class RegisterService(val service: Registry.Service) : Action()

    @Serializable
    data class UnregisterService(val service: Registry.Service) : Action()
}