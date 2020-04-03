package dbaction

import kotlin.reflect.KClass

interface DBContext {
    suspend fun <R:Any> queryAwait(returnType: KClass<R>, sql: String, vararg args: Any):List<R>
}

suspend inline fun <reified T:Any> DBContext.queryAwait(sql: String, vararg args: Any) =
    this.queryAwait(T::class,sql, args)