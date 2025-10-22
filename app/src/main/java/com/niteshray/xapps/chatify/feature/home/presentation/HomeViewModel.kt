package com.niteshray.xapps.chatify.feature.home.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niteshray.xapps.chatify.feature.auth.domain.model.User
import com.niteshray.xapps.chatify.feature.home.data.repository.HomeRepositoryImpl
import com.niteshray.xapps.chatify.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: HomeRepository = HomeRepositoryImpl()
) : ViewModel() {

    private val _usersState = MutableLiveData<UsersState>(UsersState.Loading)
    val usersState: LiveData<UsersState> = _usersState

    fun loadAllUsers(currentUserId: String) {
        _usersState.value = UsersState.Loading
        
        viewModelScope.launch {
            try {
                val result = repository.getAllUsers(currentUserId)
                _usersState.value = if (result.isSuccess) {
                    val users = result.getOrNull() ?: emptyList()
                    if (users.isEmpty()) {
                        UsersState.Empty
                    } else {
                        UsersState.Success(users)
                    }
                } else {
                    UsersState.Error(result.exceptionOrNull()?.message ?: "Failed to load users")
                }
            } catch (e: Exception) {
                _usersState.value = UsersState.Error(e.message ?: "Failed to load users")
            }
        }
    }
}

sealed class UsersState {
    object Loading : UsersState()
    object Empty : UsersState()
    data class Success(val users: List<User>) : UsersState()
    data class Error(val message: String) : UsersState()
}
