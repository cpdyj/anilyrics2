import com.fasterxml.jackson.annotation.JsonValue
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.http.HttpServerOptions
import io.vertx.kotlin.core.http.httpServerOptionsOf

const val WEB_CONFIG = "webOptions"

const val OAUTH2_LOGIN_CONFIG = "oauth2Options"

const val REDIS_OPTIONS = "redis"

const val DB_OPTIONS = "database"


data class WebVerticleOptions(
    val port: Int = 8080,
    val host: String = "127.0.0.1",
    val templates: String = "templates"
)

data class OAuthLoginVerticleOptions(
    val clients: List<OAuthProvider> = emptyList()
)

data class OAuthProvider(
    val id: String,
    val clientId: String,
    val clientSecret: String,
    val site: String,
    val callback: String = "",
    val tokenPath: String,
    val authorizationPath: String
)

data class DBOptions(
    val poolMaxSize: Int,
    val poolMaxWait: Int,
    val connUrl:String
)