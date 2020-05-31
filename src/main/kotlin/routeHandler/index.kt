package routeHandler

import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mapper.*

fun indexHandler(context: RoutingContext) {
    CoroutineScope(context.vertx().dispatcher()).launch {
        val offset = (context.queryParam("page").firstOrNull()?.toIntOrNull() ?: 0) * 20
        transaction { context.put(MUSIC_LIST, fetchMusicList(offset, 20)) }
        context.next()
    }
}

fun albumHandler(context: RoutingContext) {
    CoroutineScope(context.vertx().dispatcher()).launch {
        val offset = (context.queryParam("page").firstOrNull()?.toIntOrNull() ?: 0) * 20
        transaction { context.put(MUSIC_LIST, fetchMusicByAlbum(context.pathParam("albumList"), offset, 20)) }
        context.next()
    }
}

fun categoryHandler(context: RoutingContext) {
    CoroutineScope(context.vertx().dispatcher()).launch {
        val offset = (context.queryParam("page").firstOrNull()?.toIntOrNull() ?: 0) * 20
        transaction { context.put(MUSIC_LIST, fetchMusicByCategory(context.pathParam("categoryName"), offset, 20)) }
        context.next()
    }
}

fun artistHandler(context: RoutingContext) {
    CoroutineScope(context.vertx().dispatcher()).launch {
        val offset = (context.queryParam("page").firstOrNull()?.toIntOrNull() ?: 0) * 20
        transaction {
            context.put(
                MUSIC_LIST,
                fetchMusicByArtist(context.pathParam("artistId")?.toIntOrNull() ?: -1, offset, 20)
            )
        }
        context.next()
    }
}

private const val MUSIC_LIST = "musicList"
fun listHandler(context: RoutingContext) {
    CoroutineScope(context.vertx().dispatcher()).launch {
        context.get<List<MusicItem>>(MUSIC_LIST).map {
            mapOf(
                "id" to it.musicId,
                "title" to it.title.trim(),
                "album" to it.album.trim(),
                "category" to it.category.trim(),
                "artist" to (it.artists.find { it.type == MusicArtist.Type.ARTIST }?.let { it.id to it.name.trim() })
            )
        }.let { context.put("musics", it) }
        context.next()
    }
}