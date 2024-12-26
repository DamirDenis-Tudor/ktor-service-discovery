package io.github.damir.denis.tudor.ktor.registry.discoverer

import io.ktor.server.config.*

class Config(config: ApplicationConfig) {
    var serviceRegistryHostname = config.tryGetString("serviceRegistryHostname") ?: error("serviceRegistryHostname missing")
    var serviceRegistryPort = config.tryGetString("serviceRegistryPort")?.toIntOrNull() ?: error("serviceRegistryPort missing")
    var servicesInvalidationInterval = config.tryGetString("servicesInvalidationInterval")?.toLongOrNull() ?: error("servicesInvalidationInterval missing")
}