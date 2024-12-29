package io.github.damir.denis.tudor.ktor.registry.discoverer

import kotlinx.serialization.Serializable

@Serializable
data class Service(
    val pattern: String,
    val identity: String,
    val rootAddress: String,
    val metadata: Map<String, String> = emptyMap(),
)