import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.config.configRetrieverOptionsOf
import io.vertx.kotlin.config.configStoreOptionsOf
import io.vertx.kotlin.config.getConfigAwait
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope


lateinit var vertx: Vertx

@OptIn(ExperimentalStdlibApi::class)
suspend fun main() {
    System.getProperties().put("vertxweb.environment", "dev")
    System.getProperties().put("io.vertx.ext.web.TemplateEngine.disableCache", "true")
    JsonMapper // initialize Jackson for Kotlin

    println("Hello world")
    println("Try load config...")
    val config = loadInitConfigAwait()
    println("Config loaded. Reinitialize Vertx")
    vertx = Vertx.vertx(VertxOptions(config.getJsonObject("vertx")))
    println("Vertx initialized.")


    val webVerticleDeferred = coroutineScope {
        async {
            vertx.deployVerticleAwait(
                WebVerticle::class.qualifiedName!!,
                deploymentOptionsOf(config = config)
            )
        }
    }
    val (webVerticleId) = awaitAll(webVerticleDeferred)
    println("all deployed. $webVerticleId")
}


@OptIn(ExperimentalStdlibApi::class)
suspend fun loadInitConfigAwait(): JsonObject {
    val configVertx = Vertx.vertx()
    try {
        val configRetriever = ConfigRetriever.create(configVertx, configRetrieverOptionsOf(
            stores = buildList {
                add(configStoreOptionsOf(
                    optional = false,
                    type = "file",
                    format = "json",
                    config = json { obj("path" to "config.json") }
                ))
                System.getenv("USER_CONFIG")?.let { path ->
                    println("found environment: USER_CONFIG = $path")
                    add(configStoreOptionsOf(
                        optional = false,
                        type = "file",
                        format = "json",
                        config = json { obj("path" to path) }
                    ))
                } ?: println("not found environment: USER_CONFIG")
            }
        ))
        return configRetriever.getConfigAwait()
    } finally {
        configVertx.close()
    }
}