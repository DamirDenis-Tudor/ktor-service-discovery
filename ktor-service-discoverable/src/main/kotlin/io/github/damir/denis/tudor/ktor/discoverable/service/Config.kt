package io.github.damir.denis.tudor.ktor.discoverable.service

import io.ktor.server.config.*

class Config(config: ApplicationConfig) {
    var heartbeatInterval = config.tryGetString("heartbeatInterval")?.toLongOrNull() ?: error("heartbeatInterval is missing")
    var timeToLiveInterval = config.tryGetString("timeToLiveInterval")?.toLongOrNull() ?: error("timeToLiveInterval is missing")

    var serviceRegistryHostname = config.tryGetString("serviceRegistryHostname") ?: error("serviceRegistryHostname missing")
    var serviceRegistryPort = config.tryGetString("serviceRegistryPort")?.toIntOrNull() ?: error("serviceRegistryPort missing")

    var servicePattern = config.tryGetString("servicePattern") ?: error("servicePattern missing")
    var serviceIdentity = config.tryGetString("serviceIdentity") ?: error("serviceIdentity missing")

    var serviceRootHostname = config.tryGetString("serviceRootHostname") ?: error("serviceRootHostname missing")
    var serviceRootPort = config.tryGetString("serviceRootPort")?.toLongOrNull() ?: error("serviceRootPort missing")
}