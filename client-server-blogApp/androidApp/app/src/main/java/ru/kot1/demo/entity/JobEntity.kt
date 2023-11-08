package ru.kot1.demo.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.kot1.demo.dto.Job

@Entity
data class JobEntity(
    @PrimaryKey
    val id : Long,
    val authorId : Long,
    val name: String,
    val position: String,
    val start: Long,
    val finish: Long,
    val link: String?

){
    fun toDto() = Job(
        id,authorId,name,position,start,finish,link
    )

    companion object {
        fun fromDto(dto: Job, authorId: Long) =
            JobEntity( dto.id,
                authorId,
                dto.name,
                dto.position,
                dto.start,
                dto.finish,
                dto.link
            )
    }
}

fun List<JobEntity>.toDto(): List<Job> = map(JobEntity::toDto)
fun List<Job>.toEntity(authorId: Long): List<JobEntity> = map { JobEntity.fromDto (it, authorId) }



