package ru.kot1.demo.viewmodel

import androidx.lifecycle.*
import androidx.paging.*
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.kot1.demo.auth.AppAuth
import ru.kot1.demo.model.*
import ru.kot1.demo.repository.AppEntities
import javax.inject.Inject


@HiltViewModel
@ExperimentalCoroutinesApi
class PostViewModel @Inject constructor(var repository: AppEntities ,
                                        var workManager: WorkManager,
                                        var auth: AppAuth
) : ViewModel() {

    val cachedposts = repository.pdata.cachedIn(viewModelScope)

    private val userId = MutableStateFlow<Long>(0)

    val feedModels = userId.flatMapLatest { id ->
        cachedposts.map { pagingData ->
            if (userId.value == 0L) {
                pagingData.map { post ->
                    PostModel(post = post.copy(logined = false)) as FeedModel
                }
            } else {
                pagingData.filter { userId.value == it.authorId }
                    .map { post ->
                        if (userId.value == auth.authStateFlow.value.id) {
                            PostModel(post = post.copy(logined = true)) as FeedModel
                        } else {
                            PostModel(post = post) as FeedModel
                        }
                    }

            }
        }
    }



    private val _dataState = SingleLiveEvent<FeedModelState>()
    val dataState: SingleLiveEvent<FeedModelState>
        get() = _dataState


    fun getWallById(id: Long) = viewModelScope.launch {
        userId.value = id
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getPostsById(id)
            _dataState.value = FeedModelState()

        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }


    fun refreshPosts() = viewModelScope.launch {
        getWallById(userId.value)
    }
}




