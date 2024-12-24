package io.github.damir.denis.tudor.ktor.service.gossip

import io.github.damir.denis.tudor.ktor.service.registry.Service

import kotlinx.serialization.Serializable

@Serializable
sealed class Action {
    @Serializable
    data class RegisterService(val service: Service) : Action()

    @Serializable
    data class UnregisterService(val service: Service) : Action()
}