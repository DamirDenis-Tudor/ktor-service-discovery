package io.github.damir.denis.tudor.ktor.discoverer.plugin

import io.github.damir.denis.tudor.ktor.discoverer.balancer.LoadBalancer
import io.github.damir.denis.tudor.ktor.discoverer.discoverer.Discoverer
import io.ktor.server.application.*
import io.ktor.util.*

val DiscovererKey = AttributeKey<Discoverer>("DiscovererKey")
val LoadBalancerKey = AttributeKey<LoadBalancer>("LoadBalancerKey")

val Discoverer = createApplicationPlugin(
    name = "Discoverer",
    configurationPath = "ktor.discoverer",
    createConfiguration = ::DiscovererConfig
) {
    pluginConfig.validate()
    with(Discoverer(pluginConfig)) {
        application.attributes.put(DiscovererKey, this)
    }
    with(LoadBalancer(pluginConfig)) {
        application.attributes.put(LoadBalancerKey, this)
    }
}