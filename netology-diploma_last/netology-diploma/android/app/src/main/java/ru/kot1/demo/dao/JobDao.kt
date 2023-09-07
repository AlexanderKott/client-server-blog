package ru.kot1.demo.dao

import androidx.paging.PagingSource
import androidx.room.*
import ru.kot1.demo.entity.JobEntity

@Dao
interface JobDao {
    @Query("SELECT * FROM JobEntity WHERE authorId = :id ORDER BY id DESC")
      fun getAllJobs(id: Long): PagingSource<Int,JobEntity>

    @Query("SELECT * FROM JobEntity WHERE id = :id LIMIT 1")
    suspend fun getJobById(id: Long): JobEntity

    @Query("SELECT COUNT(*) == 0 FROM JobEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT COUNT(*) FROM JobEntity")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: JobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<JobEntity>)

    @Query("DELETE FROM JobEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM JobEntity")
    suspend fun deleteAll()
}

