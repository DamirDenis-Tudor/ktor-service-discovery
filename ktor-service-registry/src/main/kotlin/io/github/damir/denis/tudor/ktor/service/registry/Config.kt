package io.github.damir.denis.tudor.ktor.service.registry

import io.ktor.server.config.*

class Config(config: ApplicationConfig) {
    var gossipFanout = config.tryGetString("gossipFanout")?.toIntOrNull() ?: 3
    var peersInitialDelay = config.tryGetString("peersInitialDelay")?.toLongOrNull() ?: 5
    var peersDiscoveryInterval = config.tryGetString("peersDiscoveryInterval")?.toLongOrNull() ?: 60
    var registryCleanUpInterval = config.tryGetString("registryCleanUpInterval")?.toLongOrNull() ?: 10

    var registryDnsPattern = config.tryGetString("registryDnsPattern")
    var registryHostname = config.tryGetString("registryHostname")
    var registryPort = config.tryGetString("registryPort")
}