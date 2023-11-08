package ru.kot1.demo.viewmodel

import androidx.lifecycle.*
import androidx.paging.*
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.kot1.demo.adapter.events.EventsRemoteMediator
import ru.kot1.demo.api.ApiService
import ru.kot1.demo.auth.AppAuth
import ru.kot1.demo.db.AppDb
import ru.kot1.demo.dto.Event
import ru.kot1.demo.model.*
import ru.kot1.demo.repository.AppEntities
import ru.kot1.demo.repository.AuthMethods
import javax.inject.Inject


@HiltViewModel
@ExperimentalCoroutinesApi
class EventAllViewModel @Inject constructor(
    var repository: AppEntities,
    var workManager: WorkManager,
    var auth: AppAuth,
    var api: ApiService,
    var repoNetwork: AuthMethods,
    var base: AppDb
) : ViewModel() {


    @ExperimentalPagingApi
    suspend fun events(): Flow<PagingData<Event>> {
        val allUsers = repository.getAllUsersFromDB()

        return auth.authStateFlow.flatMapLatest { value ->
            Pager(
                remoteMediator = EventsRemoteMediator(api, base, repoNetwork),
                config = PagingConfig(pageSize = 5, enablePlaceholders = false),
                pagingSourceFactory = { base.eventDao().getAll() }
            ).flow.map {
                it.map { event ->
                    val spekersID = event.speakerIds ?: mutableListOf()

                    val eventDTO = event.toDto().copy(logined = value.id != 0L,
                    speakerNames = allUsers.filter { user ->
                        spekersID.contains(user.id) }
                        .map { user -> user.name })
                    eventDTO
                }

            }.cachedIn(viewModelScope).flowOn(Dispatchers.Default)
        }
    }



    private val _dataState = SingleLiveEvent<FeedModelState>()
    val dataState: SingleLiveEvent<FeedModelState>
        get() = _dataState

    fun loadEvents() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAllEvents()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }


}




