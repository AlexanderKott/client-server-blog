package ru.kot1.demo.dto

data class Job(
    val id: Long,
    val authorId: Long,
    val name: String,
    val position: String,
    val start: Long,
    val finish: Long,
    val link: String? = null,
    val belongsToMe : Boolean = false
)