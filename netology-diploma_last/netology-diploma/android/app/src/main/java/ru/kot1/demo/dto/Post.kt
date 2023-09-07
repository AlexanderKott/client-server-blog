package ru.kot1.demo.dto

data class Post(
    val attachment: Attachment?,
    val author: String?,
    val authorAvatar: String?,
    val authorId: Long,
    val content: String?,
    val coords: Coords?,
    val id: Long,
    val likeOwnerIds: List<String>?,
    val likedByMe: Boolean,
    val link: String?,
    val mentionIds: List<String>?,
    val mentionedMe: Boolean,
    val published: String?,
    val downloadingProgress : Byte?,

    val ownedByMe: Boolean = false,
    var isLoading : Boolean = false,
    val logined : Boolean = false,

)


val  empty = Post(
    attachment= null,
    author= "",
    authorAvatar= "",
    authorId= 0,
    content= "",
    coords= null,
    id= 0,
    likeOwnerIds= null,
    likedByMe= false,
    link= "",
    mentionIds= null,
    mentionedMe= false,
    published= null,
    downloadingProgress = 0
)