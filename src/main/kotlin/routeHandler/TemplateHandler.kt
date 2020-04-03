package routeHandler

import io.vertx.ext.web.common.template.TemplateEngine
import io.vertx.ext.web.handler.TemplateHandler

fun templateHandler(templateEngine: TemplateEngine, templatePath: String) =
    TemplateHandler.create(
    templateEngine,
    templatePath,
    "text/html"
)!!