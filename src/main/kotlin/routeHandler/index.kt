package routeHandler

import Song
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import services.fetchMusics

fun indexHandler(context:RoutingContext){
    CoroutineScope(context.vertx().dispatcher()).launch {
        context.put("songs", listOf(Song(0,"Music Title")))
        fetchMusics(10,0)
    }
    context.next()
}