package routeHandler

import io.vertx.ext.web.RoutingContext
import java.io.PrintWriter
import java.io.StringWriter

fun error500Handler(routingContext: RoutingContext) {
    val sw = StringWriter()
    PrintWriter(sw).use(routingContext.failure()::printStackTrace)
    routingContext.end(sw.toString())
}