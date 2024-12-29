package io.github.damir.denis.tudor.ktor.registry.plugin

import io.github.damir.denis.tudor.ktor.registry.balancer.LoadBalanceMethod
import io.github.damir.denis.tudor.ktor.registry.discoverer.Service
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*

suspend fun Application.request(
    endpoint: String,
    serviceName: String,
    loadBalanceMethod: LoadBalanceMethod = LoadBalanceMethod.RoundRobin,
    block: HttpRequestBuilder.() -> Unit = {}
): HttpResponse {
    return attributes[LoadBalancerKey].request(endpoint, serviceName, loadBalanceMethod, block)
}

suspend fun Application.services(serviceName: String, ): List<Service> {
    return attributes[DiscovererKey].getServices(serviceName)
}