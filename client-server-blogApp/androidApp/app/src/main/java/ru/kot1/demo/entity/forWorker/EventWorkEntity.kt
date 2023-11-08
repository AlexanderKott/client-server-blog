package ru.kot1.demo.entity.forWorker

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.kot1.demo.dto.Attachment
import ru.kot1.demo.dto.Coords
import ru.kot1.demo.dto.Event
import ru.kot1.demo.dto.Post

@Entity
data class EventWorkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String?,
    val authorAvatar: String?,
    val authorId: Long,
    val content: String?,
    val link: String?,
    val published: String?,
    val datetime : String?,
    val speakerIds: List<Long>?,
    @ColumnInfo(name = "event_type")
    val type: String?,
    @Embedded
    val attachment: Attachment?,
    val mediaUri: String?,
    val mediaType: String?,
    val likedByMe : Boolean,
    val participatedByMe : Boolean,
) {
    fun toDto() = Event(attachment, author, authorAvatar,authorId, content, id, link,
        published, datetime, speakerIds, type,likedByMe, participatedByMe, null)

    companion object {
        fun fromDto(dto: Event) =
            EventWorkEntity(dto.id,dto.author, dto.authorAvatar, dto.authorId, dto.content, dto.link,
                dto.published, dto.datetime,dto.speakerIds,dto.type, dto.attachment,
                 "", "",dto.likedByMe,dto.participatedByMe)
    }
}