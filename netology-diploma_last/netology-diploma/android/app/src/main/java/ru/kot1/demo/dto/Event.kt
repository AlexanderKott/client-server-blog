package ru.kot1.demo.dto

data class Event(
    val attachment: Attachment?,
    val author: String?,
    val authorAvatar: String?,
    val authorId: Long,
    val content: String?,
    val id: Long,
    val link: String?,
    val published: String?,
    val datetime : String?,
    val speakerIds: List<Long>?,
    val type: String?,
    val likedByMe : Boolean,
    val participatedByMe : Boolean,
    val downloadingProgress : Byte?,

    val speakerNames: List<String>? = null,
    val logined : Boolean = false,
    val belongsToMe : Boolean? = null,

)


val emptyEvent = Event (
    attachment = null,
    author = "",
    authorAvatar = "",
    authorId = 0,
    content = null,
    id = 0,
    link = null,
    published = null,
    datetime = null,
    speakerIds = null,
    type = null,
    likedByMe = false,
    participatedByMe = false,
    downloadingProgress = null
        )