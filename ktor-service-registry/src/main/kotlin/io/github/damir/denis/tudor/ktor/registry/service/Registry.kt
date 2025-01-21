package io.github.damir.denis.tudor.ktor.registry.service

internal data class Registry(private val immutableServices: Map<String, List<Service>> = emptyMap()) {

    val services: Map<String, List<Service>>
        get() = immutableServices

    operator fun plus(service: Service): Registry {
        val patternServices = immutableServices[service.pattern] ?: emptyList()
        val updatedPatternServices = patternServices.filterNot { it.identity == service.identity } + service

        return Registry(immutableServices + (service.pattern to updatedPatternServices))
    }

    fun remove(pattern: String, id: String): Registry {
        val updatedServices = immutableServices.mapValues { (_, patternServices) ->
            patternServices.filterNot { it.identity == id && it.pattern == pattern }
        }.filter { it.value.isNotEmpty() }

        return Registry(updatedServices)
    }

    operator fun get(pattern: String): List<Service> {
        return immutableServices[pattern]?.sortedBy { it.identity} ?: emptyList()
    }
}