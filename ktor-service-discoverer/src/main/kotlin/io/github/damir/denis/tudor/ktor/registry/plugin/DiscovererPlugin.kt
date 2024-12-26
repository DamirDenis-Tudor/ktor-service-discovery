package io.github.damir.denis.tudor.ktor.registry.plugin

import io.github.damir.denis.tudor.ktor.registry.balancer.LoadBalancer
import io.github.damir.denis.tudor.ktor.registry.discoverer.Config
import io.github.damir.denis.tudor.ktor.registry.discoverer.Discoverer
import io.ktor.server.application.*
import io.ktor.util.*

val DiscovererKey = AttributeKey<Discoverer>("DiscovererKey")
val LoadBalancerKey = AttributeKey<LoadBalancer>("LoadBalancerKey")

val Discoverer = createApplicationPlugin(
    name = "Discoverer",
    configurationPath = "ktor.discoverer",
    createConfiguration = ::Config
) {
    with(Discoverer(pluginConfig)) {
        application.attributes.put(DiscovererKey, this)
    }
    with(LoadBalancer(pluginConfig)) {
        application.attributes.put(LoadBalancerKey, this)
    }
}