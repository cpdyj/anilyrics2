package routeHandler

import io.vertx.ext.web.Route

private const val ADD_HTML_SUFFIX_HANDLER = "ADD_HTML_SUFFIX_HANDLER"
fun Route.rerouteWithHtmlSuffixOrDefault(): Route =
    handler {
        val path = it.normalisedPath()
        if ((!path.endsWith(".html") || path.endsWith("/")) && it.get<Boolean>(ADD_HTML_SUFFIX_HANDLER) != true) {
            it.put(ADD_HTML_SUFFIX_HANDLER, true)
            it.reroute("$path${if (path.endsWith("/")) "index" else ""}.html")
        } else {
            it.next()
        }
    }