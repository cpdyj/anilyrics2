package mapper

import io.vertx.kotlin.sqlclient.*
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Transaction
import io.vertx.sqlclient.Tuple
import loadFromContext
import java.util.*
import kotlin.contracts.CallsInPlace
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.typeOf

interface DBContext {
    suspend fun doQuery(sql: String, arguments: List<*>): RowSet<Row>
    suspend fun doBatchQuery(sql:String, args:List<List<Any>>): RowSet<Row>
}

suspend inline fun <reified T : Any> DBContext.query(sql: String, vararg arguments: Any): List<T> {
    return this.doQuery(sql, arguments.toList()).map { it.mapRow(T::class) }
}

suspend inline fun <reified T:Any> DBContext.batchQuery(sql:String, args:List<List<Any>>):List<T>{
    return this.doBatchQuery(sql, args).map { it.mapRow(T::class) }
}


@OptIn(ExperimentalContracts::class)
suspend fun transaction(producer: suspend DBContext.() -> Unit) {
    contract { callsInPlace(producer, InvocationKind.EXACTLY_ONCE) }
    val pool = loadFromContext<PgPool>() ?: error("load PgPool fail. NPE")
    val transaction = pool.beginAwait()
    try {
        val dbContext = object : DBContext {
            override suspend fun doQuery(sql: String, arguments: List<*>): RowSet<Row> {
                return transaction.preparedQueryAwait(sql, Tuple.wrap(arguments))
            }

            override suspend fun doBatchQuery(sql: String, args: List<List<Any>>): RowSet<Row> {
                return transaction.preparedBatchAwait(sql,args.map { Tuple.wrap(it) })
            }

        }
        producer.invoke(dbContext)
        transaction.commitAwait()
    } catch (th: Throwable) {
        th.printStackTrace()
        transaction.rollbackAwait()
        throw th
    }
}