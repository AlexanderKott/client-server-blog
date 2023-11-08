package ru.kot1.demo.adapter.jobs

import android.content.Context
import android.util.Log
import androidx.paging.*
import ru.kot1.demo.api.ApiService
import ru.kot1.demo.db.AppDb
import ru.kot1.demo.entity.JobEntity
import ru.kot1.demo.entity.toEntity
import ru.kot1.demo.error.ApiError
import ru.kot1.demo.repository.AppNetState
import ru.kot1.demo.repository.AuthMethods

@ExperimentalPagingApi
class JobsRemoteMediator(private val api: ApiService,
                         private val base: AppDb,
                         private val repoNetwork: AuthMethods,
                         private val userID : Long
)
    : RemoteMediator<Int, JobEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, JobEntity>
            ): MediatorResult {
        try {
            val connected = repoNetwork.checkConnection() == AppNetState.CONNECTION_ESTABLISHED

            if (connected) {
           val response = when (loadType){
                else -> {
                    base.jobDao().deleteAll()
                    api.getJobs(userID)
                }
            }

           if (! response.isSuccessful){
               throw ApiError(response.code(), response.message())
           }

            val body = response.body() ?: throw  ApiError(
                response.code(),
                response.message()
            )

            if (body.isEmpty()){
                return MediatorResult.Success(true)

            }
                base.jobDao().insert(body.toEntity(userID))
            return  MediatorResult.Success(true)
        } else {
            return MediatorResult.Success(true)
        }
        } catch (e: Exception){
           return MediatorResult.Error(e)
        }
    }
}