package ru.kot1.demo.entity.forWorker

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.kot1.demo.dto.JobReq

@Entity
data class JobWorkEntity(
    @PrimaryKey
    val id: Long = 0,
    val name: String,
    val finish: Long,
    val position: String,
    val start: Long

){
    fun toDto() = JobReq(
        id,name,finish,position,start
    )


    companion object {
        fun fromDto(dto: JobReq) =
            JobWorkEntity( dto.id,
                dto.name,
                dto.start,
                dto.position,
                dto.finish,
            )
    }
}

fun List<JobWorkEntity>.toDto(): List<JobReq> = map(JobWorkEntity::toDto)
fun List<JobReq>.toWorkEntity(): List<JobWorkEntity> = map { JobWorkEntity.fromDto (it) }



