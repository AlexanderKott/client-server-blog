package ru.kot1.demo.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.kot1.demo.entity.forWorker.PostWorkEntity

@Dao
interface PostWorkDao {
    @Query("SELECT * FROM PostWorkEntity WHERE id = :id")
    suspend fun getById(id: Long): PostWorkEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(work: PostWorkEntity): Long

    @Query("DELETE FROM PostWorkEntity WHERE id = :id")
    suspend fun removeById(id: Long)
}