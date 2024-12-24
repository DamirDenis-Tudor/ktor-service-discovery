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

    private fun Service.isExpired(): Boolean = System.currentTimeMillis() - timeStarted >= timeToLive

    init {
        CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(5)
                mutex.withLock {
                    registry = Registry(
                        registry.services.mapValues { (_, patternServices) ->
                            patternServices.filterNot { it.isExpired() }
                        }
                    )
                }
            }
        }
    }

    suspend fun addService(service: Service) = mutex.withLock { registry += service }

    suspend fun removeService(service: Service) = mutex.withLock { registry += service }

    suspend fun getServices(pattern: String) = mutex.withLock { registry[pattern] }

}