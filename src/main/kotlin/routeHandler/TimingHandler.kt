package routeHandler

import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext

private const val TIMING = "timing"
fun timingHandler(context: RoutingContext) {
    val st = System.currentTimeMillis()
    if (context.get<Long?>(TIMING) == null) {
        context.put(TIMING, st)
    }
    context.addHeadersEndHandler {
        val st = context.get<Long?>(TIMING)
        if (st != null) {
            context.response().putHeader("timing", (System.currentTimeMillis() - st).toString())
        }
    }
    context.next()
}

fun Route.timing(): Route = apply { handler(::timingHandler) }