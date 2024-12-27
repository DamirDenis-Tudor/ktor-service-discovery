package io.github.damir.denis.tudor.ktor.registry.gossip

import io.github.damir.denis.tudor.ktor.registry.service.Service
import kotlinx.serialization.Serializable

@Serializable
sealed class Action {
    private val timestamp: Long = System.currentTimeMillis()

    fun isExpired(threshold: Long) = System.currentTimeMillis() - timestamp > threshold * 1_000

    @Serializable
    data class RegisterService(val service: Service) : Action()

    @Serializable
    data class UnregisterService(val service: Service) : Action()
}