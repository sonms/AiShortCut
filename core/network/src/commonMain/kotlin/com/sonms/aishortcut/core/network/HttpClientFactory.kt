package com.sonms.aishortcut.core.network

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.logging.Logger as KtorLogger

private val json = Json { ignoreUnknownKeys = true }

// `engine` is the seam for tests (pass a MockEngine); production passes nothing
// and Ktor picks the platform engine on the classpath (okhttp on Android,
// darwin on iOS).
fun createHttpClient(engine: HttpClientEngine? = null): HttpClient {
    val config: HttpClientConfig<*>.() -> Unit = {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            // Route Ktor's request/response log through Kermit, so network
            // traffic shows up in Logcat / NSLog next to every other app log.
            logger = object : KtorLogger {
                override fun log(message: String) {
                    Logger.withTag("HttpClient").d(message)
                }
            }
            // ponytail: ALL prints request/response bodies -- useful now, but
            // drop to HEADERS or NONE for release builds once a build flag exists.
            level = LogLevel.ALL
        }
    }
    return if (engine == null) HttpClient(config) else HttpClient(engine, config)
}
