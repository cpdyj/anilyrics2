import io.vertx.core.Vertx
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import routeHandler.*


fun createRouter(vertx: Vertx): Router =
    Router.router(vertx).apply {
        route("/*").timing()
        route("/").rerouteTo("/index")
        route("/*").handler(BodyHandler.create(false))
        route("/index").handler(::indexHandler).handler(::listHandler).withTemplate("/list")
        route("/album/:albumName").handler(::albumHandler).handler(::listHandler).withTemplate("/list")
        route("/category/:categoryName").handler(::categoryHandler).handler(::listHandler).withTemplate("/list")
        route("/music/:musicId")
        route("/artist/:artistId").handler(::artistHandler).handler(::listHandler).withTemplate("/list")

        route("/close").handler { vertx.close() }
        route("/451").handler {
            it.response().statusCode = 451
            it.response().statusMessage = "Unavailable For Legal Reasons"
            it.response().putHeader("Link","<https://www.451unavailable.org/>; rel=\"blocked-by\"")
            it.end()
        }

        route("/error/*")
        errorHandler(500, ::error500Handler)
        errorHandler(404, ::error404Handler)

    }



fun Route.rerouteTo(path: String): Route = handler { it.reroute(path) }
fun Route.redirectTo(path: String): Route = handler { it.redirect(path) }

