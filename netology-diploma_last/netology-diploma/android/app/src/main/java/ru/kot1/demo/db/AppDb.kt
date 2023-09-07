package ru.kot1.demo.db

import android.content.Context
import androidx.room.*
import com.google.gson.Gson
import ru.kot1.demo.dao.*
import ru.kot1.demo.dao.PostWorkDao
import ru.kot1.demo.dao.pagination.EventPaginationKeyDao
import ru.kot1.demo.dao.pagination.PostPaginationKeyDao
import ru.kot1.demo.dao.pagination.UserPaginationKeyDao
import ru.kot1.demo.entity.*
import ru.kot1.demo.enumeration.AttachmentType
import com.google.gson.reflect.TypeToken
import ru.kot1.demo.entity.forWorker.EventWorkEntity
import ru.kot1.demo.entity.forWorker.JobWorkEntity
import ru.kot1.demo.entity.forWorker.PostWorkEntity
import java.lang.reflect.Type


@Database(entities = [
    PostEntity::class,
    UserEntity::class,
    PostWorkEntity::class,
    EventWorkEntity::class,
    JobWorkEntity::class,
    PostKeyEntry::class,
    EventEntity::class,
    UserKeyEntry::class,
    EventKeyEntry::class,
    JobEntity::class
                     ], version = 47, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun eventDao(): EventDao
    abstract fun jobDao(): JobDao

    abstract fun postWorkDao(): PostWorkDao
    abstract fun jobWorkDao(): JobWorkDao
    abstract fun eventWorkDao() : EventWorkDao

    abstract fun keyPostPaginationDao(): PostPaginationKeyDao
    abstract fun keyUserPaginationDao(): UserPaginationKeyDao
    abstract fun keyEventPaginationDao(): EventPaginationKeyDao



    companion object {
        @Volatile
        private var instance: AppDb? = null

        fun getInstance(context: Context): AppDb {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, AppDb::class.java, "app.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}


class Converters {

    @TypeConverter
    fun listToJson(value: List<String>?) = Gson().toJson(value)

     @TypeConverter
    fun fromString(value: String?): ArrayList<String?>? {
        val listType: Type = object : TypeToken<ArrayList<String?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<String?>?): String? {
        val gson = Gson()
        return gson.toJson(list)
    }


    @TypeConverter
    fun toLongList(value: String?): ArrayList<Long>? {
        val listType: Type = object : TypeToken<ArrayList<Long>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toStringFromLong(list: List<Long>?): String? {
        val gson = Gson()
        return gson.toJson(list)
    }

}