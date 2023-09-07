package ru.kot1.demo.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.kot1.demo.dto.Attachment
import ru.kot1.demo.dto.Event

@Entity
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @Embedded
    val attachment: Attachment?,
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
    val likedByMe : Boolean,
    val participatedByMe : Boolean,
    val downloadingProgress : Byte?

){
    fun toDto() = Event(
        attachment,
        author,
        authorAvatar,
        authorId,
        content,
        id,
        link,
        published,
        datetime,
        speakerIds,
        type,
        likedByMe,
        participatedByMe,
        downloadingProgress
    )

    companion object {
        fun fromDto(dto: Event) =
            EventEntity(dto.id,
                dto.attachment,
                dto.author ,
                dto.authorAvatar ,
                dto.authorId ,
                dto.content,
                dto.link,
                dto.published,
                dto.datetime,
                dto.speakerIds,
                dto.type,
                dto.likedByMe,
                dto.participatedByMe,
                dto.downloadingProgress
            )
    }
}

fun List<EventEntity>.toDto(): List<Event> = map(EventEntity::toDto)
fun List<Event>.toEntity(): List<EventEntity> = map(EventEntity::fromDto)
