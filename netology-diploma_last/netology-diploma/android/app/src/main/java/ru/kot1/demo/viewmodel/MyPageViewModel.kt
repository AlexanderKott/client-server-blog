package ru.kot1.demo.viewmodel

import androidx.lifecycle.*
import androidx.paging.*
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.kot1.demo.adapter.jobs.JobsRemoteMediator
import ru.kot1.demo.adapter.posts.PostRemoteMediator
import ru.kot1.demo.api.ApiService
import ru.kot1.demo.auth.AppAuth
import ru.kot1.demo.db.AppDb
import ru.kot1.demo.entity.UserEntity
import ru.kot1.demo.model.FeedModel
import ru.kot1.demo.model.FeedModelState
import ru.kot1.demo.model.PostModel
import ru.kot1.demo.repository.AppEntities
import ru.kot1.demo.repository.AuthMethods
import javax.inject.Inject


@HiltViewModel
@ExperimentalCoroutinesApi
class MyPageViewModel @Inject constructor(var repository: AppEntities,
                                          var workManager: WorkManager,
                                          var auth: AppAuth,
                                          api: ApiService,
                                          repoNetwork: AuthMethods,
                                          base: AppDb
) : ViewModel() {


    private val userId = MutableStateFlow<Long>(0)

    @ExperimentalPagingApi
    val pagedFlowJobs = userId.flatMapLatest { value ->
        Pager(
            remoteMediator = JobsRemoteMediator(api, base, repoNetwork, userId.value),
            config = PagingConfig(pageSize = 5, enablePlaceholders = false),
            pagingSourceFactory = {  base.jobDao().getAllJobs(value)  }
        ).flow.map {
            it.map{ job ->
                val myOwnJob =  job.toDto()
                    myOwnJob.copy(belongsToMe = true)
                myOwnJob
            }

        }.cachedIn(viewModelScope).flowOn(Dispatchers.Default)
    }



  @ExperimentalPagingApi
    val pagedFlowPosts = userId.flatMapLatest { value ->
        Pager(
            remoteMediator = PostRemoteMediator(api, base, repoNetwork),
            config = PagingConfig(pageSize = 5, enablePlaceholders = false),
            pagingSourceFactory = {  base.postDao().getPosts(value)  }
        ).flow.map {
            it.map { post ->
                PostModel(post = post.toDto().copy(ownedByMe = post.authorId.toLong() == value,
                logined = auth.authStateFlow.value.id != 0L)

                 ) as FeedModel
            }
        }.cachedIn(viewModelScope).flowOn(Dispatchers.Default)
  }



    private val _postsDataState = MutableLiveData<FeedModelState>()
    val postsDataState: LiveData<FeedModelState>
        get() = _postsDataState

    private val _jobsDataState = MutableLiveData<FeedModelState>()
    val jobsDataState: LiveData<FeedModelState>
        get() = _jobsDataState


    private var uid: MutableLiveData<Long> = MutableLiveData<Long>()
    private val _myInfoDataState = uid.switchMap { id ->
        repository.getUserById(id)
    }
    val myInfoDataState: LiveData<List<UserEntity>>
        get() = _myInfoDataState


   fun loadContent(id: Long){
       userId.value = id
       uid.value = id
       loadMyJobs()
       loadMyPosts()
       loadUsers()
   }

    fun loadContent(){
        loadContent(userId.value)
    }

    private fun loadMyPosts() = viewModelScope.launch {
        try {
            _postsDataState.value = FeedModelState(loading = true)
            repository.getAllPosts()
            _postsDataState.value = FeedModelState()
        }   catch (e: Exception) {
            _postsDataState.value = FeedModelState(error = true)
        }
    }


    private fun loadUsers() = viewModelScope.launch {
        try {
            _postsDataState.value = FeedModelState(loading = true)
            repository.getAllUsers()
            _postsDataState.value = FeedModelState()
        }   catch (e: Exception) {
            _postsDataState.value = FeedModelState(error = true)
        }
    }


    private fun loadMyJobs() = viewModelScope.launch {
        try {
            _jobsDataState.value = FeedModelState(loading = true)
            repository.getJobsById(userId.value)
            _jobsDataState.value = FeedModelState()
        }  catch (e: Exception) {
            _jobsDataState.value = FeedModelState(error = true)
        }
    }


}




