import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.kotlin.treeToValue
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.core.json.jackson.DatabindCodec.prettyMapper

object JsonMapper {
    val prettyMapper = prettyMapper().apply { registerKotlinModule() }
    val rawMapper = DatabindCodec.mapper().apply { registerKotlinModule() }

    init {
        println("Jackson kotlin module registered.")
    }

    val mapper = prettyMapper

    val kindMapper = mapper.copy().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    fun getMapper(ignoreUnknow: Boolean): ObjectMapper = if (ignoreUnknow) kindMapper else mapper
}

fun Any?.toJsonTree() = JsonMapper.mapper.valueToTree<JsonNode>(this)!!

fun JsonNode.encode() = JsonMapper.mapper.writeValueAsString(this)!!

fun String.decodeJson() = JsonMapper.mapper.readTree(this)!!

fun <T> JsonNode.mapTo(type: Class<T>, ignoreUnknow: Boolean = false) =
    JsonMapper.getMapper(ignoreUnknow).treeToValue(this, type)!!

inline fun <reified T> JsonNode.mapTo(obj: T, ignoreUnknow: Boolean = false) =
    JsonMapper.getMapper(ignoreUnknow).readerForUpdating(obj).treeToValue<T>(this)!!
