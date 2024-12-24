package io.github.damir.denis.tudor.ktor.service.registry


internal data class Registry(val services: Map<String, List<Service>> = emptyMap()) {

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
        return services[pattern] ?: emptyList()
    }
}
