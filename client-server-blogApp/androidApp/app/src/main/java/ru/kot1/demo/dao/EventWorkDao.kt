package ru.kot1.demo.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.kot1.demo.entity.forWorker.EventWorkEntity
import ru.kot1.demo.entity.forWorker.PostWorkEntity

@Dao
interface EventWorkDao {
    @Query("SELECT * FROM EventWorkEntity WHERE id = :id")
    suspend fun getById(id: Long): EventWorkEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(work: EventWorkEntity): Long

    @Query("DELETE FROM EventWorkEntity WHERE id = :id")
    suspend fun removeById(id: Long)
}