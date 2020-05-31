package mapper

import JsonMapper
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.sqlclient.Row
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaType

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class FieldName(val name: String)


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class JsonList


fun <T : Any> Row.mapRow(type: KClass<T>): T {
    return when (type) {
        Int::class -> this.getInteger(0) as T
        Unit::class -> Unit as T
        else -> getMapper(type)(this)
    }
}


private val mapperCache: MutableMap<KClass<*>, (Row) -> Any?> = ConcurrentHashMap()
private fun <T : Any> getMapper(type: KClass<T>): (Row) -> T {
    mapperCache[type]?.let { return it as (Row) -> T }
    val constructor = type.primaryConstructor ?: error("not found primary constructor")
    val parameterMap = constructor.parameters.map {
        it to object {
            val optional = it.isOptional
            val jsonType =
                if (it.findAnnotation<JsonList>() != null) JsonMapper.mapper.constructType(it.type.javaType) else null
            val dbName = it.findAnnotation<FieldName>()?.name ?: it.name?.camelToPascal()
            ?: error("couldn't determined field name")
        }
    }.toMap()
    return { row: Row ->
        parameterMap.map { (kp, mapper) ->
            val colId = row.getColumnIndex(mapper.dbName).takeIf { it > -1 }
                ?: if (mapper.optional) return@map null else error("not found column: ${mapper.dbName}")
            val value = row.getValue(colId)
            val jsonType = mapper.jsonType
            kp to when {
                value == null -> null
                value is JsonObject && jsonType != null -> JsonMapper.mapper.readValue(value.encode(), jsonType)
                value is JsonArray && jsonType != null -> JsonMapper.mapper.readValue(value.encode(), jsonType)
                else -> value
            }
        }.filterNotNull().toMap().let { constructor.callBy(it) }
    }.also { mapperCache[type] = it }
}


fun String.camelToPascal() = buildString {
    this@camelToPascal.forEach {
        if (it.isUpperCase())
            append('_').append(it.toLowerCase())
        else
            append(it)
    }
}