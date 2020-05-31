package mapper

data class MusicItem(
    val musicId: Int? = null,
    val title: String,
    val album: String,
    val category: String,
    @JsonList
    val artists: List<MusicArtist>
)

data class MusicArtist(
    val id: Int? = null,
    val name: String,
    val type: Type
) {
    enum class Type {
        ARTIST, COMPOSER, LYRICIST, ARRANGER
    }
}

suspend fun DBContext.countMusic() =
    query<Int>("select count(*) from musics").first()

suspend fun DBContext.fetchMusicList(offset: Int, limit: Int) =
    query<MusicItem>(
        "select * from music_list limit $1 offset $2",
        limit, offset
    )

suspend fun DBContext.fetchMusicById(id: Int) =
    query<MusicItem>(
        "select * from music_list where music_id = $1 limit 1",
        id
    ).firstOrNull()

suspend fun DBContext.countMusicByAlbum(album: String) =
    query<Int>("select count(*) from musics where album = $1", album).first()

suspend fun DBContext.fetchMusicByAlbum(album: String, offset: Int, limit: Int) =
    query<MusicItem>(
        "select * from music_list where album = $1 limit $2 offset $3",
        album, limit, offset
    )

suspend fun DBContext.countMusicByCategory(category: String) =
    query<Int>("select count(*) from musics where category = $1", category).first()

suspend fun DBContext.fetchMusicByCategory(category: String, offset: Int, limit: Int) =
    query<MusicItem>(
        "select * from music_list where category = $1 limit $2 offset $3",
        category, limit, offset
    )

suspend fun DBContext.countMusicByArtist(artistId: Int) =
    query<Int>("select count(*) from music_artist where artist_id = $1", artistId).first()

suspend fun DBContext.fetchMusicByArtist(artistId: Int, offset: Int, limit: Int) =
    query<MusicItem>(
        "select * from music_list where music_id in (select distinct music_id from music_artist where artist_id = $1 limit $2 offset $3)",
        artistId, limit, offset
    )

suspend fun DBContext.fetchOrCreateArtist(name: String): Int {
    query<Int>("select artist_id from artists where name = $1 limit 1", name).firstOrNull()?.let { return it }
    query<Int>("insert into artists(name) values ($1) returning artist_id", name).first().let { return it }
}

suspend fun DBContext.submitMusic(musicItem: MusicItem) {
    // create in musics
    val musicId = musicItem.musicId?.let {
        query<Int>(
            "update musics set title = $2, album = $3, category = $4 where music_id = $1 returning music_id",
            it, musicItem.title, musicItem.album, musicItem.category
        ).firstOrNull()?.also {
            query<Int>("delete from music_artist where music_id = $1 returning artist_id", it)
        }
    } ?: kotlin.run {
        query<Int>(
            "insert into musics(title, album, category) values ($1,$2,$3) returning music_id",
            musicItem.title, musicItem.album, musicItem.category
        ).first()
    }
    // create artists
    val arts = musicItem.artists.distinctBy { it.name }.map { it.name to fetchOrCreateArtist(it.name) }.toMap()
    // create contract
    musicItem.artists.map { listOf(musicId, arts[it.name], it.type.name) }.let {
        batchQuery<Unit>("insert into music_artist(music_id, artist_id, type) VALUES ($1,$2,$3)", it as List<List<Any>>)
    }
}