package ru.kot1.demo.dao

import androidx.paging.PagingSource
import androidx.room.*
import ru.kot1.demo.entity.EventEntity
import ru.kot1.demo.entity.JobEntity
import ru.kot1.demo.entity.PostEntity

@Dao
interface EventDao {
    @Query("SELECT * FROM EventEntity ORDER BY id DESC")
      fun getAll(): PagingSource<Int,EventEntity>

    @Query("SELECT * FROM EventEntity WHERE authorId = :id ORDER BY id DESC")
    fun getAllEventsOfUser(id: Long): PagingSource<Int, EventEntity>

    @Query("SELECT COUNT(*) == 0 FROM EventEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT * FROM EventEntity WHERE id = :id LIMIT 1")
    suspend fun getEvent(id: Long): EventEntity

    @Query("SELECT COUNT(*) FROM EventEntity")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<EventEntity>)

    @Query("DELETE FROM EventEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM EventEntity")
    suspend fun deleteAll()
}

