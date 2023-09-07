package ru.kot1.demo.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.kot1.demo.entity.forWorker.JobWorkEntity

@Dao
interface JobWorkDao {
    @Query("SELECT * FROM JobWorkEntity WHERE id = :id")
    suspend fun getById(id: Long): JobWorkEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(work: JobWorkEntity): Long

    @Query("DELETE FROM JobWorkEntity WHERE id = :id")
    suspend fun removeById(id: Long)
}