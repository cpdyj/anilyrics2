package dbaction

import dbmapper.CamelToPascal
import dbmapper.Json
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime

@Language("PostgreSQL")
suspend fun DBContext.musicList(offset: Int, limit: Int) =
    queryAwait<Music>(
        "select music_list.* from music_list limit ? offset ? ", limit, offset
    )

@Language("PostgreSQL")
suspend fun DBContext.musicList(labelId: Int, offset: Int, limit: Int) =
    queryAwait<Music>(
        "select music_list.* from music_list limit ? offset ? ", labelId, limit, offset
    )


@CamelToPascal
class Music(
    val musicId: Int,
    val title: String,
    @Json
    val labels: List<Label>,
    val createTime: LocalDateTime,
    val uploaderId: Int,
    val uploader: String
)

@CamelToPascal
class Label(
    val id: Int,
    val text: String,
    val type: Type
) {
    enum class Type {
        ALBUM, ARTIST, LYRICIST, COMPOSER, ARRANGER, CATEGORY
    }
}