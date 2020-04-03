package services

import dbaction.DBContext
import dbmapper.mapTo
import io.vertx.kotlin.sqlclient.*
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Tuple
import loadFromContext
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass

interface TransactionalDBContext : DBContext

@OptIn(ExperimentalContracts::class)
suspend fun withTransaction(actions: suspend TransactionalDBContext.() -> Unit) {
    contract {
        callsInPlace(actions, InvocationKind.EXACTLY_ONCE)
    }
    val pool = loadFromContext<PgPool>()
    checkNotNull(pool) { "load db client from context fail. NPE" }
    val transaction =
        runCatching { pool.beginAwait() }.getOrElse { throw RuntimeException("begin transaction fail", it) }
    try {
        object : TransactionalDBContext {
            override suspend fun <R : Any> queryAwait(returnType: KClass<R>, sql: String, vararg args: Any): List<R> {
                val result: RowSet<Row> = transaction.preparedQueryAwait(sql, Tuple.tuple(args.toList()))
                return result.map { it.mapTo(returnType) }
            }
        }.actions()
        transaction.commitAwait()
    } catch (th: Throwable) {
        transaction.rollbackAwait()
        throw th
    }
}