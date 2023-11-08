package ru.kot1.demo.dao.pagination

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.kot1.demo.entity.PostKeyEntry

@Dao
interface PostPaginationKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(listOf: List<PostKeyEntry>)

    @Query ("SELECT MIN(id) FROM PostKeyEntry")
    suspend fun min(): Long?

    @Query ("SELECT MAX(id) FROM PostKeyEntry")
    suspend fun max(): Long?
}