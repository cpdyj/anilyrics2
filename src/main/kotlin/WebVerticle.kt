import io.vertx.kotlin.core.http.closeAwait
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.sqlclient.poolOptionsOf
import io.vertx.pgclient.PgPool
import io.vertx.redis.client.Redis
import io.vertx.redis.client.RedisAPI
import io.vertx.redis.client.RedisOptions
import routeHandler.ThymeleafHandler

class WebVerticle : CoroutineVerticle() {
    private val webOptions by lazy { config.getJsonObject(WEB_CONFIG).mapTo(WebVerticleOptions::class.java)!! }
    private val dbOptions by lazy { config.getJsonObject(DB_OPTIONS).mapTo(DBOptions::class.java)!! }
    private val redisOption by lazy { config.getJsonObject(REDIS_OPTIONS).mapTo(RedisOptions::class.java)!! }

    private val thymeleafHandler by lazy { ThymeleafHandler(vertx, webOptions.templates) }
    private val router by lazy { createRouter(vertx) }
    private val redis by lazy { RedisAPI.api(Redis.createClient(vertx, redisOption)) }
    private val dbPool by lazy {
        PgPool.pool(
            vertx,
            dbOptions.connUrl,
            poolOptionsOf(maxSize = dbOptions.poolMaxSize, maxWaitQueueSize = dbOptions.poolMaxWait)
        )
    }
    private val httpServer by lazy { vertx.createHttpServer() }

    override suspend fun start() {
        redis.storeToContext()
        dbPool.storeToContext()
        thymeleafHandler.storeToContext()
        httpServer.requestHandler(router)
        httpServer.listenAwait(webOptions.port, webOptions.host)
        println("okokok ${httpServer.actualPort()}")
    }

    override suspend fun stop() {
        httpServer.closeAwait()
        super.stop()
    }
}

