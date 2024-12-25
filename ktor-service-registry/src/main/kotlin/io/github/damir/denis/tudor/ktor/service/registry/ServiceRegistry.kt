package io.github.damir.denis.tudor.ktor.service.registry

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ServiceRegistry {
    private var registry = Registry(emptyMap())

    companion object {
        private val mutex = Mutex()
        lateinit var config: Config
    }

    private fun Service.isExpired(): Boolean = System.currentTimeMillis() - timeStarted >= (timeToLive * 1_000)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                mutex.withLock {
                    registry = Registry(
                        registry.services.mapValues { (_, patternServices) ->
                            patternServices.filterNot { it.isExpired() }
                        }
                    )
                    println(registry)
                    delay(config.registryCleanUpInterval * 1_000)
                }
            }
        }
    }

    suspend fun addService(service: Service) = mutex.withLock { registry += service }

    suspend fun removeService(service: Service) = mutex.withLock { registry += service }

    suspend fun getServices(pattern: String) = mutex.withLock { registry[pattern] }

}