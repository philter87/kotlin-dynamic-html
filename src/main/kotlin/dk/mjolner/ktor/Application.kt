package dk.mjolner.ktor

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import dk.mjolner.ktor.plugins.*
import dk.mjolner.ktor.service.InsultRepository
import dk.mjolner.ktor.service.InsultService


import org.koin.dsl.module
import org.koin.ktor.plugin.koin

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}


fun Application.module() {
    configureRouting()

    koin {
        modules(module {
            single { InsultService(get()) }
            single { InsultRepository() }
        })
    }
}


