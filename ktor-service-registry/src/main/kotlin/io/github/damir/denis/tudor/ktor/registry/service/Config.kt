package io.github.damir.denis.tudor.ktor.registry.service

import io.ktor.server.config.*

class Config(config: ApplicationConfig) {
    var gossipFanout = config.tryGetString("gossipFanout")?.toIntOrNull() ?: error("gossipFanout is missing")
    var gossipActionTimeout = config.tryGetString("gossipActionTimeout")?.toLongOrNull() ?: error("gossipActionTimeout is missing")

    var peersInitialDelay = config.tryGetString("peersInitialDelay")?.toLongOrNull() ?: error("peersInitialDelay is missing")
    var peersDiscoveryInterval = config.tryGetString("peersDiscoveryInterval")?.toLongOrNull() ?: error("peersDiscoveryInterval is missing")
    var registryCleanUpInterval = config.tryGetString("registryCleanUpInterval")?.toLongOrNull() ?: error("registryCleanUpInterval is missing")

    var registryDnsPattern = config.tryGetString("registryDnsPattern") ?: error("registryDnsPattern is missing")
    var registryHostname = config.tryGetString("registryHostname") ?: error("registryHostname is missing")
    var registryPort = config.tryGetString("registryPort")?.toIntOrNull() ?: error("registryPort is missing")
}