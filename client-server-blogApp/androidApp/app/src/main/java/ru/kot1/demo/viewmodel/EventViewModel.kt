package ru.kot1.demo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import ru.kot1.demo.adapter.events.EventsRemoteMediator
import ru.kot1.demo.api.ApiService
import ru.kot1.demo.auth.AppAuth
import ru.kot1.demo.db.AppDb
import ru.kot1.demo.dto.Event
import ru.kot1.demo.entity.UserEntity
import ru.kot1.demo.model.FeedModelState
import ru.kot1.demo.model.SingleLiveEvent
import ru.kot1.demo.repository.AppEntities
import ru.kot1.demo.repository.AuthMethods
import javax.inject.Inject


@HiltViewModel
@ExperimentalCoroutinesApi
class EventViewModel @Inject constructor(var repository: AppEntities,
                                         var workManager: WorkManager,
                                         var auth: AppAuth,
                                         var api: ApiService,
                                         var repoNetwork: AuthMethods,
                                         var base: AppDb
) : ViewModel() {


    private val _dataState = SingleLiveEvent<FeedModelState>()
    val dataState: SingleLiveEvent<FeedModelState>
        get() = _dataState

    private val userId = MutableStateFlow<Long>(0)

    @ExperimentalPagingApi
    suspend fun events(): Flow<PagingData<Event>> {
        val allUsers : List<UserEntity> = repository.getAllUsersFromDB()

        return userId.flatMapLatest { value ->
            Pager(
                remoteMediator = EventsRemoteMediator(api, base, repoNetwork),
                config = PagingConfig(pageSize = 5, enablePlaceholders = false),
                pagingSourceFactory = { base.eventDao().getAll() }
            ).flow.map {
                it.filter{
                    it.authorId == auth.authStateFlow.value.id
                }.map { event ->
                    val spekersID = event.speakerIds ?: mutableListOf()

                    val eventDTO = event.toDto().copy(belongsToMe = true,
                        logined = true,
                        speakerNames = allUsers.filter { user ->
                            spekersID.contains(user.id)
                        }
                            .map { user -> user.name })
                    eventDTO
                }

            }.cachedIn(viewModelScope).flowOn(Dispatchers.Default)
        }
    }


    fun loadEventsForUser(it: Long) {
        userId.value = it
    }

}




