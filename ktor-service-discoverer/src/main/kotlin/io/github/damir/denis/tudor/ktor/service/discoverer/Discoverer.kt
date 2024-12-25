package io.github.damir.denis.tudor.ktor.service.discoverer

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex

class Discoverer(config: Config) {
    private val httpClient = HttpClient(CIO)
    private val mutableServices: MutableMap<String ,MutableList<Service>> = mutableMapOf()
    private val servicesMutex = Mutex()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {


                delay(config.servicesFetchInterval * 1_000)
            }
        }
    }

    fun getServices(pattern: String): List<Service> {

        return emptyList()
    }
}