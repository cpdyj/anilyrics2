import io.vertx.core.Vertx


inline fun <reified T> loadFromContext():T? =
    Vertx.currentContext().get(T::class.qualifiedName)

inline fun <reified T> T.storeToContext(){
    Vertx.currentContext().put(T::class.qualifiedName,this)
}