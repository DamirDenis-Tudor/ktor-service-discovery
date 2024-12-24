package io.github.damir.denis.tudor.ktor.service.discoverable

import kotlinx.serialization.Serializable

@Serializable
internal data class Service(
    val pattern: String,
    val id: String,
    val name: String,
    val address: String
)