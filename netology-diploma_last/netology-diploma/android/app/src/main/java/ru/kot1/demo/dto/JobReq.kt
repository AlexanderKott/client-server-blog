package ru.kot1.demo.dto

data class JobReq(
    val id: Long = 0,
    val name: String,
    val finish: Long,
    val position: String,
    val start: Long
)

