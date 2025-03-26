package io.github.damir.denis.tudor.ktor.discoverable.plugin

import io.ktor.server.config.*


class Config(config: ApplicationConfig) {
    var serviceAddress = config.tryGetString("serviceAddress")?.toString() ?: ""
    var heartbeatInterval = config.tryGetString("heartbeatInterval")?.toLongOrNull() ?: 0
    var timeToLiveInterval = config.tryGetString("timeToLiveInterval")?.toLongOrNull() ?: 0

    var serviceRegistryHostname = config.tryGetString("serviceRegistryHostname") ?: ""
    var serviceRegistryPort = config.tryGetString("serviceRegistryPort")?.toIntOrNull() ?: 0
    var serviceRegistryRetryInterval = config.tryGetString("serviceRegistryRetryInterval")?.toLongOrNull() ?: 5L

    var servicePattern = config.tryGetString("servicePattern") ?: ""
    var serviceIdentity = config.tryGetString("serviceIdentity") ?: ""

    var serviceMetadata: Map<String, String> = config.config("serviceMetadata").toMap() as? Map<String, String> ?: emptyMap()

    fun validate() {
        require(heartbeatInterval > 0) { "heartbeatInterval must be greater than zero" }
        require(timeToLiveInterval > 0) { "timeToLiveInterval must be greater than zero" }

        require(serviceRegistryHostname.isNotEmpty()) { "serviceRegistryHostname must not be empty" }
        require(serviceRegistryPort > 0) { "serviceRegistryPort must be greater than zero" }

        require(servicePattern.isNotEmpty()) { "servicePattern must not be empty" }
        require(serviceIdentity.isNotEmpty()) { "serviceIdentity must not be empty" }
    }
}
