import io.github.damir.denis.tudor.ktor.service.Registry
import io.github.damir.denis.tudor.ktor.service.extensions.registry
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.testApplication
import kotlin.test.Test

fun Application.test() {
    install(Registry) {
        registryHostname = "127.0.0.2"
        registryDnsPattern = "localhost"
        gossipFanout = 3
    }

    routing {
        registry("/v1/registry")
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
