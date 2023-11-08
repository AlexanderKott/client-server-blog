package ru.kot1.demo.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.kot1.demo.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM UserEntity")
    fun getAll(): PagingSource<Int, UserEntity>

    @Query("SELECT * FROM UserEntity WHERE id = :id")
      fun getUser(id: Long): LiveData<List<UserEntity>>

    @Query("SELECT * FROM UserEntity")
     suspend fun getAllUsers(): List<UserEntity>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: List<UserEntity>)

   /* @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT COUNT(*) FROM PostEntity")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)
*/
    @Query("DELETE FROM UserEntity")
    suspend fun deleteAll()
}

