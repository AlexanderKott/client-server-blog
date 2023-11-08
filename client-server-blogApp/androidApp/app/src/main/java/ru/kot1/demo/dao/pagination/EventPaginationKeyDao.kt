package ru.kot1.demo.dao.pagination

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.kot1.demo.entity.EventKeyEntry

@Dao
interface EventPaginationKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(listOf: List<EventKeyEntry>)

    @Query ("SELECT MIN(id) FROM EventKeyEntry")
    suspend fun min(): Long?

    @Query ("SELECT MAX(id) FROM EventKeyEntry")
    suspend fun max(): Long?
}