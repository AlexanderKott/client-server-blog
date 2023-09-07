package ru.kot1.demo.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.kot1.demo.dto.*


interface ApiService {

    //USER auth and login
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("users/authentication")
    suspend fun authMe (@Query("login") login: String,@Query("pass") pass: String)
            : Response<AuthState>

    @Multipart
    @POST("users/registration")
    suspend fun regMeAvatar (@Query("login") login: String,
                             @Query("pass") pass: String,
                             @Query("name") name: String,
                             @Part media: MultipartBody.Part?
    )
            : Response<AuthState>

    // trick to check am I authenticatificated
    @POST("posts/-555/likes")
    suspend fun checkToken(): Response<Unit>


    // JOBS
    @POST("my/jobs")
    suspend fun postJob(@Body jobReq: JobReq): Response<Unit>

    @DELETE("my/jobs/{id}")
    suspend fun deleteJob(@Path("id") id : Long): Response<Unit>

    @GET("{id}/jobs")
    suspend fun getJobs(@Path("id") id : Long): Response<List<Job>>




    //EVENTS
    @GET("events")
    suspend fun getAllEvents(): Response<List<Event>>

    @GET("events/{id}")
    suspend fun getEventById(@Path("id") id: Long): Response<Event>

    @POST("events")
    suspend fun saveEvent(@Body event: Event): Response<Event>

    @DELETE ("events/{id}")
    suspend fun deleteEvent(@Path("id") eventId: Long): Response<Unit>

    @POST ("events/{id}/likes")
    suspend fun setLikeToEvent(@Path("id") id: Long): Response<Event>

    @DELETE ("events/{id}/likes")
    suspend fun setDislikeToEvent(@Path("id") postId: Long): Response<Event>

    @POST ("events/{id}/participants")
    suspend fun participateToEvent(@Path("id") id: Long): Response<Event>

    @DELETE  ("events/{id}/participants")
    suspend fun doNotParticipateToEvent(@Path("id") id: Long): Response<Event>





    //USERS
    @GET("users")
    suspend fun getAllUsers(): Response<List<User>>


    // POSTS
    @GET("posts")
    suspend fun getAllPosts(): Response<List<Post>>

    @POST("posts")
    suspend fun savePost(@Body post: Post): Response<Post>

    @GET("{id}/wall")
    suspend fun getWall(@Path("id") id: Long): Response<List<Post>>

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Unit>

    @DELETE ("posts/{postId}")
    suspend fun deletePost(@Path("postId") postId: Long): Response<Unit>

    @POST ("posts/{postId}/likes")
    suspend fun setLikeToPost(@Path("postId") postId: Long): Response<Post>

    @DELETE ("posts/{postId}/likes")
    suspend fun setDislikeToPost(@Path("postId") postId: Long): Response<Post>



    // upload media
    @Multipart
    @POST("media")
    suspend fun upload(@Part media: MultipartBody.Part): Response<Media>

}