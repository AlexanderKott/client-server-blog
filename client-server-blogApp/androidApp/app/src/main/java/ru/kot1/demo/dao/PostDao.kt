package ru.kot1.demo.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import ru.kot1.demo.entity.PostEntity
import ru.kot1.demo.entity.UserEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity WHERE authorId = :id ORDER BY id DESC")
      fun getPosts(id : Long): PagingSource<Int,PostEntity>

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAllPosts(): PagingSource<Int,PostEntity>

    @Query("SELECT * FROM PostEntity WHERE id = :id LIMIT 1")
    suspend fun getPost(id: Long): PostEntity

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT COUNT(*) FROM PostEntity")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM PostEntity")
    suspend fun deleteAll()
}

