package ru.kot1.demo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import ru.kot1.demo.auth.AppAuth
import ru.kot1.demo.dto.AuthState
import ru.kot1.demo.model.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(var auth: AppAuth) : ViewModel() {

    val authData: LiveData<AuthState> = auth
        .authStateFlow
        .asLiveData(Dispatchers.Default)

    val authenticated: Boolean
        get() = auth.authStateFlow.value.id != 0L


    private val _mutableSelectedItem = SingleLiveEvent<Boolean>()
    val logined: SingleLiveEvent<Boolean> get() = _mutableSelectedItem

    fun selectMyPage() {
        _mutableSelectedItem.value = true
    }

    fun markMyPageAlreadyOpened(){
        _mutableSelectedItem.value = false
    }
}