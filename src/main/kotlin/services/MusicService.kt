package services

import dbaction.Label
import dbaction.Music
import dbaction.musicList
import kotlin.properties.Delegates

suspend fun fetchMusics(limit: Int, offset: Int) {
    val musics: List<Music>
    withTransaction {
        musics = musicList(limit, offset)

    }
}

class Music(base: Music? = null) {
    private val labelMap by lazy {
        mutableMapOf<Label.Type, MutableSet<Label>>().apply {
            base?.labels?.forEach { getOrPut(it.type) { mutableSetOf() }.add(it) }
        }
    }
}

class Label(val id:Int?=null,val text:String)
