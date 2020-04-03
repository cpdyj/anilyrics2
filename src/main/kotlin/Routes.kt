import io.vertx.core.Vertx
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.common.template.TemplateEngine
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.SessionHandler
import routeHandler.error500Handler
import routeHandler.indexHandler
import routeHandler.rerouteWithHtmlSuffixOrDefault
import routeHandler.templateHandler


fun createRouter(vertx: Vertx, templateEngine: TemplateEngine, templatePath: String) =
    Router.router(vertx).apply {
        route("/*").handler(BodyHandler.create(false))
        route("/").rerouteTo("/index")
        route("/index").handler(::indexHandler).rerouteTo("/render/index")

        route("/close").handler { vertx.close() }

        route("/render/*").rerouteWithHtmlSuffixOrDefault().handler { it.put("context",it); it.next() }.handler(templateHandler(templateEngine, templatePath))
        errorHandler(500,::error500Handler)
    }

data class Song(
    val id: Int = 0,
    val title: String = "",
    val album: String = "",
    val albumId:Int=0,
    val artist: String = "",
    val artistId:Int=0,
    val tags: String = "",
    val tagId:Int=0,
    val uploader: String = "",
    val uploaderId:Int=0
)


fun Route.rerouteTo(path: String) = handler { it.reroute(path) }

