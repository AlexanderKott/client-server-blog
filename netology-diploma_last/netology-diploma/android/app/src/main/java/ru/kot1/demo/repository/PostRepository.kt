package ru.kot1.demo.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.kot1.demo.dto.*
import ru.kot1.demo.entity.EventEntity
import ru.kot1.demo.entity.JobEntity
import ru.kot1.demo.entity.PostEntity
import ru.kot1.demo.entity.UserEntity
import ru.kot1.demo.entity.forWorker.EventWorkEntity
import ru.kot1.demo.viewmodel.CallbackR
import java.io.File


interface AppEntities : UserRepository, PostRepository, EventRepository, JobsRepository,
    AuthMethods, AppWork


enum class AppNetState {
    NO_INTERNET, NO_SERVER_CONNECTION, CONNECTION_ESTABLISHED, THIS_USER_NOT_REGISTERED,
    INCORRECT_PASSWORD, SERVER_ERROR_500
}

enum class RecordOperation {
    NEW_RECORD, CHANGE_RECORD, DELETE_RECORD
}


interface AppWork {
    fun saveViewPagerPageToPrefs(position: Int)
    fun getSavedViewPagerPage(): Int
    fun download(url: String, destFile: File, answer: CallbackR<Byte>)
}



interface AuthMethods {
    suspend fun checkConnection(): AppNetState
    suspend fun authUser(login: String, pass: String, function: (id: Long, token: String) -> Unit)
    suspend fun checkToken(): Boolean
    suspend fun regNewUser(
        login: String,
        pass: String,
        name: String,
        uri : String? = null,
        success: (id: Long, token: String) -> Unit
    )
}

interface JobsRepository {
    suspend fun getJobsById(id: Long)
    suspend fun processJobWork(task: Array<String>)
    suspend fun deleteJob(id: Long)
    suspend fun postJob(jobReq: JobReq)
    suspend fun getJobById(id: Long): JobEntity
    suspend fun saveJobForWorker(job: JobReq): Long
}

interface EventRepository {
    val edata: Flow<PagingData<Event>>
    suspend fun getAllEvents()
    suspend fun processEventWork(task: Array<String>)
    suspend fun saveEventForWorker(event: Event, uri: String?, type: String?): Long
    suspend fun getEventByIdFromDB(id: Long): EventEntity?
    suspend fun sendDeleteEvent(id: Long)
    suspend fun prepareEventFromEntity(event: Event, operation: RecordOperation): Event
    suspend fun sendWholeEventToServer(event: Event)
    suspend fun likeEventById(id: Long)
    suspend fun setDislikeToEventById(id: Long)
    suspend fun participateToEvent(id: Long)
    suspend fun doNotParticipateToEvent(id: Long)
    suspend fun updateEvent(event: Event)
}

interface UserRepository {
    val udata: Flow<PagingData<User>>
    suspend fun getAllUsers()
    fun getUserById(id: Long): LiveData<List<UserEntity>>
    suspend fun getAllUsersFromDB(): List<UserEntity>
}


interface PostRepository {
    val pdata: Flow<PagingData<Post>>
    suspend fun getPostsById(id: Long)
    suspend fun getEventById(id: Long)
    suspend fun getAllPosts()
    suspend fun sendWholePostToServer(post: Post)
    suspend fun disLikeById(id: Long)
    suspend fun likeById(id: Long)
    suspend fun uploadMfileToServer(upload: String): String
    suspend fun savePostForWorker(post: Post, uri: String?, type: String?) : Long
    suspend fun processPostWork(task: Array<String>)
    suspend fun sendDeletePost(id: Long)
    suspend fun getPostById(id: Long): PostEntity
    suspend fun updatePost(post: Post)
}

