package io.github.damir.denis.tudor.ktor.registry.plugin

import io.ktor.server.config.*

class DiscovererConfig(config: ApplicationConfig) {
    var serviceRegistryHostname = config.tryGetString("serviceRegistryHostname") ?: ""
    var serviceRegistryPort = config.tryGetString("serviceRegistryPort")?.toIntOrNull() ?: ""
    var servicesInvalidationInterval = config.tryGetString("servicesInvalidationInterval")?.toLongOrNull() ?: 0

    fun validate(){
        require(serviceRegistryHostname.isNotEmpty()) { "serviceRegistryHostname must not be empty" }
        require(serviceRegistryPort != 0) { "serviceRegistryPort must not be empty" }
        require(servicesInvalidationInterval != 0L) { "servicesInvalidationInterval must not be empty" }
    }
}