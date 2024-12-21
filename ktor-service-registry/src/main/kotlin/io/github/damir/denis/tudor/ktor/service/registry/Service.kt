package io.github.damir.denis.tudor.ktor.service.registry

import kotlinx.serialization.Serializable

@Serializable
data class Service(
    val pattern: String,
    val id: String,
    val name: String,
    val address: String
)