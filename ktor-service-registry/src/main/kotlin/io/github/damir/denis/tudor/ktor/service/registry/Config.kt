package io.github.damir.denis.tudor.ktor.service.registry

import io.ktor.server.application.*
import io.ktor.server.config.*

class Config(config: ApplicationConfig) {
    var registryDnsPattern = config.tryGetString("registryDnsPattern") ?: "service-registry"
    var gossipFanout = config.tryGetString("gossipFanout")?.toIntOrNull() ?: 4
    var peersDiscoveryInterval = config.tryGetString("peersDiscoveryInterval")?.toLongOrNull() ?: 30
    var peersInitialDelay = config.tryGetString("peersDiscoveryInterval")?.toLongOrNull() ?: 5

    var registryPort = config.port
    var registryHostname = config.host
}