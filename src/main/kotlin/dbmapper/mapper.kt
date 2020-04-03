package dbmapper

import com.fasterxml.jackson.databind.JavaType
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.sqlclient.Row
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaType


suspend fun <T : Any> Row.mapTo(type: KClass<T>): T {
    val mapDef = getMapper(type)
    println(mapDef)
    return mapDef.parameters.map { (kp, n, j) ->
        val colId = this.getColumnIndex(n).takeIf { it > -1 } ?: error("Unknown column: $n")
        val columnValue: Any? = this.getValue(colId)
        kp to if (j != null && columnValue != null) {
            when (columnValue) {
                is JsonObject -> columnValue.encode()
                is JsonArray -> columnValue.encode()
                is String -> columnValue
                else -> error("map json fail. unknown value type: ${columnValue::class} -> $columnValue")
            }.let { JsonMapper.mapper.readValue<Any?>(it, j) }
        } else columnValue
    }.toMap().also { println(it) }.let(mapDef.constructor::callBy).also { println(it!!::class) } as T
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class DBFieldName(val name: String)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CONSTRUCTOR)
annotation class DBRowCreator

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Json

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class CamelToPascal

private val cacheMap = WeakHashMap<KClass<*>, MapperCache>()
private val mutex = ReentrantReadWriteLock()
fun <T : Any> getMapper(type: KClass<T>): MapperCache {
    mutex.read { cacheMap[type] }?.let { return it }
    val autoCamelcaseConvert = type.findAnnotation<CamelToPascal>() != null
    val constructor = type.constructors.find { it.findAnnotation<DBRowCreator>() != null } ?: type.primaryConstructor
    ?: error("Not found constructor")
    val list = constructor.parameters.map { kParameter: KParameter ->
        val fieldName =
            kParameter.findAnnotation<DBFieldName>()?.name
                ?: kParameter.name?.let { if (autoCamelcaseConvert) it.camelToPascal() else it }
                ?: error("undetermined name: $kParameter")
        val jsonType =
            kParameter.findAnnotation<Json>()?.let { JsonMapper.mapper.constructType(kParameter.type.javaType) }
        MapperCache.Parameter(kParameter, fieldName, jsonType)
    }
    val cache = MapperCache(constructor, list)
    mutex.write { cacheMap[type] = cache }
    return cache
}

data class MapperCache(val constructor: KFunction<*>, val parameters: List<Parameter>) {
    data class Parameter(
        val kParameter: KParameter,
        val name: String,
        val jsonType: JavaType?
    )
}

private fun String.camelToPascal() = buildString {
    this@camelToPascal.forEach { ch ->
        if (ch.isUpperCase())
            append('_').append(ch.toLowerCase())
        else
            append(ch)
    }
}