package ru.kot1.demo.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.kot1.demo.dto.User

@Entity
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val login: String,
    val name: String,
    val avatar: String,
    val authorities: List<String>
) {


    fun toDto() = User(id, name, login, avatar, authorities)

  companion object {
      fun fromDto(dto: User) =
          UserEntity(dto.id, dto.name , dto.login, dto.avatar ?: "" ,dto.authorities )
   }
}

fun List<UserEntity>.toDto(): List<User> = map(UserEntity::toDto)
fun List<User>.toEntity(): List<UserEntity> = map(UserEntity::fromDto)
