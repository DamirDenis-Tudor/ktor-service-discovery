package io.github.damir.denis.tudor.ktor.registry.balancer

enum class LoadBalanceMethod{
    RoundRobin,
    LeastConnections,
    LowestLatency,
    WeightBased
}