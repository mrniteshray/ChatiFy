package com.niteshray.xapps.chatify.feature.auth.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niteshray.xapps.chatify.feature.auth.data.repository.AuthRepositoryImpl
import com.niteshray.xapps.chatify.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            val result = repository.login(email, password)
            _authState.value = if (result.isSuccess) {
                AuthState.Success
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun signup(email: String, password: String, name: String) {
        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            val result = repository.signup(email, password, name)
            _authState.value = if (result.isSuccess) {
                AuthState.Success
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Signup failed")
            }
        }
    }


    fun getCurrentUser() = viewModelScope.launch { repository.getCurrentUser() }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}