<div align="center">

# ![ktor-service-discovery](https://github.com/user-attachments/assets/960ba62b-b435-4df1-8602-c2f7257109a9) Ktor Service Discovery

</div>

![Deployment Status](https://img.shields.io/badge/deployment-success-green?style=flat)

## Overview
- This repository contains three plugins that, when used together, provide a service discovery solution for `Ktor-based microservices`:
  - `Registry`: Once installed, it provides a decentralized registry solution based on the gossip protocol.
  - `Discoverable`: Should be installed on any service that needs to be visible to others.
  - `Discoverer`: Should be installed on services that need to interact with other services.

## Usage

### Registry

```kotlin
/* Plugin installation. */
install(Registry){
    gossipFanout = 3
    gossipActionTimeout = 60

    peersInitialDelay = 5
    peersDiscoveryInterval = 600

    registryCleanUpInterval = 50
    registryDnsPattern = "<dns-hostname>"
}
```
### Discoverable

```kotlin
/* Plugin installation. */
install(Discoverable){
    heartbeatInterval = 10
    timeToLiveInterval = 20

    serviceRegistryHostname = "registry hostname"
    serviceRegistryPort = 7000
    serviceRegistryRetryInterval = 10

    servicePattern = "service-pattern"
    serviceIdentity = "unique-identifier"

    serviceMetadata = mapOf(
        "data1" to "value1",
    )
}
```

### Discoverer

- Install

```kotlin
/* Plugin installation. */
install(Discoverer) {
    serviceRegistryHostname = "registry-hostname"
    serviceRegistryPort = 7000
    servicesInvalidationInterval = 30
}
```

- Requests

```kotlin
/* Making requests with a load balancing method. */
request(
    endpoint = "/test",
    serviceName = "service",
    loadBalanceMethod = LoadBalanceMethod.RoundRobin
) {
    method = HttpMethod.Post
    body = "pong"
}.onSuccess { response ->
    environment.log.info("Request successful: ${response.status}")
}.onFailure { exception ->
    environment.log.error("Request failed: ${exception.message}", exception)
}
```

```kotlin
/* Accessing service replicas info. */
services("service").forEach { service ->
    service.pattern.let { log.info(it) }
    service.identity.let { log.info(it) }
    service.rootAddress.let { log.info(it) }
    service.metadata.let { log.info(it.toString()) }
}
```