package io.github.damir.denis.tudor.ktor.registry.plugin

import io.ktor.server.config.*

class Config(config: ApplicationConfig) {
    var gossipFanout = config.tryGetString("gossipFanout")?.toIntOrNull() ?: 0
    var gossipActionTimeout = config.tryGetString("gossipActionTimeout")?.toLongOrNull() ?: 0

    var peersInitialDelay = config.tryGetString("peersInitialDelay")?.toLongOrNull() ?: 0
    var peersDiscoveryInterval = config.tryGetString("peersDiscoveryInterval")?.toLongOrNull() ?: 0

    var registryCleanUpInterval = config.tryGetString("registryCleanUpInterval")?.toLongOrNull() ?: 0
    var registryDnsPattern = config.tryGetString("registryDnsPattern") ?: ""

    fun validate() {
        require(gossipFanout != 0) { "gossipFanout must be a positive integer" }
        require(gossipActionTimeout != 0L) { "gossipActionTimeout must be a positive long" }

        require(peersInitialDelay != 0L) { "peersInitialDelay must be a positive long" }
        require(peersDiscoveryInterval != 0L) { "peersDiscoveryInterval must be a positive long" }

        require(registryCleanUpInterval != 0L) { "registryCleanUpInterval must be a positive long" }
        require(registryDnsPattern.isNotEmpty()) { "registryDnsPattern cannot be empty" }
    }
}