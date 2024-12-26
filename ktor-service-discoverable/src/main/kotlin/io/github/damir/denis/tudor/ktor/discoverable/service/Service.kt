package io.github.damir.denis.tudor.ktor.discoverable.service

import kotlinx.serialization.Serializable

@Serializable
data class Service(
    val pattern: String,
    val identity: String,
    val rootAddress: String,
    val timeToLive: Long,
    val timeStarted: Long = System.currentTimeMillis(),
)