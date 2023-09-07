package ru.kot1.demo.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.kot1.demo.activity.utils.prepareFileName
import ru.kot1.demo.auth.AppAuth
import ru.kot1.demo.dto.Post
import ru.kot1.demo.model.FeedModel
import ru.kot1.demo.model.FeedModelState
import ru.kot1.demo.model.PostModel
import ru.kot1.demo.model.SingleLiveEvent
import ru.kot1.demo.repository.AppEntities
import java.io.File
import javax.inject.Inject


@HiltViewModel
@ExperimentalCoroutinesApi
class PostAllViewModel @Inject constructor(
    var repository: AppEntities,
    var workManager: WorkManager,
    var auth: AppAuth,
    var context: Application
) : AndroidViewModel(context) {

    private val cachedposts = repository.pdata.cachedIn(viewModelScope)

    private val _dataState = SingleLiveEvent<FeedModelState>()
    val dataState: SingleLiveEvent<FeedModelState>
        get() = _dataState


    val feedModels = auth.authStateFlow.flatMapLatest { user ->
        cachedposts.map { pagingData ->
            pagingData.map { post ->
                PostModel(post = post.copy(logined = user.id != 0L)) as FeedModel
            }
        }
    }


    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAllPosts()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getAllPosts()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }




    fun like(post: Post) = viewModelScope.launch {
        try {
            if (post.likedByMe) {
                repository.disLikeById(post.id)
            } else {
                repository.likeById(post.id)
            }
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }


}




