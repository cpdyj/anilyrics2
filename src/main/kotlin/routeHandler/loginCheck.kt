package routeHandler

import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext

fun loginCheckHandler(context: RoutingContext){
    
    context.user()
}

fun Route.needLogin() = apply{ handler(::loginCheckHandler) }