package routeHandler

import allSegment
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine
import loadFromContext
import java.util.*

class ThymeleafHandler(
    val vertx: Vertx, basePath: String = "templates", val converter: (String) -> String = {
        // add `index` if endsWith `/` and add `.html` suffix
        (if (it.endsWith("/")) it + "index" else it).let { if (!it.endsWith(".html")) "$it.html" else it }
    }
) : Handler<RoutingContext> {
    private val basePath: String = basePath.removeSuffix("/")
    private val engine = ThymeleafTemplateEngine.create(vertx)
    override fun handle(context: RoutingContext) {
        val path = context.getTemplate().let(converter).let { "$basePath/$it" }
        if (!context.data().containsKey("lang")) {
            // put available `lang` attribute if not exists
            context.acceptableLanguages().find { runCatching { Locale.forLanguageTag(it.value()) }.isSuccess }?.let {
                context.data().put("lang", it.value())
            }
        }
        engine.render(context.data(), path) {
            if (it.failed()) {
                context.fail(it.cause())
            } else {
                context.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/html; charset=utf-8")
                context.end(it.result())
            }
        }
    }
}

fun RoutingContext.setTemplate(path: String) {
    put("template", path)
}

fun RoutingContext.getTemplate(): String = get("template")
private val regex = Regex("#\\{([^]]+)\\}")
fun Route.withTemplate(template: String, thymeleafHandler: ThymeleafHandler = loadFromContext<ThymeleafHandler>()!!) =
    this.apply {
        check(template.startsWith("/")) { "must start with `/`" }
        val seq = regex.allSegment(template).map {
            it.fold(
                { { rt: RoutingContext -> it.groupValues[1].orEmpty().let { rt.get<String>(it) } ?: "" } },
                { { _: RoutingContext -> it } }
            )
        }
        handler { it ->
            it.setTemplate(seq.fold(StringBuilder()) { acc, f -> acc.append(f(it)) }.toString())
            it.next()
        }
        handler(thymeleafHandler)
    }