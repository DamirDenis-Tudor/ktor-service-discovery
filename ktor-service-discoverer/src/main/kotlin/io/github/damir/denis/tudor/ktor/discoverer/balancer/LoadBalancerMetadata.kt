package io.github.damir.denis.tudor.ktor.discoverer.balancer

enum class LoadBalanceMethod{
    RoundRobin,
    LeastConnections,
    LowestLatency,
    WeightBased
}

data class LeastConnectionsMetadata (
    var connections: Int = 0
)

data class LowestLatencyMetadata (
    var count: Long = 0,
    var latency: Long = 0
)

data class RoundRobinMetadata(
    var currentIndex: Int = 0
)