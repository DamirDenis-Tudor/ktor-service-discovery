package io.github.damir.denis.tudor.ktor.service.registry.gossip

import kotlinx.serialization.Serializable

data class Registry(val services: Map<String, List<Service>> = emptyMap()) {

    @Serializable
    data class Service(val pattern: String, val id: String, val name: String, val address: String)

    operator fun plus(service: Service): Registry {
        val patternServices = services[service.pattern] ?: emptyList()
        val updatedPatternServices = patternServices.filterNot { it.id == service.id } + service
        return Registry(services + (service.pattern to updatedPatternServices))
    }

    operator fun minus(service: Service): Registry {
        val updatedServices = services.mapValues { (_, patternServices) ->
            patternServices.filterNot { it.id == service.id }
        }.filter { it.value.isNotEmpty() }
        return Registry(updatedServices)
    }

    operator fun get(pattern: String): List<Service> {
        println(services[pattern])
        return services[pattern] ?: emptyList()
    }
    operator fun get(pattern: String, id: String): Service? =
        services[pattern]?.firstOrNull { it.id == id }
}
