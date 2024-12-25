package io.github.damir.denis.tudor.ktor.service.discoverer

import io.ktor.server.config.*

class Config(config: ApplicationConfig) {
    var serviceRegistryAddress = config.property("ktor.serviceRegistryAddress").getString()
    var servicesFetchInterval = config.property("ktor.serviceRegistryFetchInterval").getString().toLongOrNull() ?: 5

}