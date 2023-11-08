package ru.kot1.demo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.kot1.demo.adapter.jobs.JobsRemoteMediator
import ru.kot1.demo.api.ApiService
import ru.kot1.demo.db.AppDb
import ru.kot1.demo.dto.EventUI
import ru.kot1.demo.dto.JobReq
import ru.kot1.demo.dto.JobUI
import ru.kot1.demo.entity.JobEntity
import ru.kot1.demo.model.FeedModelState
import ru.kot1.demo.model.SingleLiveEvent
import ru.kot1.demo.repository.AppEntities
import ru.kot1.demo.repository.AuthMethods
import ru.kot1.demo.repository.RecordOperation
import ru.kot1.demo.work.SaveJobWorker
import javax.inject.Inject


@HiltViewModel
@ExperimentalCoroutinesApi
class JobsViewModel @Inject constructor(
    var repository: AppEntities,
    var workManager: WorkManager,
    api: ApiService,
    repoNetwork: AuthMethods,
    base: AppDb
) : ViewModel() {

    private val _dataState = SingleLiveEvent<FeedModelState>()
    val dataState: SingleLiveEvent<FeedModelState>
        get() = _dataState

    private val userId = MutableStateFlow<Long>(0)

    @ExperimentalPagingApi
    val pagedFlowJobs = userId.flatMapLatest { value ->
        Pager(
            remoteMediator = JobsRemoteMediator(api, base, repoNetwork, userId.value),
            config = PagingConfig(pageSize = 5, enablePlaceholders = false),
            pagingSourceFactory = { base.jobDao().getAllJobs(value) }
        ).flow.map {
            it.map(JobEntity::toDto)
        }.cachedIn(viewModelScope)
    }


    private val _loadJob = SingleLiveEvent<JobUI>()
    val loadJob: SingleLiveEvent<JobUI>
        get() = _loadJob


    fun refreshJobs() {
        loadJobsById(userId.value)
    }

    fun loadJobsById(id: Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            userId.value = id
            repository.getJobsById(id)
            _dataState.value = FeedModelState()
        }  catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun postNewOrChangedJob(id: Long, cname: String, pos: String, start: Long, finish: Long) = viewModelScope.launch {
        try {
            repository.saveJobForWorker(JobReq(id = id, position = pos,
                name = cname, finish = start, start = finish))
            initWorkManager(id.toInt(), RecordOperation.NEW_RECORD)
        } catch (e: Exception) {
        }

    }


    fun deleteJob(id: Long){
        try {
            initWorkManager(id.toInt(), RecordOperation.DELETE_RECORD)
        } catch (e: Exception) {
          }
    }


    private fun initWorkManager(id: Int, operation: RecordOperation) {
        val data = workDataOf(
            SaveJobWorker.postKey to arrayOf(operation.toString(),"$id")
        )

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SaveJobWorker>()
            .setInputData(data)
            .setConstraints(constraints)
            .build()
        workManager.enqueue(request)
    }

    fun loadThisJobToUI(jobId: Long) = viewModelScope.launch {
        if (jobId != 0L){
            val job = repository.getJobById(jobId)
            _loadJob.value = JobUI(job.name, job.position, job.start, job.finish)
        }
    }


}




