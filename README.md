<div align="center">

# ![logo(1)(1)](https://github.com/user-attachments/assets/fc0483f2-1e12-4f22-9caf-c3c49a8585bd)tor Service Discovery

</div>

![Deployment Status](https://img.shields.io/badge/deployment-success-green?style=flat)

## Overview
- This repository contains three plugins that, when used together, provide a service discovery solution for `Ktor-based microservices`:
  - `Registry`: Once installed, it provides a decentralized registry solution based on the gossip protocol.
  - `Discoverable`: Should be installed on any service that needs to be visible to others.
  - `Discoverer`: Should be installed on services that need to interact with other services.

## Usage

- ### Registry

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
- ### Discoverable

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

- ### Discoverer

```kotlin
/* Plugin installation. */
install(Discoverer) {
    serviceRegistryHostname = "registry-hostname"
    serviceRegistryPort = 7000
    servicesInvalidationInterval = 30
}
```

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
