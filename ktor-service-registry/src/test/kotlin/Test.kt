import io.github.damir.denis.tudor.ktor.registry.plugin.Registry
import io.github.damir.denis.tudor.ktor.service.registry
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.Test

fun Application.test() {
    install(Registry) {
        registryHostname = "127.0.0.2"
        registryDnsPattern = "localhost"
        gossipFanout = 3
    }

    routing {
        registry()
    }
}

class PluginTest {
    @Test
    fun test() = testApplication {
        application {
            test()
        }
    }
}
