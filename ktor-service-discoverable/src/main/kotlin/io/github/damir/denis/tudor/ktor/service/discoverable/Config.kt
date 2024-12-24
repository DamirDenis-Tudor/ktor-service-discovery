package io.github.damir.denis.tudor.ktor.service.discoverable

import io.ktor.server.config.*

class Config(config: ApplicationConfig) {
    var heartbeatInterval = config.tryGetString("heartbeatInterval")?.toLongOrNull() ?: 15L
    var timeToLiveInterval = config.tryGetString("timeToLiveInterval")?.toLongOrNull() ?: 30L

    var serviceRegistryAddress = config.tryGetString("serviceRegistryAddress") ?: "localhost"

    var servicePattern = config.tryGetString("servicePattern") ?: "servicePattern"
    var serviceIdentity = config.tryGetString("serviceIdentity") ?: "serviceIdentity"
    var serviceRootUrl = config.tryGetString("serviceUrl") ?: "serviceUrl"
}