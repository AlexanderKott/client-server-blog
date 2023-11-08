package ru.kot1.demo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ru.kot1.demo.auth.AppAuth
import ru.kot1.demo.model.FeedModelState
import ru.kot1.demo.model.SingleLiveEvent
import ru.kot1.demo.repository.AppEntities
import javax.inject.Inject


@HiltViewModel
@ExperimentalCoroutinesApi
class UsersViewModel @Inject constructor(var repository: AppEntities,
                                        var workManager: WorkManager,
                                        var auth: AppAuth
) : ViewModel() {
    val cachedusers = repository.udata.cachedIn(viewModelScope)

    private val _dataState = SingleLiveEvent<FeedModelState>()
    val dataState: SingleLiveEvent<FeedModelState>
        get() = _dataState


    fun loadUsers() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAllUsers()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }


}




