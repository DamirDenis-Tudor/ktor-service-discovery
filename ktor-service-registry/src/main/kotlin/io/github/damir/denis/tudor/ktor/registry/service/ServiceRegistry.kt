package io.github.damir.denis.tudor.ktor.registry.service

import io.github.damir.denis.tudor.ktor.registry.gossip.Action
import io.github.damir.denis.tudor.ktor.registry.gossip.ActionHandler
import io.github.damir.denis.tudor.ktor.registry.plugin.RegistryConfig
import io.ktor.util.logging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ServiceRegistry {
    private val logger = KtorSimpleLogger(this.javaClass.name)

    private val actionHandler = ActionHandler()
    private var registry = Registry(emptyMap())

    companion object {
        private val mutex = Mutex()
        lateinit var registryConfig: RegistryConfig
    }

    private fun Service.isExpired(): Boolean = System.currentTimeMillis() - timeStarted >= (timeToLive * 1_000)

    init {
        CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                mutex.withLock {
                    delay(registryConfig.registryCleanUpInterval * 1_000)

                    logger.debug("Performing registry clean up.")

                    registry = Registry(
                        registry.services.mapValues { (_, patternServices) ->
                            patternServices.filterNot { it.isExpired() }
                        }
                    )
                }
            }
        }
    }

    suspend fun addService(service: Service) {
        logger.debug("Added service {}", service)

        registry += service
        actionHandler.publishAction(Action.RegisterService(service))
    }

    suspend fun removeService(pattern: String, id: String) {
        logger.debug("Removed service {} with id {}", pattern, id)

        registry = registry.remove(pattern, id)
        actionHandler.publishAction(Action.UnregisterService(pattern, id))
    }

    fun getServices(pattern: String) = registry[pattern]

}